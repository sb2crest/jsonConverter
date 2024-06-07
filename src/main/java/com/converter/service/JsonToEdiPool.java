package com.converter.service;

import com.converter.exceptions.InvalidDataException;
import com.converter.exceptions.MaxRetriesReachedException;
import com.converter.initializers.Positions;
import com.converter.utils.FileWriter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class JsonToEdiPool {
    private int poolSize = 10;
    private final Queue<JsonToEdi> pool;
    private static final Logger log = LoggerFactory.getLogger(JsonToEdiPool.class);

    @Autowired
    public JsonToEdiPool(Positions positions, FileWriter fileWriter) {
        this.pool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            pool.offer(new JsonToEdi(positions, fileWriter));
        }
    }

    public JsonToEdi getJsonToEdi() {
        return get();
    }
    public JsonToEdi getJsonToEdi(int poolSize) {
        this.poolSize = poolSize;
        return get();
    }
    private JsonToEdi get(){
        JsonToEdi jsonToEdi = pool.poll();
        if (jsonToEdi == null) {
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                jsonToEdi = pool.poll();
                if (jsonToEdi != null) {
                    return jsonToEdi;
                } else {
                    log.warn("All objects in the pool are in use. Retrying after 1 second...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrupted while waiting for retry.");
                    }
                }
            }
            log.error("Pool exhausted after {} retries. Increase pool size or wait for a resource to become available.", maxRetries);
            throw new MaxRetriesReachedException("Pool exhausted after " + maxRetries + " retries. Increase pool size or wait for a resource to become available.");
        }
        return jsonToEdi;
    }

    public void returnJsonToEdi(JsonToEdi jsonToEdi) {
        pool.offer(jsonToEdi);
    }

    public int getPoolSize() {
        return poolSize;
    }
}
