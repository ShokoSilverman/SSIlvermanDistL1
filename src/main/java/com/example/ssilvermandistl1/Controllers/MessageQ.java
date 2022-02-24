package com.example.ssilvermandistl1.Controllers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequestMapping(path="/messageQ")
public class MessageQ {

    String hostname = InetAddress.getLocalHost().toString().split("/")[1];

    public MessageQ() throws UnknownHostException {
    }

    @PostMapping(path = "/testOut")
    public String testRabQ(@RequestParam String message, @RequestParam String email){
        System.out.println(hostname);
        try{
            String strInfoOut = String.format("%s||%s",email,message);
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostname);
            factory.setPort(9001); //used to change port
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare("SSilverman_Queue", false, false, false, null);
            channel.basicPublish("", "SSilverman_Queue", null, strInfoOut.getBytes());
            channel.close();
            connection.close();
        }catch (Exception e){
            System.err.println("FUck");
            e.printStackTrace();
            return "failed";
        }
        return "done";
    }



}
