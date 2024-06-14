package com.converter.controller;

import com.converter.exceptions.ProcessExecutionException;
import com.converter.objects.EdiRequest;
import com.converter.objects.EdiResponse;
import com.converter.service.ConverterService;
import com.fasterxml.jackson.databind.JsonNode;
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
public class EdiController {
    private final ConverterService ediConverter;

    public EdiController(ConverterService jsonToEdi) {
        this.ediConverter = jsonToEdi;
    }

    @PostMapping("/json-edi")
    public List<EdiResponse> convertToEdi(@RequestBody List<EdiRequest> requests) {
        return ediConverter.convertToEdiParallel(requests);
    }
}
