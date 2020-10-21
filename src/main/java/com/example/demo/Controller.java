package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
public class Controller {
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    static int i=0;
    @CrossOrigin
    @GetMapping("/start")
    public void start() throws IOException {
        System.out.println();
    }

    @CrossOrigin
    @GetMapping("/stop")
    public void stop() throws IOException {
        System.out.println();
    }

    @GetMapping("/calls1")
    public void call2s() throws Exception {
        System.out.println();
        messagingTemplate.convertAndSend("/topic/messages", new Message(i*2, 5,11));
        i++;
    }

    @SubscribeMapping("/messages")
    public Message chatInit() {
        return new Message(50, 5,11);
    }
}
