package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    private final static int TURBO_MODE_INJECTORS_COUNT = 5;

    private ExecutorService executor = Executors.newFixedThreadPool(TURBO_MODE_INJECTORS_COUNT);
    private List<Future<?>> injectors;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    public void start(StartRequest startRequest) {
        if (injectors == null) { // no injection is in progress
            injectors = new ArrayList<>();
            this.totalCallsToInject = startRequest.getCallsToInject();
            int injectorsCount = startRequest.isTurboMode() ? TURBO_MODE_INJECTORS_COUNT : 1;
            injectors = new ArrayList<>();

            for (int i = 0; i < injectorsCount; i++) {
                Future<?> injector = executor.submit(() -> {
                    Instant start = Instant.now();

                    for (int j = 0; j < 5000; j++) {
                        try {
                            callsInjected.incrementAndGet();
                            callsInjected.incrementAndGet();
                            long millisSinceStart = Duration.between(start, Instant.now()).toMillis();
                            long secondsSinceStart = millisSinceStart == 0 ? 0 : millisSinceStart / 1000;
                            int callsPerSecondInt = secondsSinceStart == 0 ? 0 : (int) (callsInjected.get() / secondsSinceStart);
                            callsPerSecond.set(callsPerSecondInt);
                            messagingTemplate.convertAndSend("/topic/messages", getStatusMessage());
                            Thread.sleep(100);
                            System.out.println("sleeping");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            int x = 5 / 0;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

                injectors.add(injector);
            }
        } else {
            throw new RuntimeException("Injection is already in progress! bug?");
        }
    }

    public void stop() {
        injectors.forEach(injector -> injector.cancel(true));
        injectors = null;
        callsInjected.set(0);
        callsPerSecond.set(0);
        messagingTemplate.convertAndSend("/topic/messages", getStatusMessage());
    }

    public Message getStatusMessage() {
        int callsAlreadyInjectedInt = callsInjected.get();
        int remainingAmountOfCallsToInjectInt = totalCallsToInject - callsAlreadyInjectedInt;
        int progress = totalCallsToInject != 0 ? callsAlreadyInjectedInt * 100 / totalCallsToInject : 0;
        int callsPerSecondInt = callsPerSecond.get();
        int remainingSeconds = callsPerSecondInt != 0 ? remainingAmountOfCallsToInjectInt / callsPerSecondInt : 0;

        return new Message(totalCallsToInject,progress, callsInjected.get(), remainingSeconds, callsPerSecondInt, injectors != null);
    }
}
