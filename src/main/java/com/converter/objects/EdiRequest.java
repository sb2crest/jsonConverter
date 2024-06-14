package com.converter.objects;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class EdiRequest {
    private String refId;
    private JsonNode subject;
}
