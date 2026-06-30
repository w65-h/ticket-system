package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.service.DashboardService;
import com.ticket.vo.DashboardVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAuthority('dashboard:view')")
    public Result<DashboardVO> getDashboard() {
        return Result.ok(dashboardService.getDashboardData());
    }
}