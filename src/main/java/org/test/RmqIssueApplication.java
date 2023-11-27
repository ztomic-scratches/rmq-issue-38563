package org.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class RmqIssueApplication {

	@RabbitListener(
			id = "${rabbitmq.exchange}-rabbit-listener",
			containerFactory = "rabbitListenerContainerFactory",
			bindings = {
					@QueueBinding(
							value = @Queue(name = "${rabbitmq.queue}"),
							exchange = @Exchange("${rabbitmq.exchange}"),
							key = "${rabbitmq.routing-key}")
			})
	@Component
	static class Listener {

		private static final Logger log = LoggerFactory.getLogger(Listener.class);

		@RabbitHandler
		public void receiveMessage(String message) {
			log.info("Received '{}' -> thread: {}, virtual: {}", message, Thread.currentThread().getName(), Thread.currentThread().isVirtual());
		}
		
	}

	@Component
	static class Runner {

		private static final Logger log = LoggerFactory.getLogger(Runner.class);

		private final RabbitTemplate rabbitTemplate;
		private final Environment environment;

		public Runner(RabbitTemplate rabbitTemplate, Environment environment) {
			this.rabbitTemplate = rabbitTemplate;
			this.environment = environment;
		}

		@EventListener(ApplicationStartedEvent.class)
		public void applicationStarted() {
			log.info("Sending message...");

			rabbitTemplate.convertAndSend(environment.getRequiredProperty("rabbitmq.exchange"), environment.getRequiredProperty("rabbitmq.routing-key"), "Hello from RabbitMQ!");
			
			log.info("Message sent.");
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(RmqIssueApplication.class, args);
	}

}
