package com.ticket.vo;

import lombok.Data;

@Data
public class DeptStatVO {
    private Long deptId;
    private String deptName;
    private long totalCount;
    private long completedCount;
    private double avgHours;
}