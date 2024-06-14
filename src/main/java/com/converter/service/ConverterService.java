package com.converter.service;

import com.converter.exceptions.ProcessExecutionException;
import com.converter.objects.EdiRequest;
import com.converter.objects.EdiResponse;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ConverterService {
    private final ExecutorService executorService;
    private final JsonToEdi jsonToEdi;

    public ConverterService(JsonToEdi jsonToEdi) {
        this.executorService = Executors.newFixedThreadPool(20);
        this.jsonToEdi = jsonToEdi;
    }

    public List<EdiResponse> convertToEdiParallel(List<EdiRequest> requests) throws ProcessExecutionException {
        List<Future<EdiResponse>> responseFutures = new ArrayList<>();
        requests.parallelStream()
                .forEach(request -> {
                    Callable<EdiResponse> task = () -> jsonToEdi.convert(request);
                    responseFutures.add(executorService.submit(task));
                });
        List<EdiResponse> responses = new ArrayList<>();
        for (Future<EdiResponse> future : responseFutures) {
            try {
                responses.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new ProcessExecutionException(e.getMessage());
            }
        }
        return responses;
    }

    public List<EdiResponse> convertToEdiSequential(List<EdiRequest> requests, boolean isOrderImportant) throws ProcessExecutionException {
        if (isOrderImportant) {
            return requests.stream()
                    .map(jsonToEdi::convert)
                    .toList();
        } else {
            return requests.parallelStream()
                    .map(jsonToEdi::convert
                    ).toList();
        }
    }
    public EdiResponse convertToEdi(EdiRequest request) throws ProcessExecutionException {
        return jsonToEdi.convert(request);
    }

}
