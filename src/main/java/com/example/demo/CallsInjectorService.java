package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CallsInjectorService {
    private AtomicInteger callsInjected = new AtomicInteger(0);
    private AtomicInteger callsPerSecond = new AtomicInteger(0);
    private int totalCallsToInject;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> injector;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    public void start(StartRequest startRequest) {
        this.totalCallsToInject = startRequest.getCallsToInject();

        injector = executor.submit(() -> {
            Instant start = Instant.now();

            for (int i = 0; i < 50; i++) {
                try {
                    callsInjected.incrementAndGet();
                    callsInjected.incrementAndGet();
                    callsInjected.incrementAndGet();
                    callsInjected.incrementAndGet();
                    callsInjected.incrementAndGet();
                    callsInjected.incrementAndGet();
                    callsInjected.incrementAndGet();
                    callsInjected.incrementAndGet();
                    callsInjected.incrementAndGet();
                    long millisSinceStart = Duration.between(start, Instant.now()).toMillis();
                    long secondsSinceStart = millisSinceStart == 0 ? 0 : millisSinceStart / 1000;
                    int callsPerSecondInt = secondsSinceStart == 0 ? 0 : (int) (callsInjected.get() / secondsSinceStart);
                    callsPerSecond.set(callsPerSecondInt);
                    messagingTemplate.convertAndSend("/topic/messages", getStatusMessage());
                    Thread.sleep(5000);
                    System.out.println("sleeping");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void stop() {
        injector.cancel(true);
        callsInjected.set(0);
        callsPerSecond.set(0);
    }

    public Message getStatusMessage() {
        int callsAlreadyInjectedInt = callsInjected.get();
        int remainingAmountOfCallsToInjectInt = totalCallsToInject - callsAlreadyInjectedInt;
        int progress = totalCallsToInject != 0 ? callsAlreadyInjectedInt*100 / totalCallsToInject : 0;
        int callsPerSecondInt = callsPerSecond.get();
        int remainingSeconds = callsPerSecondInt != 0 ? remainingAmountOfCallsToInjectInt / callsPerSecondInt : 0;

        return new Message(progress, callsInjected.get(), remainingSeconds);
    }
}
