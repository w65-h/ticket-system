package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.dto.LoginDTO;
import com.ticket.service.AuthService;
import com.ticket.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO vo = authService.login(loginDTO);
        return Result.ok(vo);
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return Result.ok();
    }
}