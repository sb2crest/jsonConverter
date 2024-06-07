package com.converter.controller;

import com.converter.exceptions.ProcessExecutionException;
import com.converter.service.ConverterService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("convert")
public class EdiController {
    private final ConverterService ediConverter;

    public EdiController(ConverterService jsonToEdi) {
        this.ediConverter = jsonToEdi;
    }

    @PostMapping("/json-edi")
    public String convertToEdi(@RequestBody JsonNode inputData, @RequestParam("org") String org) {
        try {
            return ediConverter.convertToEdi(inputData, org);
        } catch (ExecutionException | InterruptedException e) {
            throw new ProcessExecutionException(e.getMessage());
        }
    }


}
