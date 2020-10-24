package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Getter
public class DateRange {
    Instant startDate;
    Instant endDate;
}
