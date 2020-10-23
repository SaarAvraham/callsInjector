package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Message {
    private Integer totalCallsToInject;
    private Integer injectionProgress;
    private Integer callsInjected;
    private Integer remainingSeconds;
    private Integer callsPerSecond;
    private boolean isRunning;
}
