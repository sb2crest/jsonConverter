package com.converter.service;

import com.converter.exceptions.InvalidDataException;
import com.converter.exceptions.ProcessExecutionException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ConverterService {
    private final ExecutorService executorService;
    private final JsonToEdiPool jsonToEdiPool;

    @Autowired
    public ConverterService(JsonToEdiPool jsonToEdiPool) {
        this.jsonToEdiPool = jsonToEdiPool;
        this.executorService = Executors.newFixedThreadPool(jsonToEdiPool.getPoolSize());
    }

    public String convertToEdi(JsonNode json, String agencyCode) throws ExecutionException, InterruptedException {
        JsonToEdi jsonToEdi = jsonToEdiPool.getJsonToEdi();
        return executorService.submit(() -> {
            try {
                return jsonToEdi.convert(json, agencyCode);
            } finally {
                jsonToEdiPool.returnJsonToEdi(jsonToEdi);
            }
        }).get();
    }
    public String convertToEdi(JsonNode json, String agencyCode,int poolSize) {
        JsonToEdi jsonToEdi = jsonToEdiPool.getJsonToEdi(poolSize);
        try{
            return executorService.submit(() -> {
                try {
                    return jsonToEdi.convert(json, agencyCode);
                } finally {
                    jsonToEdiPool.returnJsonToEdi(jsonToEdi);
                }
            }).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new ProcessExecutionException(e.getMessage());
        }

    }
}
