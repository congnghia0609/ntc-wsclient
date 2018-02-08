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

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
 * https://github.com/TooTallNate/Java-WebSocket/blob/master/src/main/example/SSLClientExample.java
 * 
 * keytool -genkey -validity 3650 -keystore "client-keystore.jks" -storepass "storetest123" \
 * -keypass "keytest123" -alias "default" -dname "CN=127.0.0.1, OU=UTS, O=UTS, L=NewYork, S=WEST, C=USA"
 */
public class WSSClient extends WebSocketClient {

    public static String uri = "";
    public static Draft protocolDraft = new Draft_6455();
    public static Map<String, String> mapHeader = new HashMap<>();
    public static int connectTimeout = 30;
    public static WSSClient instance = null;
    private static Lock lock = new ReentrantLock();
    private long timeWaitMilli = 5000;
    public AtomicBoolean isReconnect = new AtomicBoolean(false);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String uri = "wss://127.0.0.1:15051";
            Map<String, String> mapHeader = new HashMap<>();
            int connectTimeout = 30;
            mapHeader.put("abc", "123");
            mapHeader.put("address", "address123");
            WSSClient client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
            Thread.sleep(1000);

//            // 1. Test 1
//            Map<String, Object> mapData = new HashMap<>();
//            mapData.put("ws_topic", Common.WS_TOPIC_PRICE_MATCH);
//            mapData.put("ws_type", Common.WS_TYPE_ORDER);
//            for (int i = 0; i < 100; i++) {
//                mapData.put("order_id", i);
//                if (i%2 == 0){
//                    mapData.put("order_side", "buy");
//                } else{
//                    mapData.put("order_side", "sell");
//                }
//                mapData.put("order_price", randomRange(1, 1000));
//                mapData.put("order_size", randomRange(1, 100));
//                String msg = JsonUtils.Instance.toJson(mapData);
//                client.sendMsg(msg);
//                System.out.println("WSSClient send: " + msg);
//                
//                Thread.sleep(1000);
//            }
            
