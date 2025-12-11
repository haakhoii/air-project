package com.air.user_service.service;

import com.air.common_service.constants.PredefinedRole;
import com.air.common_service.dto.request.UserCreationRequest;
import com.air.common_service.dto.response.UserResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

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
}
