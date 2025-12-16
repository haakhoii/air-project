package com.air.user_service.controller;

import com.air.common_service.dto.ApiResponse;
import com.air.common_service.dto.request.PasswordUpdateRequest;
import com.air.common_service.dto.request.UserCreationRequest;
import com.air.common_service.dto.response.PageResponse;
import com.air.common_service.dto.response.UserResponse;
import com.air.common_service.dto.request.UserUpdatedRequest;
import com.air.user_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping("/registration")
    ApiResponse<UserResponse> registration(@RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.registration(request))
                .build();
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<PageResponse<UserResponse>> getList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "searchByRole", required = false) String searchByRole
    ) {
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(userService.getList(page, size, searchByRole))
                .build();
    }

    @GetMapping("/details")
    @PreAuthorize("isAuthenticated()")
    ApiResponse<UserResponse> getUserDetails(
            @RequestParam(name = "userId", required = false) String userId
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserDetails(userId))
                .build();
    }

    @PutMapping("/upd")
    @PreAuthorize("isAuthenticated()")
    ApiResponse<UserResponse> updateUser(
            @RequestParam(name = "userId", required = false) String userId,
            @RequestBody UserUpdatedRequest request
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @PatchMapping("/password")
    @PreAuthorize("isAuthenticated()")
    ApiResponse<String> updatePassword(
            @RequestParam(name = "userId", required = false) String userId,
            @RequestBody PasswordUpdateRequest request
    ) {
        return ApiResponse.<String>builder()
                .result(userService.changePassword(userId, request))
                .build();
    }

    @DeleteMapping("/del")
    @PreAuthorize("hasRole('USER')")
    ApiResponse<String> deleteUser(
            @RequestParam(name = "userId") String userId
    ) {
        return ApiResponse.<String>builder()
                .result(userService.deleteUser(userId))
                .build();
    }

}
