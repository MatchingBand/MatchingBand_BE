package com.mate.band.domain.user.controller;

import com.mate.band.domain.user.dto.SignUpRequest;
import com.mate.band.domain.user.service.UserService;
import com.mate.band.global.util.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "UserController", description = "회원 관련 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(hidden = true)
    @PostMapping("/signUp")
    public ApiResponse<?> signUp(@RequestBody SignUpRequest requestSignUp) {
        return ApiResponse.success();
    }

}