            // 2. Test 2
            String order_side = "buy";
            int order_price = 1;
            int order_size = 1;
            int order_size_cancel = 1;
            for (int i = 1; i <= 1000; i++) {
                if (i%2 == 0){
                    order_side = "buy";
                } else{
                    order_side = "sell";
                }
                order_price = 100; //randomRange(1, 1000);
                order_size = 10; //randomRange(1, 100);
                String msgBook = createJsonOrderBook(i, order_side, order_price, order_size);
                client.sendMsg(msgBook);
                System.out.println("WSSClient send[Order_Book]: " + msgBook);
                
//                if(i%5 == 0) {
//                    order_size_cancel = randomRange(1, 100);
//                    String msgCancel = createJsonOrderCancel(i-1, order_size_cancel);
//                    client.sendMsg(msgCancel);
//                    System.out.println("WSSClient send[Order_Cancel]: " + msgCancel);
//                }
                
                //Thread.sleep(1000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void testFullMatch(){
        try {
            String uri = "wss://127.0.0.1:15051";
            Map<String, String> mapHeader = new HashMap<>();
            int connectTimeout = 30;
            mapHeader.put("abc", "123");
            mapHeader.put("address", "address123");
            WSSClient client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
            Thread.sleep(1000);
            
            String order_side = "buy";
            int order_price = 1;
            int order_size = 1;
            for (int i = 1; i <= 1000; i++) {
                if (i%2 == 0){
                    order_side = "buy";
                } else{
                    order_side = "sell";
                }
                order_price = 100; //randomRange(1, 1000);
                order_size = 10; //randomRange(1, 100);
                String msgBook = createJsonOrderBook(i, order_side, order_price, order_size);
                client.sendMsg(msgBook);
                System.out.println("WSSClient send[Order_Book]: " + msgBook);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void testRandom(){
        try {
            String uri = "wss://127.0.0.1:15051";
            Map<String, String> mapHeader = new HashMap<>();
            int connectTimeout = 30;
            mapHeader.put("abc", "123");
            mapHeader.put("address", "address123");
            WSSClient client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
            Thread.sleep(1000);
            
            String order_side = "buy";
            int order_price = 1;
            int order_size = 1;
            int order_size_cancel = 1;
            for (int i = 1; i <= 1000; i++) {
                if (i%2 == 0){
                    order_side = "buy";
                } else{
                    order_side = "sell";
                }
                order_price = randomRange(1, 1000);
                order_size = randomRange(1, 100);
                String msgBook = createJsonOrderBook(i, order_side, order_price, order_size);
                client.sendMsg(msgBook);
                System.out.println("WSSClient send[Order_Book]: " + msgBook);

                if(i%5 == 0) {
                    order_size_cancel = randomRange(1, 100);
                    String msgCancel = createJsonOrderCancel(i-1, order_size_cancel);
                    client.sendMsg(msgCancel);
                    System.out.println("WSSClient send[Order_Cancel]: " + msgCancel);
                }

                //Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String createJsonOrderBook(long orderId, String side, int price, int size){
        String rs = "{\"ws_topic\":\"ws_topic_price_match\",\"ws_type\":\"ws_type_order_book\",\"order_id\":"+orderId
                +",\"order_side\":\""+side+"\",\"order_price\":"+price+",\"order_size\":"+size+"}";
        return rs;
    }
    
    public static String createJsonOrderCancel(long orderId, int size){
        String rs = "{\"ws_topic\":\"ws_topic_price_match\",\"ws_type\":\"ws_type_order_cancel\",\"order_id\":"+orderId +",\"order_size\":"+size+"}";
        return rs;
    }
    
    private static Random rd = new Random();
    public static int randomRange(int min, int max){
        return rd.nextInt((max - min) + 1) + min;
    }

    public static WSSClient getInstance(String uri) {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    WSSClient.uri = uri;
                    URI serverUri = new URI(uri);
                    instance = new WSSClient(serverUri, protocolDraft, mapHeader, connectTimeout);
                    instance.initSSL();
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

    public static WSSClient getInstance(String uri, Map<String, String> mapHeader) {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    WSSClient.uri = uri;
                    WSSClient.mapHeader = mapHeader;
                    URI serverUri = new URI(uri);
                    instance = new WSSClient(serverUri, protocolDraft, mapHeader, connectTimeout);
                    instance.initSSL();
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

    public static WSSClient getInstance(String uri, Map<String, String> mapHeader, int connectTimeout) {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    WSSClient.uri = uri;
                    WSSClient.mapHeader = mapHeader;
                    WSSClient.connectTimeout = connectTimeout;
                    URI serverUri = new URI(uri);
                    instance = new WSSClient(serverUri, protocolDraft, mapHeader, connectTimeout);
                    instance.initSSL();
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

    public WSSClient(URI serverUri) {
        super(serverUri);
    }

    public WSSClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public WSSClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    public void initSSL() {
        try {
            System.out.println(">>>>> initSSL");
            // load up the key store
            String STORETYPE = "JKS";
            String KEYSTORE = "./cert/client-keystore.jks";
            String STOREPASSWORD = "storetest123";
            String KEYPASSWORD = "keytest123";

            KeyStore ks = KeyStore.getInstance(STORETYPE);
            File kf = new File(KEYSTORE);
            ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, KEYPASSWORD.toCharArray());
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//            tmf.init(ks);
            
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }
            }};

            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            sslContext.init(kmf.getKeyManagers(), trustAllCerts, null);
            SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();
            instance.setSocket(factory.createSocket());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(ServerHandshake sh) {
        System.out.println("WSSClient new connection opened to: " + getURI());
    }

    @Override
    public void onMessage(String message) {
        System.out.println("WSSClient received message: " + message);
        
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WSSClient closed with exit code " + code + " additional info: " + reason);
        instance = null;
        System.out.println("============================= RetryConnection =============================");
        WSSClient client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WSSClient an error occurred:" + ex);
    }

    public void sendMsg(String msg) {
        if (msg != null && !msg.isEmpty()) {
            send(msg);
        }
    }
}
