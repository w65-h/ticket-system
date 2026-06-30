package com.ticket.dto;

import lombok.Data;

@Data
public class TicketQueryDTO {

    private String ticketNo;
    private String title;
    private Integer status;
    private Integer priority;
    private Long assigneeId;
    private Long deptId;
    private String startTime;
    private String endTime;
    private Integer page = 1;
    private Integer size = 10;
}