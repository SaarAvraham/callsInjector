package com.example.demo;

import com.example.demo.data.Message;
import com.example.demo.data.StartRequest;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CallsInjectorService {
    // State
    private final static int TURBO_MODE_INJECTORS_COUNT = 5;
    private AtomicInteger callsInjected = new AtomicInteger(0);
    private AtomicInteger callsPerSecond = new AtomicInteger(0);
    private int totalCallsToInject;
    private boolean isTurboMode = false;
    private LocalDateTime queryableInEgressAfterDate;
    private Instant startInstant;
    private TimerTask repeatedTask;

    // Executors
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private Future<?> injector = null;

    // 3rd party
    private final SimpMessageSendingOperations messagingTemplate;
    private final Connection rabbitConnection;
    private StartRequest startRequest;
    private int callsPerDay = 0;

    @Autowired
    public CallsInjectorService(SimpMessageSendingOperations messagingTemplate, Connection rabbitConnection) {
        this.messagingTemplate = messagingTemplate;
        this.rabbitConnection = rabbitConnection;
    }


    private int initState(StartRequest startRequest) throws IOException {
        this.startRequest = startRequest;
        queryableInEgressAfterDate = LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(startRequest.getCallsToInject() / 500);
//        injector = new Injector();
        startInstant = Instant.now();
        this.callsPerDay = calculateCallsPerDay(startRequest);
        this.totalCallsToInject = callsPerDay * (int) ChronoUnit.DAYS.between(startRequest.getDateRange().getStartDate(), startRequest.getDateRange().getEndDate());
        int injectorsCount = startRequest.isTurboMode() ? TURBO_MODE_INJECTORS_COUNT : 1;

        return injectorsCount;
    }

    public void stop() {
        if (injector != null) {
            injector.cancel(true);
        }

        if (repeatedTask != null) {
            repeatedTask.cancel();
        }

        injector = null;
        callsInjected.set(0);
        callsPerSecond.set(0);
        totalCallsToInject = 0;
        messagingTemplate.convertAndSend("/topic/messages", getStatusMessage());
    }

    public Message getStatusMessage() {
        int callsAlreadyInjectedInt = callsInjected.get();
        int remainingAmountOfCallsToInjectInt = totalCallsToInject - callsAlreadyInjectedInt;
        int progress = totalCallsToInject != 0 ? callsAlreadyInjectedInt * 100 / totalCallsToInject : 0;
        int callsPerSecondInt = callsPerSecond.get();
        int remainingSeconds = callsPerSecondInt != 0 ? remainingAmountOfCallsToInjectInt / callsPerSecondInt : 0;

        return new Message(totalCallsToInject, progress, callsInjected.get(), remainingSeconds, queryableInEgressAfterDate, callsPerSecondInt, injector != null, isTurboMode);
    }

    private void updateStats(Instant start) {
        long millisSinceStart = Duration.between(start, Instant.now()).toMillis();
        long secondsSinceStart = millisSinceStart == 0 ? 0 : millisSinceStart / 1000;
        int callsPerSecondInt = secondsSinceStart == 0 ? 0 : (int) (callsInjected.get() / secondsSinceStart);
        callsPerSecond.set(callsPerSecondInt);

        messagingTemplate.convertAndSend("/topic/messages", getStatusMessage());
    }

    public void start(StartRequest startRequest) throws IOException, TimeoutException {
        if (injector == null) { // no injection is in progress
            int injectorsCount = initState(startRequest);

            TimerTask repeatedTask = new TimerTask() {
                public void run() {
                    updateStats(startInstant);
                }
            };
            Timer timer = new Timer("Timer");
            long period = 500L * 1L;
            timer.scheduleAtFixedRate(repeatedTask, 0L, period);

            Future<?> injector = executor.submit(new Injector());

        } else {
            throw new RuntimeException("Injection is already in progress! bug?");
        }
    }

    public int calculateCallsPerDay(StartRequest startRequest) {
        int days = (int) ChronoUnit.DAYS.between(startRequest.getDateRange().getStartDate(), startRequest.getDateRange().getEndDate());

        return days != 0 ? (int) Math.ceil(startRequest.getCallsToInject() / (double) days) : 0;
    }


    public class Injector implements Runnable {
        private List<Future<?>> injectorWorkers;
        private ExecutorService workersExecutor;
        private BlockingQueue<LocalDateTime> blockingQueue = new ArrayBlockingQueue<LocalDateTime>(6000);

        public Injector() throws IOException {
            injectorWorkers = new ArrayList<>();
            int workersCount = startRequest.isTurboMode() ? TURBO_MODE_INJECTORS_COUNT : 1;
            workersExecutor = Executors.newFixedThreadPool(workersCount);

            for (int i = 0; i < workersCount; i++) {
                workersExecutor.submit(new InjectorWorker(rabbitConnection));
            }

//            messagingTemplate.convertAndSend("/topic/errors", e.getMessage());
//            stop();
        }

        // Injector
        @SneakyThrows
        public void run() {
            LocalDateTime startDate = startRequest.getDateRange().getStartDate();
            LocalDateTime endDate = startRequest.getDateRange().getEndDate();

            System.out.println("Going to inject " + callsPerDay + " calls per day");

            for (LocalDateTime currDate = LocalDateTime.from(startDate); currDate.isBefore(endDate); currDate = currDate.plusDays(1)) {
                for (int i = 0; i < callsPerDay; i++) {

                    blockingQueue.put(currDate);
//                    JSONObject segmentJson = createSegmentEnd(currDate);
//                    JSONObject contactJson = createContactEnd(segmentJson);
//
//                    channel.basicPublish("ic-call-data", "segment-end." + segmentJson.get("segmentId") + "." + segmentJson.get("segmentId"), MessageProperties.PERSISTENT_TEXT_PLAIN, segmentJson.toString().getBytes());
//                    channel.basicPublish("ic-call-data", "contact-end." + segmentJson.get("segmentId"), MessageProperties.PERSISTENT_TEXT_PLAIN, createContactEnd(segmentJson).toString().getBytes());
//
//                    callsInjected.incrementAndGet();
                }

                System.out.println("DONE - " + currDate);
            }

//            JSONObject segmentJson = createSegmentEnd();
//            JSONObject contactJson = createContactEnd(segmentJson);

        }

        private JSONObject createSegmentEnd(LocalDateTime currDate) {
            return new JSONObject(createSegment3(1, 1));
        }

        private String createSegment3(int segmentId, int contactId) { // phone number - i%50000, agentId - i%50000
            return "{\"version\":1.0,\"tenantId\":1,\"switchId\":1,\"contactId\":" + contactId + ",\"contactStartTime\":\"2020-05-13 09:01:08.734\",\"contactEndTime\":\"2020-05-13 09:01:29.956\",\"segmentId\":" + segmentId + ",\"segmentStartTime\":\"2020-05-13 09:01:08.734\",\"segmentEndTime\":\"2020-05-13 09:01:29.956\",\"callDirection\":\"Internal\",\"DNIS\":\"2408\",\"pbxCallId\":null,\"pbxUniqueCallId\":null,\"participants\":[{\"userId\":-1,\"ctiUserIdentifier\":null,\"agentId\":null,\"phoneNumber\":\"2307\",\"uniqueDeviceId\":\"\",\"userGroupIds\":[],\"participantType\":\"Internal\",\"isInitiator\":false,\"recordings\":[1]},{\"userId\":-1,\"ctiUserIdentifier\":null,\"agentId\":null,\"phoneNumber\":\"2408\",\"uniqueDeviceId\":\"SEP000000002408\",\"userGroupIds\":[],\"participantType\":\"Internal\",\"isInitiator\":false,\"recordings\":[1]}],\"recordings\":[{\"recordingId\":1,\"mediaType\":\"Voice\",\"direction\":\"Summed\",\"sessionId\":0,\"recorderId\":100,\"startTime\":\"0001-01-01 00:00:00.000\",\"endTime\":\"0001-01-01 00:00:00.000\",\"recordingStatus\":\"Failed\",\"recordingPolicyId\":1}],\"recordingStatus\":{\"Voice\":\"Failed\"},\"exceptions\":[{\"exceptionType\":\"VoiceRecordingFailed\",\"exceptionDetails\":\"RCM.RCM.NoMediaCaptured, ComplexCode = M7C6 RCM, Description = 'No audio / video stream or screen packets on channel', Resolution = 'Check status of Logger/Recorder'\",\"exceptionTime\":\"2020-05-13 09:01:30.044\",\"recordingId\":1}]}";
        }

        private JSONObject createContactEnd(JSONObject segmentJson) throws JSONException {
            JSONObject contact = new JSONObject();
            contact.put("ContactId", segmentJson.get("contactId"));
            contact.put("ContactStart", segmentJson.get("contactStartTime"));
            contact.put("ContactEnd", segmentJson.get("contactEndTime"));
            contact.put("SegmentIDs", Collections.singletonList(segmentJson.get("segmentId").toString()));
            contact.put("State", "Closed");

            return contact;
        }

        public void cancel() {
            injectorWorkers.forEach(worker -> worker.cancel(true));
        }

        public class InjectorWorker implements Runnable {
            private Channel channel;

            public InjectorWorker(Connection connection) throws IOException {
                channel = rabbitConnection.createChannel();
            }

            @SneakyThrows
            public void run() {
                while (true) {
                    LocalDateTime currDate = blockingQueue.take();

                    JSONObject segmentJson = createSegmentEnd(currDate);
                    JSONObject contactJson = createContactEnd(segmentJson);

                    channel.basicPublish("ic-call-data", "segment-end." + segmentJson.get("segmentId") + "." + segmentJson.get("segmentId"), MessageProperties.PERSISTENT_TEXT_PLAIN, segmentJson.toString().getBytes());
                    channel.basicPublish("ic-call-data", "contact-end." + segmentJson.get("segmentId"), MessageProperties.PERSISTENT_TEXT_PLAIN, createContactEnd(segmentJson).toString().getBytes());
                }
            }
        }
    }
}

