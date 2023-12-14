package com.birtek.cashew.web;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CashewStatusServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CashewStatusServer.class);

    private static final ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    private HttpServer server;
    public CashewStatusServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(80), 0);
        } catch (IOException e) {
            LOGGER.error("Failed to create CashewStatusServer");
            return;
        }

        server.createContext("/", new CashewStatusHttpHandler());
        server.setExecutor(threadPoolExecutor);
        server.start();
    }
}
