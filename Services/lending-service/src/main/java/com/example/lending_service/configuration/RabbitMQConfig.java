package com.example.lending_service.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "lending.exchange";

    public static final String BOOK_LENT_QUEUE = "book.lent.queue";
    public static final String BOOK_RETURNED_QUEUE = "book.returned.queue";

    public static final String ROUTING_BOOK_LENT = "lending.book-lent";
    public static final String ROUTING_BOOK_RETURNED = "lending.book-returned";

    @Bean
    public TopicExchange lendingExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue bookLentQueue() {
        return new Queue(BOOK_LENT_QUEUE);
    }

    @Bean
    public Queue bookReturnedQueue() {
        return new Queue(BOOK_RETURNED_QUEUE);
    }

    @Bean
    public Binding bindingBookLent(Queue bookLentQueue, TopicExchange lendingExchange) {
        return BindingBuilder.bind(bookLentQueue).to(lendingExchange).with(ROUTING_BOOK_LENT);
    }

    @Bean
    public Binding bindingBookReturned(Queue bookReturnedQueue, TopicExchange lendingExchange) {
        return BindingBuilder.bind(bookReturnedQueue).to(lendingExchange).with(ROUTING_BOOK_RETURNED);
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
