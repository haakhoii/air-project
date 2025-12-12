package com.air.seat_service.service;

import com.air.common_service.constants.SeatClass;
import com.air.common_service.constants.SeatStatus;
import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.HoldSeatRequest;
import com.air.common_service.dto.request.SeatRequest;
import com.air.common_service.dto.request.VerifyHoldRequest;
import com.air.common_service.dto.response.FlightResponse;
import com.air.common_service.dto.response.SeatResponse;
import com.air.common_service.exception.AppException;
import com.air.common_service.exception.ErrorCode;
import com.air.seat_service.entity.Seat;
import com.air.seat_service.httpclient.FlightClient;
import com.air.seat_service.mapper.SeatMapper;
import com.air.seat_service.repository.SeatRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatService {
    SeatRepository seatRepository;
    FlightClient flightClient;
    RedisTemplate<String, Object> redisTemplate;

    String  SEAT_HOLD_KEY = "seat:hold:";
    long HOLD_TTL_SECONDS = 60;

    public List<SeatResponse> create(SeatRequest request) {
        FlightResponse flight = flightClient.getFlightById(request.getFlightId()).getResult();
        int seatsEconomy = flight.getSeatsEconomy();
        int seatsBusiness = flight.getSeatsBusiness();

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= seatsEconomy; i++) {
            seats.add(Seat.builder()
                    .flightId(request.getFlightId())
                    .seatClass(SeatClass.ECONOMY)
                    .seatNumber("E" + i)
                    .seatStatus(SeatStatus.AVAILABLE)
                    .holdBy(null)
                    .build());
        }

        for (int i = 1; i <= seatsBusiness; i++) {
            seats.add(Seat.builder()
                    .flightId(request.getFlightId())
                    .seatClass(SeatClass.BUSINESS)
                    .seatNumber("B" + i)
                    .seatStatus(SeatStatus.AVAILABLE)
                    .holdBy(null)
                    .build());
        }

        List<Seat> savedSeats = seatRepository.saveAll(seats);

        return SeatMapper.toSeatResponseList(savedSeats, flight);
    }

    public List<SeatResponse> getSeatsByFlight(String flightId) {
        List<Seat> seats = seatRepository.findByFlightId(flightId);
        FlightResponse flight = flightClient.getFlightById(flightId).getResult();
        return SeatMapper.toSeatResponseList(seats, flight);
    }

    @Transactional
    public List<SeatResponse> holdSeat(HoldSeatRequest request) {
        List<String> seatIds = request.getSeatIds();
        String holdBy = SecurityContextHolder.getContext().getAuthentication().getName();

        List<String> acquiredKeys = new ArrayList<>();
        try {
            for (String seatId : seatIds) {
                String lockKey = SEAT_HOLD_KEY + seatId + ":lock";
                Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, holdBy, 5, TimeUnit.SECONDS);
                if (!Boolean.TRUE.equals(ok)) {
                    for (String k : acquiredKeys) redisTemplate.delete(k);
                    throw new AppException(ErrorCode.SEAT_ALREADY_HELD);
                }
                acquiredKeys.add(lockKey);
            }

            List<Seat> seats = seatRepository.findAllByIdInForUpdate(seatIds);

            if (seats.size() != seatIds.size()) {
                // release locks
                for (String k : acquiredKeys) redisTemplate.delete(k);
                throw new AppException(ErrorCode.SEAT_NOT_FOUND);
            }

            for (Seat seat : seats) {
                if (seat.getSeatStatus() == SeatStatus.BOOKED) {
                    throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
                }
                String holdKey = SEAT_HOLD_KEY + seat.getId();
                boolean redisHeld = Boolean.TRUE.equals(redisTemplate.hasKey(holdKey));
                if (seat.getSeatStatus() == SeatStatus.HOLD && !redisHeld) {
                    throw new AppException(ErrorCode.SEAT_ALREADY_HELD);
                }
                seat.setSeatStatus(SeatStatus.HOLD);
                seat.setHoldBy(holdBy);
            }

            seatRepository.saveAll(seats);

            for (String seatId : seatIds) {
                String holdKey = SEAT_HOLD_KEY + seatId;
                redisTemplate.opsForValue().set(holdKey, holdBy, HOLD_TTL_SECONDS, TimeUnit.SECONDS);
            }

            for (String lockKey : acquiredKeys) redisTemplate.delete(lockKey);

            FlightResponse flight = flightClient.getFlightById(seats.get(0).getFlightId()).getResult();
            return SeatMapper.toSeatResponseList(seats, flight);

        } catch (AppException ae) {
            for (String k : acquiredKeys) {
                try { redisTemplate.delete(k); } catch (Exception ignore) {}
            }
            throw ae;
        } catch (Exception ex) {
            for (String k : acquiredKeys) {
                try { redisTemplate.delete(k); } catch (Exception ignore) {}
            }
            throw ex;
        }
    }

    public List<SeatResponse> releaseSeats(HoldSeatRequest request) {
        List<String> seatIds = request.getSeatIds();
        List<Seat> seats = seatRepository.findAllById(seatIds);
        String holdBy = SecurityContextHolder.getContext().getAuthentication().getName();

        for (Seat seat : seats) {
            if (!holdBy.equals(seat.getHoldBy())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        for (String seatId : seatIds) {
            redisTemplate.delete(SEAT_HOLD_KEY + seatId);
        }

        for (Seat seat : seats) {
            seat.setSeatStatus(SeatStatus.AVAILABLE);
            seat.setHoldBy(null);
        }
        seatRepository.saveAll(seats);
        FlightResponse flight = flightClient.getFlightById(seats.get(0).getFlightId()).getResult();

        return SeatMapper.toSeatResponseList(seats, flight);
    }

    @Transactional
    public List<SeatResponse> cancelHold(HoldSeatRequest request) {

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        for (Seat seat : seats) {

            String redisKey = "seat:hold:" + seat.getId();
            redisTemplate.delete(redisKey);

            if (seat.getSeatStatus() == SeatStatus.HOLD || seat.getSeatStatus() == SeatStatus.BOOKED) {
                seat.setSeatStatus(SeatStatus.AVAILABLE);
                seat.setHoldBy(null);
            }
        }

        seatRepository.saveAll(seats);
        FlightResponse flight = flightClient.getFlightById(seats.get(0).getFlightId()).getResult();

        return SeatMapper.toSeatResponseList(seats, flight);
    }

    @Transactional
    public List<SeatResponse> bookSeats(HoldSeatRequest request) {
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        FlightResponse flight = flightClient.getFlightById(seats.get(0).getFlightId()).getResult();

        for (Seat seat : seats) {
            String redisKey = SEAT_HOLD_KEY + seat.getId();

            boolean exist = Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
            if (!exist || !user.equals(seat.getHoldBy())) {
                throw new AppException(ErrorCode.SEAT_NOT_HELD);
            }
            seat.setSeatStatus(SeatStatus.BOOKED);
            redisTemplate.delete(redisKey);
        }

        seatRepository.saveAll(seats);

        return SeatMapper.toSeatResponseList(seats, flight);
    }

    public List<SeatResponse> getSeats(HoldSeatRequest request) {
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        FlightResponse flight = flightClient.getFlightById(seats.get(0).getFlightId()).getResult();

        if (seats.isEmpty()) {
            throw new AppException(ErrorCode.SEAT_NOT_FOUND);
        }

        return SeatMapper.toSeatResponseList(seats, flight);
    }

    public Boolean verifyHold(VerifyHoldRequest request) {
        String currentUser = request.getUserId();
        if (currentUser == null || currentUser.isBlank()) {
            currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        List<String> seatIds = request.getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            throw new AppException(ErrorCode.SEAT_NOT_FOUND);
        }

        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new AppException(ErrorCode.SEAT_NOT_FOUND);
        }

        for (Seat seat : seats) {
            if (seat.getSeatStatus() != SeatStatus.HOLD) {
                throw new AppException(ErrorCode.SEAT_NOT_HELD);
            }

            if (!currentUser.equals(seat.getHoldBy())) {
                throw new AppException(ErrorCode.SEAT_ALREADY_HELD);
            }

            String holdKey = SEAT_HOLD_KEY + seat.getId();
            Long ttl = redisTemplate.getExpire(holdKey);
            if (ttl == null || ttl <= 0) {
                throw new AppException(ErrorCode.SEAT_HOLD_EXPIRED);
            }
        }

        return true;
    }

}
