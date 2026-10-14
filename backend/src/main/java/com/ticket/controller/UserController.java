package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.entity.User;
import com.ticket.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ticket:list') or hasAuthority('ticket:create')")
    public Result<List<User>> list() {
        return Result.ok(userService.listAll());
    }
}
