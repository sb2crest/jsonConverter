package com.converter.controller;

import com.converter.exceptions.ProcessExecutionException;
import com.converter.objects.EdiRequest;
import com.converter.objects.EdiResponse;
import com.converter.service.ConverterService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("convert")
@Slf4j
public class EdiController {
    private final ConverterService ediConverter;

    public EdiController(ConverterService jsonToEdi) {
        this.ediConverter = jsonToEdi;
    }

    @PostMapping("/json-edi")
    public List<EdiResponse> convertToEdi(@RequestBody List<EdiRequest> requests) {
        List<EdiResponse> responses = ediConverter.convertToEdiSequential(requests,true);
        for (EdiResponse response : responses) {
            downloadRandomSampleEdiFile(response.getFile(), getFilePath(response.getRefId()));
        }
        return responses;
    }
    private Path getFilePath(String refId) {
        String fileName = "output_" + refId + ".txt";
        Path downloadsPath = Paths.get(System.getProperty("user.home"), "Downloads" + "/Test_folder/");
        return downloadsPath.resolve(fileName);
    }

    private void downloadRandomSampleEdiFile(String ediFile, Path filename) {
        log.info("Downloading sample EDI, Saved file:- > {} location", filename);
        try (BufferedWriter writer = Files.newBufferedWriter(filename)) {
            writer.write(ediFile);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
