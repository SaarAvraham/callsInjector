package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DemoApplication {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() throws InterruptedException {
        ScheduledFuture<?> countdown = scheduler.schedule(() -> {
            try {
                if (Controller.loggedInUsers.get() == 0){
					Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start chrome http://localhost:9090/"});
					System.out.println("Launched chrome at http://localhost:9090/");
				}
                else{
					System.out.println("Didn't launch chrome at http://localhost:9090/, because there's already an open tab");
				}
            } catch (IOException e) {
                System.out.println("Couldn't open chrome. please open a browser at http://localhost:9090/");
            }
        }, 3, TimeUnit.SECONDS);

        scheduler.shutdown();
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
