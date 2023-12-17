package com.example;

import java.io.IOException;
import java.sql.SQLException;

import com.rabbitmq.client.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Consumer {
    private final static int GIVENNUMBER = 1000;
    private final static String QUEUE_NAME = "MessageQueue";
    private static DatabaseService dbService = new DatabaseService();
    private static final Logger logger = LogManager.getLogger(Consumer.class);
    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
    
        Runnable runnable = new Runnable() {
            @Override
            public void run() {                 
                try {
                    final Channel channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                    // max one message per receiver
                    channel.basicQos(1);
                    System.out.println(" [*] Thread waiting for RPC messages. To exit press CTRL+C");

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                                .Builder()
                                .correlationId(delivery.getProperties().getCorrelationId())
                                .build();
                        String response = "";

                        try {
                            String message = new String(delivery.getBody(), "UTF-8");
                            System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
                            String albumID = message.split("::")[0];
                            String likeorDislike = message.split("::")[1];
                            Consumer.dbService.postReview(albumID, likeorDislike);
                        } catch (SQLException e) {
                            response = "Album not found";
                        }finally {
                            channel.basicPublish( "", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        }
                    };
                    // process messages
                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
                    } catch (IOException ex) {
                        logger.error("ERROR FROM CONSUMER------------------------------------------------"+ex);
                }
            }
        };
        // start threads and block to receive messages
        for(int i=0; i<GIVENNUMBER ; i++){
            Thread recv = new Thread(runnable);
            recv.start();
        }
    }
}