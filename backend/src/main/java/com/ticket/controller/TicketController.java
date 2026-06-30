package com.ticket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ticket.common.PageResult;
import com.ticket.common.Result;
import com.ticket.dto.TicketCreateDTO;
import com.ticket.dto.TicketQueryDTO;
import com.ticket.dto.TicketStatusDTO;
import com.ticket.entity.Ticket;
import com.ticket.service.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    @Resource
    private TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAuthority('ticket:create')")
    public Result<Ticket> create(@Valid @RequestBody TicketCreateDTO dto) {
        Ticket ticket = ticketService.createTicket(dto);
        return Result.ok(ticket);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ticket:list')")
    public Result<PageResult<Ticket>> list(TicketQueryDTO query) {
        Page<Ticket> page = ticketService.queryTickets(query);
        return Result.ok("查询成功",
                PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ticket:list')")
    public Result<Ticket> detail(@PathVariable Long id) {
        Ticket ticket = ticketService.getById(id);
        return Result.ok(ticket);
    }

    @PutMapping("/status")
    @PreAuthorize("hasAuthority('ticket:process')")
    public Result<?> updateStatus(@Valid @RequestBody TicketStatusDTO dto) {
        ticketService.updateStatus(dto);
        return Result.ok("状态更新成功");
    }
}