package com.atguigu.tingshu.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, Object data) {
        this.sendMessage(topic, null, data);
    }
    public void sendMessage(String topic,String key,Object data) {
        CompletableFuture completableFuture = kafkaTemplate.send(topic, key, data);
        completableFuture.thenAcceptAsync(result->{
            logger.info("发送消息成功{}",result);
        }).exceptionally(e->{
            logger.error("发送消息失败{}",e);
            return null;
        });
    }

}
