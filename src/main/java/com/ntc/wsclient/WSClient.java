/*
 * Copyright 2017 nghiatc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ntc.wsclient;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author nghiatc
 * @since Nov 14, 2017
 * 
 * https://github.com/TooTallNate/Java-WebSocket
 */
public class WSClient extends WebSocketClient {
    public static String uri = "";
    public static Draft protocolDraft = new Draft_6455();
    public static Map<String, String> mapHeader = new HashMap<>();
    public static int connectTimeout = 30;
    public static WSClient instance = null;
    private static Lock lock = new ReentrantLock();
    private long timeWaitMilli = 5000;
    public AtomicBoolean isReconnect = new AtomicBoolean(false);
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String uri = "wss://localhost:15051";
            Map<String, String> mapHeader = new HashMap<>();
            int connectTimeout = 30;
            mapHeader.put("abc", "123");
            mapHeader.put("address", "address123");
            WSClient client = WSClient.getInstance(uri, mapHeader, connectTimeout);
            

//            URI serverUri = new URI("ws://localhost:15051");
//            WSClient client = new WSClient(serverUri);
//            client.connect();
            Thread.sleep(1000);

            for (int i = 0; i < 10; i++) {
                String msg = "Message no " + i;
                client.sendMsg(msg);
                System.out.println("WSClient send: " + msg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static WSClient getInstance(String uri){
        if (instance == null){
            lock.lock();
            try {
                if (instance == null){
                    WSClient.uri = uri;
                    URI serverUri = new URI(uri);
                    instance = new WSClient(serverUri, protocolDraft, mapHeader, connectTimeout);
                    instance.connect();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }
    
    public static WSClient getInstance(String uri, Map<String, String> mapHeader){
        if (instance == null){
            lock.lock();
            try {
                if (instance == null){
                    WSClient.uri = uri;
                    WSClient.mapHeader = mapHeader;
                    URI serverUri = new URI(uri);
                    instance = new WSClient(serverUri, protocolDraft, mapHeader, connectTimeout);
                    instance.connect();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }
    
    public static WSClient getInstance(String uri, Map<String, String> mapHeader, int connectTimeout){
        if (instance == null){
            lock.lock();
            try {
                if (instance == null){
                    WSClient.uri = uri;
                    WSClient.mapHeader = mapHeader;
                    WSClient.connectTimeout = connectTimeout;
                    URI serverUri = new URI(uri);
                    instance = new WSClient(serverUri, protocolDraft, mapHeader, connectTimeout);
                    instance.connect();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public WSClient(URI serverUri) {
        super(serverUri);
    }

    public WSClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public WSClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        System.out.println("WSClient new connection opened to: " + getURI());
    }

    @Override
    public void onMessage(String message) {
        System.out.println("WSClient received message: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WSClient closed with exit code " + code + " additional info: " + reason);
        instance = null;
        System.out.println("============================= RetryConnection =============================");
        WSClient client = WSClient.getInstance(uri, mapHeader, connectTimeout);
        
//        isReconnect.set(true);
////        RetryConnection rc = new RetryConnection();
////        while(rc.shouldRetry() && isReconnect.get()){
//            System.out.println("============================= RetryConnection =============================");
//            WSClient client = WSClient.getInstance(uri, mapHeader, connectTimeout);
//            if(client != null){
//                isReconnect.set(false);
////                break;
//            } else{
//                try {
////                    rc.reconnection();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
////        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WSClient an error occurred:" + ex);
    }
    
    private void waitRetry() {
        try {
            Thread.sleep(timeWaitMilli);
        } catch (InterruptedException ignored) {
        }
    }

    public void sendMsg(String msg) {
        if (msg != null && !msg.isEmpty()) {
            send(msg);
        }
    }

    static class RetryConnection {

        public static final int DEFAULT_RETRIES = 3;
        public static final int DEFAULT_TRIES_LEFT = 10000;
        public static final long DEFAULT_WAIT_TIME_IN_MILLI = 5000;

        private int numberOfRetries;
        private int numberOfTriesLeft;
        private long timeToWait;

        public RetryConnection() {
            this(DEFAULT_RETRIES, DEFAULT_TRIES_LEFT, DEFAULT_WAIT_TIME_IN_MILLI);
        }

        public RetryConnection(int numberOfRetries, long timeToWait) {
            this.numberOfRetries = numberOfRetries;
            this.numberOfTriesLeft = numberOfRetries;
            this.timeToWait = timeToWait;
        }

        public RetryConnection(int numberOfRetries, int numberOfTriesLeft, long timeToWait) {
            this.numberOfRetries = numberOfRetries;
            this.numberOfTriesLeft = numberOfTriesLeft;
            this.timeToWait = timeToWait;
        }

        /**
         * @return true if there are tries left
         */
        public boolean shouldRetry() {
            return numberOfTriesLeft > 0;
        }

        public void reconnection() throws Exception {
            numberOfTriesLeft--;
            if (!shouldRetry()) {
                throw new Exception("Retry Failed: Total " + numberOfRetries
                        + " attempts made at interval " + getTimeToWait()
                        + "ms");
            }
            waitUntilNextTry();
        }

        public long getTimeToWait() {
            return timeToWait;
        }

        private void waitUntilNextTry() {
            try {
                Thread.sleep(getTimeToWait());
            } catch (InterruptedException ignored) {
            }
        }
    }
}
