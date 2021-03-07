package org.apache.rocketmq.example.test;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * @author AntzUhl
 * @Date 2020/12/15 10:38
 * @Description
 */
public class ProducerTest {
    public static void main(String[] args) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        DefaultMQProducer producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr("127.0.0.1:9876");
        producer.start();
        Message message = new Message("topic_order", "TagA", "keyA", "hello".getBytes());
        SendResult result = producer.send(message);
        System.out.printf("result %s\r\n", result);
        producer.shutdown();;
    }
}
