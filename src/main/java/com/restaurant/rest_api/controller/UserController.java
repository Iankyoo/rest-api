package com.restaurant.rest_api.controller;

import com.restaurant.rest_api.dto.UpdateRoleRequest;
import com.restaurant.rest_api.dto.UserResponse;
import com.restaurant.rest_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id, @RequestBody @Valid UpdateRoleRequest request){
        return ResponseEntity.ok(userService.updateRole(id, request.role()));
    }
}
