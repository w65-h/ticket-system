package com.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ticket")
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;
    private String title;
    private String content;

    /**
     * 1-待处理 2-处理中 3-待验收 4-已完成 5-已驳回
     */
    private Integer status;

    /**
     * 1-低 2-中 3-高 4-紧急
     */
    private Integer priority;

    private Long creatorId;
    private Long assigneeId;
    private Long deptId;

    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private String assigneeName;
    @TableField(exist = false)
    private String deptName;

    private LocalDateTime deadline;
    private LocalDateTime finishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}