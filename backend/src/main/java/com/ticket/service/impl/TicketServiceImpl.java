package com.ticket.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticket.common.BusinessException;
import com.ticket.config.RabbitMQConfig;
import com.ticket.dto.TicketCreateDTO;
import com.ticket.dto.TicketQueryDTO;
import com.ticket.dto.TicketStatusDTO;
import com.ticket.entity.Notification;
import com.ticket.entity.Ticket;
import com.ticket.entity.TicketRecord;
import com.ticket.entity.User;
import com.ticket.mapper.NotificationMapper;
import com.ticket.mapper.TicketMapper;
import com.ticket.mapper.TicketRecordMapper;
import com.ticket.mapper.UserMapper;
import com.ticket.service.DepartmentService;
import com.ticket.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService {

    @Resource
    private TicketRecordMapper ticketRecordMapper;

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ticket createTicket(TicketCreateDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User creator = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Ticket ticket = new Ticket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setTitle(dto.getTitle());
        ticket.setContent(dto.getContent());
        ticket.setPriority(dto.getPriority());
        ticket.setStatus(1);
        ticket.setCreatorId(creator.getId());
        ticket.setAssigneeId(dto.getAssigneeId());
        ticket.setDeptId(dto.getDeptId() != null ? dto.getDeptId() : creator.getDeptId());

        if (StrUtil.isNotBlank(dto.getDeadline())) {
            ticket.setDeadline(LocalDateTime.parse(dto.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        save(ticket);

        TicketRecord record = new TicketRecord();
        record.setTicketId(ticket.getId());
        record.setOperation("创建工单");
        record.setFromStatus(null);
        record.setToStatus(1);
        record.setOperatorId(creator.getId());
        record.setOperatorName(creator.getRealName());
        ticketRecordMapper.insert(record);

        if (dto.getAssigneeId() != null) {
            sendStatusNotification(dto.getAssigneeId(), ticket.getTicketNo(), "您有一个新的工单待处理: " + ticket.getTitle());
        }

        return ticket;
    }

    @Override
    public Page<Ticket> queryTickets(TicketQueryDTO query) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        List<Long> deptIds = departmentService.getSubDeptIds(currentUser.getDeptId());

        Page<Ticket> page = new Page<>(query.getPage(), query.getSize());
        return baseMapper.selectTicketPage(page, query, deptIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(TicketStatusDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User operator = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        Ticket ticket = getById(dto.getTicketId());
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        Integer fromStatus = ticket.getStatus();
        ticket.setStatus(dto.getTargetStatus());

        if (dto.getTargetStatus() == 4) {
            ticket.setFinishTime(LocalDateTime.now());
        }

        updateById(ticket);

        TicketRecord record = new TicketRecord();
        record.setTicketId(ticket.getId());
        record.setOperation(getStatusOperation(dto.getTargetStatus()));
        record.setFromStatus(fromStatus);
        record.setToStatus(dto.getTargetStatus());
        record.setOperatorId(operator.getId());
        record.setOperatorName(operator.getRealName());
        record.setRemark(dto.getRemark());
        ticketRecordMapper.insert(record);

        sendStatusNotification(ticket.getCreatorId(), ticket.getTicketNo(),
                "工单 " + ticket.getTicketNo() + " 状态已变更为: " + getStatusName(dto.getTargetStatus()));

        if (ticket.getAssigneeId() != null && !ticket.getAssigneeId().equals(operator.getId())) {
            sendStatusNotification(ticket.getAssigneeId(), ticket.getTicketNo(),
                    "工单 " + ticket.getTicketNo() + " 状态已变更为: " + getStatusName(dto.getTargetStatus()));
        }
    }

    @Override
    public String generateTicketNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "TK-" + date + "-" + random;
    }

    private void sendStatusNotification(Long userId, String ticketNo, String content) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle("工单状态变更 - " + ticketNo);
            notification.setContent(content);
            notification.setIsRead(0);
            notification.setRetryCount(0);
            notification.setMaxRetry(3);
            notificationMapper.insert(notification);

            Map<String, Object> msg = new HashMap<>();
            msg.put("notificationId", notification.getId());
            msg.put("userId", userId);
            msg.put("ticketNo", ticketNo);
            msg.put("content", content);

            rabbitTemplate.convertAndSend(RabbitMQConfig.TICKET_EXCHANGE,
                    RabbitMQConfig.TICKET_STATUS_ROUTING_KEY, msg);
        } catch (Exception e) {
            log.error("发送通知失败: ticketNo={}, error={}", ticketNo, e.getMessage());
        }
    }

    private String getStatusOperation(Integer status) {
        switch (status) {
            case 1: return "退回待处理";
            case 2: return "开始处理";
            case 3: return "提交验收";
            case 4: return "验收通过";
            case 5: return "驳回";
            default: return "状态变更";
        }
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 1: return "待处理";
            case 2: return "处理中";
            case 3: return "待验收";
            case 4: return "已完成";
            case 5: return "已驳回";
            default: return "未知";
        }
    }
}