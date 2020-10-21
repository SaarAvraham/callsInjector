package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
public class Controller {
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private CallsInjectorService callsInjectorService;

    static int i=0;
    @CrossOrigin
    @PostMapping("/start")
    public void start(@RequestBody StartRequest startRequest) throws IOException {
        System.out.println();
        callsInjectorService.start(startRequest);
    }

    @CrossOrigin
    @DeleteMapping("/stop")
    public void stop() throws IOException {
        System.out.println();
        callsInjectorService.stop();
    }

    @GetMapping("/calls1")
    public void call2s() throws Exception {
        System.out.println();
        messagingTemplate.convertAndSend("/topic/messages", new Message(i*2, 5,11));
        i++;
    }

    @SubscribeMapping("/messages")
    public Message chatInit() {
       return callsInjectorService.getStatusMessage();
//        return new Message(50, 5,11);
    }
}
