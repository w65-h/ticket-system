package com.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String content;

    /**
     * 0-未读 1-已读
     */
    private Integer isRead;

    private Integer retryCount;
    private Integer maxRetry;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}