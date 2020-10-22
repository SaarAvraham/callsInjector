package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StartRequest {
    private int callsToInject;
    private int rangeFromMonth;
    private int rangeToMonth;
    private int year;
    private boolean isTurboMode;
}
