package com.converter.service;

import com.converter.exceptions.ProcessExecutionException;
import com.converter.objects.EdiRequest;
import com.converter.objects.EdiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ConverterService {
    private final ExecutorService executorService;
    private final JsonToEdiPool jsonToEdiPool;

    public ConverterService(JsonToEdiPool jsonToEdiPool) {
        this.jsonToEdiPool = jsonToEdiPool;
        this.executorService = Executors.newFixedThreadPool(jsonToEdiPool.getPoolSize());
    }

    public List<EdiResponse> convertToEdi(List<EdiRequest> requests, String agencyCode) throws ProcessExecutionException {
        List<Future<EdiResponse>> futures = new ArrayList<>();

        try {
            for (EdiRequest request : requests) {
                // Borrow a JsonToEdi object from the pool
                JsonToEdi jsonToEdi = jsonToEdiPool.borrowJsonToEdi();
                Callable<EdiResponse> task = () -> {
                    try {
                        return jsonToEdi.convert(request, agencyCode);
                    } finally {
                        // Return the JsonToEdi object to the pool after use
                        jsonToEdiPool.returnJsonToEdi(jsonToEdi);
                    }
                };
                futures.add(executorService.submit(task));
            }

            List<EdiResponse> responses = new ArrayList<>();
            for (Future<EdiResponse> future : futures) {
                try {
                    responses.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new ProcessExecutionException(e.getMessage());
                }
            }
            return responses;
        } catch (Exception e) {
            throw new ProcessExecutionException(e.getMessage());
        }
    }



}
