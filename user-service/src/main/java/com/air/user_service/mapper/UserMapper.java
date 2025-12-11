package com.air.user_service.mapper;

import com.air.common_service.dto.request.UserCreationRequest;
import com.air.common_service.dto.response.RoleResponse;
import com.air.common_service.dto.response.UserResponse;
import com.air.user_service.entity.User;

import java.util.stream.Collectors;

public class UserMapper {
    public static User toUser(UserCreationRequest request) {
        return User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .avatar(request.getAvatar())
                .build();
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(user.getRoles().stream()
                        .map(role -> RoleResponse.builder()
                                .name(role.getName())
                                .description(role.getDescription())
                                .build()
                        ).collect(Collectors.toSet())
                )
                .build();
    }
}
