package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Date;

@AllArgsConstructor
@Getter
public class StartRequest {
    private int callsToInject;
    private DateRange dateRange;
    private boolean isTurboMode;
}
