package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class Controller {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private CallsInjectorService callsInjectorService;

    public static AtomicInteger loggedInUsers = new AtomicInteger(0);

    @CrossOrigin
    @PostMapping("/start")
    public void start(@RequestBody StartRequest startRequest) {
        callsInjectorService.start(startRequest);
    }

    @EventListener(SessionConnectEvent.class)
    public void handleWebSocketConnectionOpened(SessionConnectEvent event) {
        System.out.println("Received a new WebSocket connection");
        loggedInUsers.incrementAndGet();
    }

    @EventListener(SessionDisconnectEvent.class)
    public void handleWebSocketConnectionClosed(SessionDisconnectEvent event) {
        System.out.println("A WebSocket connection closed");
        loggedInUsers.decrementAndGet();
    }

    @CrossOrigin
    @DeleteMapping("/stop")
    public void stop() throws IOException {
        System.out.println();
        callsInjectorService.stop();
    }

    @SubscribeMapping("/messages")
    public Message chatInit() {
       return callsInjectorService.getStatusMessage();

//        return new Message(50, 5,11);
    }
}
