/*
 * Copyright 2018 nghiatc.
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

import static com.ntc.wsclient.WSSClient.createJsonOrderBook;
import static com.ntc.wsclient.WSSClient.createJsonOrderCancel;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author nghiatc
 * @since Feb 5, 2018
 */
public class BenchmarkWSSClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // 1. testFixBuyFixSell
            //testFixBuyFixSell();
            
            // 2. testBuySellRandom
            // testBuySellRandom();
            
            // 3. testBuySellCancelRandom
            //testBuySellCancelRandom();
            
            // 4. testMultiFixBuyFixSell
            //testMultiFixBuyFixSell();
            
            // 5. testMultiBuySellCancelRandom
            testMultiBuySellCancelRandom();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void testBuySellRandom(){
        try {
            Thread tbuysellrandom = new Thread(new BuySellRandomRunner(1000));
            tbuysellrandom.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void testFixBuyFixSell(){
        try {
            Thread tbuy = new Thread(new BuyFixRunner(1, 1000));
            Thread tsell = new Thread(new SellFixRunner(10001, 1000));
            
            tbuy.start();
            tsell.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void testMultiFixBuyFixSell(){
        try {
            long startId = 1;
            int number = 1000;
            int nThread = 20;
            List<Thread> listT = new ArrayList<>();
            for(int i = 0; i < nThread; i++) {
                if(i%2 == 0) {
                    Thread tbuy = new Thread(new BuyFixRunner(startId, number));
                    listT.add(tbuy);
                } else {
                    Thread tsell = new Thread(new SellFixRunner(startId, number));
                    listT.add(tsell);
                }
                startId += number;
            }
            
            for(Thread t : listT) {
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void testBuySellCancelRandom(){
        try {
            Thread tbuysellcancelrandom = new Thread(new BuySellCancelRandomRunner(1000));
            tbuysellcancelrandom.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void testMultiBuySellCancelRandom(){
        try {
            long startId = 1;
            int number = 1000;
            int nThread = 20;
            ExecutorService executor = Executors.newFixedThreadPool(nThread);
            List <Future<Map<String, Long>>> listF = new ArrayList<Future<Map<String, Long>>>();
            
            for(int i = 0; i < nThread; i++) {
                Future<Map<String, Long>> future = executor.submit(new BuySellCancelRandomCaller(startId, number));
                listF.add(future);
                startId += number;
            }
            
            Thread.sleep(2000);
            // Stats
            long total_order = 0;
            long total_price = 0;
            long total_order_buy = 0;
            long total_price_buy = 0;
            long total_order_sell = 0;
            long total_price_sell = 0;
            long total_order_cancel = 0;
            for(Future<Map<String, Long>> f : listF) {
                Map<String, Long> mapRS = f.get();
                total_order_buy += mapRS.getOrDefault("total_order_buy", 0L);
                total_price_buy += mapRS.getOrDefault("total_price_buy", 0L);
                total_order_sell += mapRS.getOrDefault("total_order_sell", 0L);
                total_price_sell += mapRS.getOrDefault("total_price_sell", 0L);
                total_order_cancel += mapRS.getOrDefault("total_order_cancel", 0L);
                total_order += mapRS.getOrDefault("total_order", 0L);
                total_price += mapRS.getOrDefault("total_price", 0L);
            }
            Thread.sleep(2000);
            System.out.println("***************** All Stats ****************");
            System.out.println("ALL total_order_buy: " + total_order_buy);
            System.out.println("ALL total_price_buy: " + total_price_buy);
            System.out.println("ALL total_order_sell: " + total_order_sell);
            System.out.println("ALL total_price_sell: " + total_price_sell);
            System.out.println("ALL total_order_cancel: " + total_order_cancel);
            System.out.println("ALL total_order: " + total_order);
            System.out.println("ALL total_price: " + total_price);
            System.out.println("********************************************");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static Random rd = new Random();
    public static int randomRange(int min, int max){
        return rd.nextInt((max - min) + 1) + min;
    }
    
    public static class BuyFixRunner implements Runnable{
        private int time = 0;
        private long startId = 1;
        private long number = 1000;
        private int order_price = 100;
        private int order_size = 10;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        public BuyFixRunner(long startId, long number) {
            this.startId = startId;
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuyFixRunner(long startId, long number, int order_price, int order_size) {
            this.startId = startId;
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuyFixRunner(long startId, long number, String uri) {
            this.startId = startId;
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuyFixRunner(long startId, long number, int order_price, int order_size, String uri) {
            this.startId = startId;
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }

        @Override
        public void run() {
            try {
                String order_side = "buy";
                long n = number + startId - 1;
                for (long i = startId; i <= n; i++) {
                    String msgBook = createJsonOrderBook(i, order_side, order_price, order_size);
                    client.sendMsg(msgBook);
                    System.out.println("BuyFixRunner send[Order_Book]["+i+"]: " + msgBook);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static class BuyRandomRunner implements Runnable{
        private int time = 0;
        private int startId = 1;
        private long number = 1000;
        private int order_price = 100;
        private int order_size = 10;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        public BuyRandomRunner(int startId, long number) {
            this.startId = startId;
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuyRandomRunner(int startId, long number, int order_price, int order_size) {
            this.startId = startId;
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuyRandomRunner(int startId, long number, String uri) {
            this.startId = startId;
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuyRandomRunner(int startId, long number, int order_price, int order_size, String uri) {
            this.startId = startId;
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }

        @Override
        public void run() {
            try {
                String order_side = "buy";
                long n = number + startId - 1;
                int order_price_rd = 1;
                int order_size_rd = 1;
                for (long i = startId; i <= n; i++) {
                    order_price_rd = randomRange(1, order_price);
                    order_size_rd = randomRange(1, order_size);
                    String msgBook = createJsonOrderBook(i, order_side, order_price_rd, order_size_rd);
                    client.sendMsg(msgBook);
                    System.out.println("BuyRandomRunner send[Order_Book]["+i+"]: " + msgBook);
                }
            } catch (Exception e) {
            }
        }
    }
    
    public static class SellFixRunner implements Runnable{
        private int time = 0;
        private long startId = 1;
        private long number = 1000;
        private int order_price = 100;
        private int order_size = 10;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        public SellFixRunner(long startId, long number) {
            this.startId = startId;
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public SellFixRunner(long startId, long number, int order_price, int order_size) {
            this.startId = startId;
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public SellFixRunner(long startId, long number, String uri) {
            this.startId = startId;
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public SellFixRunner(long startId, long number, int order_price, int order_size, String uri) {
            this.startId = startId;
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }

        @Override
        public void run() {
            try {
                String order_side = "sell";
                long n = number + startId - 1;
                for (long i = startId; i <= n; i++) {
                    String msgBook = createJsonOrderBook(i, order_side, order_price, order_size);
                    client.sendMsg(msgBook);
                    System.out.println("SellFixRunner send[Order_Book]["+i+"]: " + msgBook);
                }
            } catch (Exception e) {
            }
        }
    }
    
    public static class SellRandomRunner implements Runnable{
        private int time = 0;
        private int startId = 1;
        private long number = 1000;
        private int order_price = 100;
        private int order_size = 10;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        public SellRandomRunner(int startId, long number) {
            this.startId = startId;
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public SellRandomRunner(int startId, long number, int order_price, int order_size) {
            this.startId = startId;
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public SellRandomRunner(int startId, long number, String uri) {
            this.startId = startId;
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public SellRandomRunner(int startId, long number, int order_price, int order_size, String uri) {
            this.startId = startId;
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }

        @Override
        public void run() {
            try {
                String order_side = "sell";
                long n = number + startId - 1;
                int order_price_rd = 1;
                int order_size_rd = 1;
                for (long i = startId; i <= n; i++) {
                    order_price_rd = randomRange(1, order_price);
                    order_size_rd = randomRange(1, order_size);
                    String msgBook = createJsonOrderBook(i, order_side, order_price_rd, order_size_rd);
                    client.sendMsg(msgBook);
                    System.out.println("SellRandomRunner send[Order_Book]["+i+"]: " + msgBook);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static class CancelFixRunner implements Runnable{
        private int time = 0;
        private int startId = 1;
        private long number = 1000;
        private int order_size = 5;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        public CancelFixRunner(int startId, long number) {
            this.startId = startId;
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public CancelFixRunner(int startId, long number, int order_size) {
            this.startId = startId;
            this.number = number;
            this.order_size = order_size;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public CancelFixRunner(int startId, long number, String uri) {
            this.startId = startId;
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public CancelFixRunner(int startId, long number, int order_size, String uri) {
            this.startId = startId;
            this.number = number;
            this.order_size = order_size;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }

        @Override
        public void run() {
            try {
                long n = number + startId - 1;
                for (long i = startId; i <= n; i++) {
                    String msgCancel = createJsonOrderCancel(i-1, order_size);
                    client.sendMsg(msgCancel);
                    System.out.println("CancelFixRunner send[Order_Cancel]["+i+"]: " + msgCancel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static class CancelRandomRunner implements Runnable{
        private int time = 0;
        private int startId = 1;
        private long number = 1000;
        private int order_size = 5;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        public CancelRandomRunner(int startId, long number) {
            this.startId = startId;
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public CancelRandomRunner(int startId, long number, int order_size) {
            this.startId = startId;
            this.number = number;
            this.order_size = order_size;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public CancelRandomRunner(int startId, long number, String uri) {
            this.startId = startId;
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public CancelRandomRunner(int startId, long number, int order_size, String uri) {
            this.startId = startId;
            this.number = number;
            this.order_size = order_size;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }

        @Override
        public void run() {
            try {
                long n = number + startId - 1;
                int order_size_rd = 1;
                for (long i = startId; i <= n; i++) {
                    order_size_rd = randomRange(1, order_size);
                    String msgCancel = createJsonOrderCancel(i-1, order_size_rd);
                    client.sendMsg(msgCancel);
                    System.out.println("CancelRandomRunner send[Order_Cancel]["+i+"]: " + msgCancel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static class BuySellFixRunner implements Runnable{
        private int time = 0;
        private long number = 1000;
        private int order_price = 100;
        private int order_size = 10;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        public BuySellFixRunner(long number) {
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellFixRunner(long number, int order_price, int order_size) {
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellFixRunner(long number, String uri) {
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellFixRunner(long number, int order_price, int order_size, String uri) {
            this.number = number;
            this.order_price = order_price;
            this.order_size = order_size;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }

        @Override
        public void run() {
            try {
                String order_side = "buy";
                for (long i = 1; i <= number; i++) {
                    if (i%2 == 0){
                        order_side = "buy";
                    } else{
                        order_side = "sell";
                    }
                    String msgBook = createJsonOrderBook(i, order_side, order_price, order_size);
                    client.sendMsg(msgBook);
                    System.out.println("BuySellFixRunner send[Order_Book]["+i+"]: " + msgBook);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static class BuySellRandomRunner implements Runnable{
        private int time = 0;
        private long number = 1000;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        public BuySellRandomRunner(long number) {
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellRandomRunner(long number, String uri) {
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }

        @Override
        public void run() {
            try {
                // Stats.
                long total_order = 0;
                long total_price = 0;
                long total_order_buy = 0;
                long total_price_buy = 0;
                long total_order_sell = 0;
                long total_price_sell = 0;
                
                // Send message.
                String order_side = "buy";
                int order_price = 1;
                int order_size = 1;
                for (long i = 1; i <= number; i++) {
                    order_price = randomRange(1, 1000);
                    order_size = randomRange(1, 100);
                    if (i%2 == 0){
                        order_side = "buy";
                        total_order_buy += order_size;
                        total_price_buy += order_size * order_price;
                    } else{
                        order_side = "sell";
                        total_order_sell += order_size;
                        total_price_sell += order_size * order_price;
                    }
                    total_order += order_size;
                    total_price += order_size * order_price;
                    
                    String msgBook = createJsonOrderBook(i, order_side, order_price, order_size);
                    client.sendMsg(msgBook);
                    System.out.println("BuySellRandomRunner send[Order_Book]["+i+"]: " + msgBook);
                }
                Thread.sleep(3000);
                System.out.println("******************* Stats ******************");
                System.out.println("total_order_buy: " + total_order_buy);
                System.out.println("total_price_buy: " + total_price_buy);
                System.out.println("total_order_sell: " + total_order_sell);
                System.out.println("total_price_sell: " + total_price_sell);
                System.out.println("total_order: " + total_order);
                System.out.println("total_price: " + total_price);
                System.out.println("********************************************");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static class BuySellCancelRandomCaller implements Callable<Map<String, Long>> {
        private int time = 0;
        private long startId = 1;
        private long number = 1000;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        // Stats.
        public long total_order = 0;
        public long total_price = 0;
        public long total_order_buy = 0;
        public long total_price_buy = 0;
        public long total_order_sell = 0;
        public long total_price_sell = 0;
        public long total_order_cancel = 0;
        
        public BuySellCancelRandomCaller(long number) {
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellCancelRandomCaller(long startId, long number) {
            this.startId = startId;
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellCancelRandomCaller(long number, String uri) {
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellCancelRandomCaller(long startId, long number, String uri) {
            this.startId = startId;
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        @Override
        public Map<String, Long> call() throws Exception {
            Map<String, Long> rs = new HashMap<>();
            try {
                // Send message.
                String order_side = "buy";
                int order_price = 1;
                int order_size = 1;
                int order_size_cancel = 1;
                long n = startId + number - 1;
                for (long i = startId; i <= n; i++) {
                    order_price = randomRange(1, 1000);
                    order_size = randomRange(1, 100);
                    if (i%2 == 0){
                        order_side = "buy";
                        total_order_buy += order_size;
                        total_price_buy += order_size * order_price;
                    } else{
                        order_side = "sell";
                        total_order_sell += order_size;
                        total_price_sell += order_size * order_price;
                    }
                    total_order += order_size;
                    total_price += order_size * order_price;
                    
                    String msgBook = createJsonOrderBook(i, order_side, order_price, order_size);
                    client.sendMsg(msgBook);
                    System.out.println("BuySellCancelRandomRunner send[Order_Book]["+i+"]: " + msgBook);

                    if(i%5 == 0) {
                        order_size_cancel = randomRange(1, 100);
                        total_order_cancel += order_size_cancel;
                        String msgCancel = createJsonOrderCancel(i-1, order_size_cancel);
                        client.sendMsg(msgCancel);
                        System.out.println("BuySellCancelRandomRunner send[Order_Cancel]["+i+"]: " + msgCancel);
                    }
                }
                
                rs.put("total_order_buy", total_order_buy);
                rs.put("total_price_buy", total_price_buy);
                rs.put("total_order_sell", total_order_sell);
                rs.put("total_price_sell", total_price_sell);
                rs.put("total_order_cancel", total_order_cancel);
                rs.put("total_order", total_order);
                rs.put("total_price", total_price);
                Thread.sleep(3000);
                System.out.println("******************* Stats ******************");
                System.out.println("total_order_buy: " + total_order_buy);
                System.out.println("total_price_buy: " + total_price_buy);
                System.out.println("total_order_sell: " + total_order_sell);
                System.out.println("total_price_sell: " + total_price_sell);
                System.out.println("total_order_cancel: " + total_order_cancel);
                System.out.println("total_order: " + total_order);
                System.out.println("total_price: " + total_price);
                System.out.println("********************************************");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rs;
        }
    }
    
    public static class BuySellCancelRandomRunner implements Runnable{
        private int time = 0;
        private long startId = 1;
        private long number = 1000;
        private WSSClient client;
        private String uri = "wss://127.0.0.1:8787";
        
        // Stats.
        public long total_order = 0;
        public long total_price = 0;
        public long total_order_buy = 0;
        public long total_price_buy = 0;
        public long total_order_sell = 0;
        public long total_price_sell = 0;
        public long total_order_cancel = 0;
        
        public BuySellCancelRandomRunner(long number) {
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellCancelRandomRunner(long startId, long number) {
            this.startId = startId;
            this.number = number;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "456");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellCancelRandomRunner(long number, String uri) {
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }
        
        public BuySellCancelRandomRunner(long startId, long number, String uri) {
            this.startId = startId;
            this.number = number;
            this.uri = uri;
            int connectTimeout = 30;
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("abc", "123");
            mapHeader.put("xyz", "xyz");
            client = WSSClient.getInstance(uri, mapHeader, connectTimeout);
        }

        @Override
        public void run() {
            try {
                // Send message.
                String order_side = "buy";
                int order_price = 1;
                int order_size = 1;
                int order_size_cancel = 1;
                long n = startId + number - 1;
                for (long i = startId; i <= n; i++) {
                    order_price = randomRange(1, 1000);
                    order_size = randomRange(1, 100);
                    if (i%2 == 0){
                        order_side = "buy";
                        total_order_buy += order_size;
                        total_price_buy += order_size * order_price;
                    } else{
                        order_side = "sell";
                        total_order_sell += order_size;
                        total_price_sell += order_size * order_price;
                    }
                    total_order += order_size;
                    total_price += order_size * order_price;
                    
                    String msgBook = createJsonOrderBook(i, order_side, order_price, order_size);
                    client.sendMsg(msgBook);
                    System.out.println("BuySellCancelRandomRunner send[Order_Book]["+i+"]: " + msgBook);

                    if(i%5 == 0) {
                        order_size_cancel = randomRange(1, 100);
                        total_order_cancel += order_size_cancel;
                        String msgCancel = createJsonOrderCancel(i-1, order_size_cancel);
                        client.sendMsg(msgCancel);
                        System.out.println("BuySellCancelRandomRunner send[Order_Cancel]["+i+"]: " + msgCancel);
                    }
                }
                
                Thread.sleep(3000);
                System.out.println("******************* Stats ******************");
                System.out.println("total_order_buy: " + total_order_buy);
                System.out.println("total_price_buy: " + total_price_buy);
                System.out.println("total_order_sell: " + total_order_sell);
                System.out.println("total_price_sell: " + total_price_sell);
                System.out.println("total_order_cancel: " + total_order_cancel);
                System.out.println("total_order: " + total_order);
                System.out.println("total_price: " + total_price);
                System.out.println("********************************************");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
