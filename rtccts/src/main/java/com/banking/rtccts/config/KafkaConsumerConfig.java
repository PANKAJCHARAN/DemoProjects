package com.banking.rtccts.config;

import com.banking.rtccts.handler.KafkaConsumerHandler;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaConsumerConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${kafka.topics.main-topic}")
    private String topicName;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServer;

    @Autowired
    private KafkaConsumerHandler kafkaConsumerHandler;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    KafkaMessageDrivenChannelAdapter<String, String> kafkaMessageDrivenChannelAdapter() {
        KafkaMessageDrivenChannelAdapter<String, String> adapter = new KafkaMessageDrivenChannelAdapter<>(
                kafkaListenerContainer(), KafkaMessageDrivenChannelAdapter.ListenerMode.record);
        adapter.setOutputChannel(kafkaInputChannel());
        adapter.setErrorChannel(errorChannel());
        return adapter;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> kafkaListenerContainer() {
        return new ConcurrentMessageListenerContainer<>(consumerFactory(), containerProperties());
    }

    private ContainerProperties containerProperties() {
        ContainerProperties properties = new ContainerProperties(topicName);
        return properties;
    }

    @Bean
    public MessageChannel kafkaInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel errorChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow kafkaListenerFlow() {
        return IntegrationFlow
                .from(kafkaMessageDrivenChannelAdapter())
                .handle(kafkaConsumerHandler)
                .get();
    }

    @Bean
    public IntegrationFlow kafkaListenerErrorFlow() {
        return IntegrationFlow
                .from(kafkaMessageDrivenChannelAdapter())
                .handle(errorHandler())
                .get();
    }

    @Bean
    public MessageHandler errorHandler() {
        return message -> {
            logger.error("Error message{}", message.getPayload());
        };
    }
}
