package com.converter.controller;

import com.converter.exceptions.ProcessExecutionException;
import com.converter.objects.EdiRequest;
import com.converter.objects.EdiResponse;
import com.converter.service.ConverterService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

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
    public List<EdiResponse> convertToEdi(@RequestBody List<EdiRequest> requests, @RequestParam("org") String org) {
        List<EdiResponse> responses= ediConverter.convertToEdi(requests, org);
        System.out.println(responses.get(0).getFile());
        return responses;
    }


}
