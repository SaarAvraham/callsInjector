package com.example.demo.data;

import com.example.demo.DateRange;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonInclude
public class StartRequest {
    private int callsToInject;
    private DateRange dateRange;
    private boolean isTurboMode;
}
