package com.example;

import java.io.IOException;
import java.sql.SQLException;

import com.rabbitmq.client.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {
    private final static String QUEUE_NAME = "MessageQueue";
    private static DatabaseService dbService = new DatabaseService();
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
                            System.out.println("albumID: " + albumID);
                            System.out.println("likeorDislike: " + likeorDislike);
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
                        Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        // start threads and block to receive messages
        Thread recv1 = new Thread(runnable);
        Thread recv2 = new Thread(runnable);
        recv1.start();
        recv2.start();
    }
}