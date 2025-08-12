package com.example.reporting_service.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${reporting.queue}")
    private String queueName;

    @Value("${reporting.exchange}")
    private String exchange;

    @Value("${reporting.routingKey}")
    private String routingKey;

    @Bean
    public Queue queue() {
        return new Queue(queueName);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}