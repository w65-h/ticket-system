package com.ticket.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalTickets;
    private long pendingTickets;
    private long processingTickets;
    private long completedTickets;
    private long rejectedTickets;
    private double avgProcessHours;
    private List<StatusStatVO> statusStats;
    private List<DeptStatVO> deptStats;
    private List<DayTrendVO> dayTrend;
}