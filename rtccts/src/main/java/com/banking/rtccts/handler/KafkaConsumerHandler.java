package com.banking.rtccts.handler;

import com.banking.rtccts.config.KafkaConsumerConfig;
import com.banking.rtccts.service.KafkaConsumerService;
import com.rtccts.Transaction;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerHandler implements MessageHandler {

    Logger logger = LoggerFactory.getLogger(KafkaConsumerHandler.class);

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @Autowired
    private PrometheusMeterRegistry meterRegistry;

    private final Timer messageHandlingTimer = meterRegistry.timer("kafka.consumer.message.handling");


    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            ConsumerRecord<String, Transaction> consumerRecord = (ConsumerRecord<String, Transaction>) message.getPayload();
            logger.info("message payload:{}", consumerRecord.value());
            kafkaConsumerService.processRecord(consumerRecord.value());
        } catch (Exception e) {
            logger.error("Message while processing the transaction {}", e.getMessage());
        } finally {
            sample.stop(messageHandlingTimer);
        }
    }
}
