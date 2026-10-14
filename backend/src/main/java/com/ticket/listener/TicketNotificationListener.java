package com.ticket.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ticket.config.RabbitMQConfig;
import com.ticket.entity.Notification;
import com.ticket.mapper.NotificationMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class TicketNotificationListener {

    @Resource
    private NotificationMapper notificationMapper;

    @RabbitListener(queues = RabbitMQConfig.TICKET_STATUS_QUEUE)
    public void handleStatusChange(Map<String, Object> msg, Channel channel, Message message) {
        Long notificationId = Long.valueOf(msg.get("notificationId").toString());
        String ticketNo = (String) msg.get("ticketNo");

        try {
            log.info("处理工单状态变更通知: ticketNo={}, notificationId={}", ticketNo, notificationId);
            Thread.sleep(100);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("通知处理失败: notificationId={}, error={}", notificationId, e.getMessage());
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.TICKET_DEAD_QUEUE)
    public void handleDeadMessage(Map<String, Object> msg, Channel channel, Message message) {
        Long notificationId = Long.valueOf(msg.get("notificationId").toString());
        String ticketNo = (String) msg.get("ticketNo");

        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException ignored) {}
            return;
        }

        int retryCount = notification.getRetryCount() != null ? notification.getRetryCount() : 0;
        int maxRetry = notification.getMaxRetry() != null ? notification.getMaxRetry() : 3;

        if (retryCount >= maxRetry) {
            log.warn("通知已达最大重试次数，丢弃: notificationId={}, ticketNo={}", notificationId, ticketNo);
            notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                    .eq(Notification::getId, notificationId)
                    .set(Notification::getRetryCount, retryCount + 1));
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException ignored) {}
            return;
        }

        try {
            log.info("死信队列重试通知: notificationId={}, retry={}/{}", notificationId, retryCount + 1, maxRetry);
            Thread.sleep(200);

            notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                    .eq(Notification::getId, notificationId)
                    .set(Notification::getRetryCount, retryCount + 1));

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("死信队列重试仍失败: notificationId={}, error={}", notificationId, e.getMessage());
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException ex) {
                log.error("拒绝死信消息失败", ex);
            }
        }
    }
}