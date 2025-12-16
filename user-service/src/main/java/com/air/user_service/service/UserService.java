package com.air.user_service.service;

import com.air.common_service.constants.PredefinedRole;
import com.air.common_service.dto.request.PasswordUpdateRequest;
import com.air.common_service.dto.request.UserCreationRequest;
import com.air.common_service.dto.response.PageResponse;
import com.air.common_service.dto.response.UserResponse;
import com.air.common_service.dto.request.UserUpdatedRequest;
import com.air.common_service.exception.AppException;
import com.air.common_service.exception.ErrorCode;
import com.air.user_service.entity.Role;
import com.air.user_service.entity.User;
import com.air.user_service.mapper.UserMapper;
import com.air.user_service.repository.RoleRepository;
import com.air.user_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    public UserResponse registration(UserCreationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new AppException(ErrorCode.USER_EXISTS);
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);

        User user = UserMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER.getRoleName()).ifPresent(roles::add);
        user.setRoles(roles);
        user = userRepository.save(user);

        return UserMapper.toUserResponse(user);
    }

    public PageResponse<UserResponse> getList(int page, int size, String searchByRole) {
        Pageable pageable = PageRequest.of(page -1, size);
        Page<User> userPage;

        if (searchByRole != null && !searchByRole.isBlank()) {
            userPage = userRepository.findAllByRoleName(searchByRole.toUpperCase(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(UserMapper::toUserResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .currentPage(page)
                .totalPages(userPage.getTotalPages())
                .pageSize(size)
                .totalElements(userPage.getTotalElements())
                .data(userResponses)
                .build();
    }

    public UserResponse getUserDetails(String userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String targetUserId;

        if (isAdmin) {
            targetUserId = (userId != null && !userId.isBlank())
                    ? userId
                    : currentUserId;
        } else {
            if (userId != null && !userId.isBlank()) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
            targetUserId = currentUserId;
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        return UserMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(String userId, UserUpdatedRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        String targetUserId;

        if (isAdmin) {
            targetUserId = (userId != null && !userId.isBlank())
                    ? userId
                    : currentUserId;
        } else {
            if (userId != null && !userId.isBlank()) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            targetUserId = currentUserId;
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userRepository.save(user);

        return UserMapper.toUserResponse(user);
    }

    @Transactional
    public String changePassword(String userId, PasswordUpdateRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String targetUserId;

        if (isAdmin) {
            targetUserId = (userId != null && !userId.isBlank())
                    ? userId
                    : currentUserId;
        } else {
            if (userId != null && !userId.isBlank()) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            targetUserId = currentUserId;
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (targetUserId.equals(currentUserId)) {
            if (request.getOldPassword() == null ||
                    !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new AppException(ErrorCode.INCORRECT_PASSWORD);
            }
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        if (isAdmin && !targetUserId.equals(currentUserId)) {
            return "Password reset successfully for userId: " + targetUserId;
        }

        return "Password updated successfully";

    }

    @Transactional
    public String deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isAdminUser = user.getRoles().stream()
                .anyMatch(auth -> auth.getName().equals("ADMIN"));

        if (isAdminUser) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        userRepository.delete(user);

        return "User deleted successfully with userId: " + userId;
    }
}
