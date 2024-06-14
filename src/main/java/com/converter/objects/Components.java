package com.converter.objects;

import lombok.Data;

import java.util.*;

@Data
public class Components {
    private  Map<String, StringBuilder> nonRepeatableLinesMap = new HashMap<>();
    private  Map<String, List<StringBuilder>> groupRepeatableMap = new HashMap<>();
    private Set<String> keySet = new HashSet<>();
    private List<StringBuilder> allEntries = new ArrayList<>();
    private List<String> finalOutputFile = new ArrayList<>();
}
