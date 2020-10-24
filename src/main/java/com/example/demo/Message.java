package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class Message {
    private Integer totalCallsToInject;
    private Integer injectionProgress;
    private Integer callsInjected;
    private Integer remainingSeconds;
    private LocalDateTime queryableInEgressAfterDate;
    private Integer callsPerSecond;
    private boolean isRunning;
    private boolean isTurboMode;
}
