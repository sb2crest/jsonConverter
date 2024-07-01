package com.converter.objects;

import lombok.Data;

import java.util.*;

@Data
public class Components {
    private Map<String, StringBuilder> singleLineHolder = new HashMap<>();
    private Map<String, List<StringBuilder>> groupRepeatableLineHolder = new HashMap<>();
    private Map<String, List<StringBuilder>> innerListLineHolder = new HashMap<>();
    private Map<String, List<StringBuilder>> temporaryLineHolder = new HashMap<>();
    private Set<String> keySet = new HashSet<>();
    private List<StringBuilder> allEntries = new ArrayList<>();
    private List<String> finalOutputFile = new ArrayList<>();
}
