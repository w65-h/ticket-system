package com.ticket.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String TICKET_EXCHANGE = "ticket.exchange";
    public static final String TICKET_STATUS_QUEUE = "ticket.status.queue";
    public static final String TICKET_STATUS_ROUTING_KEY = "ticket.status.change";

    public static final String TICKET_DEAD_EXCHANGE = "ticket.dead.exchange";
    public static final String TICKET_DEAD_QUEUE = "ticket.dead.queue";
    public static final String TICKET_DEAD_ROUTING_KEY = "ticket.dead";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public DirectExchange ticketExchange() {
        return new DirectExchange(TICKET_EXCHANGE);
    }

    @Bean
    public DirectExchange ticketDeadExchange() {
        return new DirectExchange(TICKET_DEAD_EXCHANGE);
    }

    @Bean
    public Queue ticketStatusQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", TICKET_DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", TICKET_DEAD_ROUTING_KEY);
        return QueueBuilder.durable(TICKET_STATUS_QUEUE).withArguments(args).build();
    }

    @Bean
    public Queue ticketDeadQueue() {
        return QueueBuilder.durable(TICKET_DEAD_QUEUE).build();
    }

    @Bean
    public Binding ticketStatusBinding() {
        return BindingBuilder.bind(ticketStatusQueue()).to(ticketExchange()).with(TICKET_STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding ticketDeadBinding() {
        return BindingBuilder.bind(ticketDeadQueue()).to(ticketDeadExchange()).with(TICKET_DEAD_ROUTING_KEY);
    }
}