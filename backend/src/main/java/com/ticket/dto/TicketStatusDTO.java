package com.ticket.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TicketStatusDTO {

    @NotNull(message = "工单ID不能为空")
    private Long ticketId;

    @NotNull(message = "目标状态不能为空")
    private Integer targetStatus;

    private String remark;
}