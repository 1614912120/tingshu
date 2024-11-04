package com.atguigu.tingshu.account.receiver;


import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

@Component
public class AccountReceiver {
    @Autowired
    private UserAccountService userAccountService;

    @KafkaListener(topics = KafkaConstant.QUEUE_USER_REGISTER)
    public void initUserAccount(ConsumerRecord<String,String> record) {
        String  userId = record.value();
        if(StringUtils.isBlank(userId)) {
            return;
        }

        userAccountService.saveUserAccount(Long.valueOf(userId));
    }
}
