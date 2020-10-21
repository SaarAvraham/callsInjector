package com.example.demo;


public class Message {
    private Integer injectionProgress;
    private Integer callsInjected;
    private Integer remainingSeconds;

    public Message(Integer injectionProgress, Integer callsInjected, Integer remainingSeconds) {
        this.injectionProgress = injectionProgress;
        this.callsInjected = callsInjected;
        this.remainingSeconds = remainingSeconds;
    }

    public Integer getInjectionProgress() {
        return injectionProgress;
    }

    public void setInjectionProgress(Integer injectionProgress) {
        this.injectionProgress = injectionProgress;
    }

    public Integer getCallsInjected() {
        return callsInjected;
    }

    public void setCallsInjected(Integer callsInjected) {
        this.callsInjected = callsInjected;
    }

    public Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Integer remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
