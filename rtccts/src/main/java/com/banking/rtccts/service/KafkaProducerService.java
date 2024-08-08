package com.banking.rtccts.service;

import com.banking.rtccts.config.KafkaConsumerConfig;
import com.rtccts.Transaction;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Value("${kafka.topics.main-topic}")
    private String topicName;

    @Autowired
    KafkaTemplate<String, Transaction> kafkaTemplate;

    @Autowired
    private PrometheusMeterRegistry meterRegistry;

    private final Timer messageHandlingTimer = meterRegistry.timer("kafka.producer.message.handling");

    private Random random = new Random();

    @Scheduled(fixedRate = 5000)
    public void sendMessage() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try{
            Transaction cardTransaction = getRandomMessage();
            this.kafkaTemplate.send(topicName, cardTransaction);
            logger.info("Message published successfully to kafka topic: {}", cardTransaction);
        }catch(Exception e){
            logger.error("Message publish failed");
            throw new RuntimeException("Failed to publish message: " + e.getMessage(), e);
        }finally {
            sample.stop(messageHandlingTimer);
        }

    }

    public Transaction getRandomMessage() {
        return Transaction.newBuilder()
                .setTransactionId(random.nextInt(100))
                .setTimestamp(System.currentTimeMillis())
                .setMerchantId("abc")
                .setCardNumber(random.nextInt(100000000))
                .setAmount(random.nextLong())
                .build();

    }
}
