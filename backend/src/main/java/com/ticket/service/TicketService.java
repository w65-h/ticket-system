package com.ticket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ticket.dto.TicketCreateDTO;
import com.ticket.dto.TicketQueryDTO;
import com.ticket.dto.TicketStatusDTO;
import com.ticket.entity.Ticket;

public interface TicketService extends IService<Ticket> {

    Ticket createTicket(TicketCreateDTO dto);

    Page<Ticket> queryTickets(TicketQueryDTO query);

    void updateStatus(TicketStatusDTO dto);

    String generateTicketNo();
}