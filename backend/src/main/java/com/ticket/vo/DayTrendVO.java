package com.ticket.vo;

import lombok.Data;

@Data
public class DayTrendVO {
    private String date;
    private long createCount;
    private long completeCount;
}