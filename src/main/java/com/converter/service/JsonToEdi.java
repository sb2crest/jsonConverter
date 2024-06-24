package com.converter.service;

import com.converter.exceptions.InvalidDataException;
import com.converter.initializers.Positions;
import com.converter.objects.Components;
import com.converter.objects.EdiRequest;
import com.converter.objects.EdiResponse;
import com.converter.objects.ProcessResult;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class JsonToEdi {
    private static final Logger log = LoggerFactory.getLogger(JsonToEdi.class);
    private static  List<String> SORTING_ORDER;
    private static  Map<String, String> MAP_POSITIONS;

    public JsonToEdi(String organization) {
        log.info("Feeding Information for organization "+ organization);
        feedMappingDetails(organization);
    }
    @Autowired
    public JsonToEdi(){
    }

    public EdiResponse convert(EdiRequest request) {
        feedMappingDetails("FDA");
        validateInput(request);

        Components components = new Components();
        try {
            String ediFile = map(request.getSubject(),components);
            components.getFinalOutputFile().clear();
            EdiResponse response = new EdiResponse();
            response.setRefId(request.getRefId());
            response.setFile(ediFile);
            return response;
        } catch (InvalidDataException e) {
            components.getFinalOutputFile().clear();
            reset(components);
            throw new InvalidDataException("Error processing JSON data, " + e.getMessage());
        }
    }

    private String map(JsonNode jsonData,Components components) {
        components.getFinalOutputFile().addAll(List.of("A0901SY2SECFIL111715     PE                                361634    361634     ", "B  0901ME0PE                                               CRIMSONTEST1         "));
        processJsonData(jsonData,components);
        components.getFinalOutputFile().addAll(List.of("Y  0901ME0PE                                                                    ", "Z0901SY2      111715                                                            "));
        String finalOutput = convertListToString(components.getFinalOutputFile());
        components.getFinalOutputFile().clear();
        return finalOutput;
    }

    public String convertListToString(List<String> list) {
        return String.join("\n", list) + "\n";
    }

    private void validateInput(EdiRequest request) {
        if (request.getSubject() == null) {
            throw new InvalidDataException("The provided json data is null");
        }
        if (request.getRefId() == null) {
            throw new InvalidDataException("The provided json data is null, This is thread based execution. Providing a reference is mandatory for identification");
        }
        if (SORTING_ORDER==null || MAP_POSITIONS==null) {
            throw new InvalidDataException("Please Configure organization name in your configuration file");
        }
    }

    private static void feedMappingDetails(String agencyCode) {
        if (agencyCode.equals("FDA")) {
            if (MAP_POSITIONS != null && SORTING_ORDER != null) {
                return;
            }
            Map<List<String>, Map<String, String>> fda = Positions.getFDA();
            if (!fda.isEmpty()) {
                Map.Entry<List<String>, Map<String, String>> entry = fda.entrySet().iterator().next();
                SORTING_ORDER = entry.getKey();
                MAP_POSITIONS = entry.getValue();
            } else {
                throw new InvalidDataException("No mapping information found for the entered Agency code " + agencyCode);
            }
        } else {
            throw new InvalidDataException("No mapping information found for the entered Agency code " + agencyCode);
        }
    }

    private void sortLines(Components components) {
        List<String> sortedKeys = new ArrayList<>(components.getSingleLineHolder().keySet());
        sortedKeys.addAll(components.getGroupRepeatableLineHolder().keySet());
        sortedKeys = sortedKeys.stream().distinct().filter(key -> !key.isEmpty()).sorted(Comparator.comparingInt(SORTING_ORDER::indexOf)).collect(Collectors.toList());
        sortedKeys.forEach(entry -> {
            if (components.getSingleLineHolder().containsKey(entry)) {
                components.getAllEntries().add(components.getSingleLineHolder().get(entry));
            } else if (components.getGroupRepeatableLineHolder().containsKey(entry)) {
                components.getAllEntries().addAll(components.getGroupRepeatableLineHolder().get(entry));
            } else {
                log.error("Not matching key :{}", entry);
            }
        });
    }

    private void processJsonData(JsonNode jsonData,Components components) {
        jsonData.fields().forEachRemaining(data -> {
            if (!data.getValue().isArray() && !data.getValue().isObject()) {
                ProcessResult processResult = processField(data.getKey(), data.getValue().asText(),components);
                components.getKeySet().add(processResult.getPgSegment());
            } else if (data.getValue().isArray()) {
                processRootArray(data.getValue(),components);
            }
        });
    }

    private void processRootArray(JsonNode jsonNode,Components components) {
        jsonNode.forEach(element -> {
            if (element.isObject()) {
                processObject(element,components);
            }
            writeToFile(components);
        });
    }

    private void writeToFile(Components components) {
        sortLines(components);
        components.getFinalOutputFile().addAll(components.getAllEntries().stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList()
        );
        reset(components);
    }

    private void reset(Components components) {
        components.getAllEntries().clear();
        components.getGroupRepeatableLineHolder().clear();
        components.getSingleLineHolder().clear();
    }

    private void processArray(JsonNode arrayNode,Components components) {
        arrayNode.forEach(element -> {
            if (element.isObject()) {
                List<String> pgSegment = processObject(element,components);
                processSegment(pgSegment,components);
            }
        });
    }
    private Set<String> processInnerArray(JsonNode arrayNode,Components components) {
        Set<String> pgSegmentList = new HashSet<>();
        arrayNode.forEach(element -> {
            if (element.isObject()) {
                List<String> pgSegment = processObject(element,components);
                pgSegmentList.addAll(pgSegment);
                processSegmentForInnerArrays(pgSegment,components);
            }
        });
        return pgSegmentList;
    }
    private void processSegmentForInnerArrays(List<String> pgSegments, Components components){
        pgSegments.forEach(
                segment -> {
                    if (components.getSingleLineHolder().containsKey(segment)) {
                        components.getInnerListLineHolder().computeIfAbsent(segment, k -> new ArrayList<>())
                               .add(components.getSingleLineHolder().get(segment));
                        components.getSingleLineHolder().remove(segment);
                    } else {
                        log.error("Not matching key :{}", segment);
                    }
                }
        );
    }

    private List<String> processObject(JsonNode element,Components components) {
        List<String> pgSegmentTracker = new ArrayList<>();
        AtomicBoolean isGroupRepeatable = new AtomicBoolean(false);
        element.fields().forEachRemaining(field -> {
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            if (!fieldValue.isArray() && !fieldValue.isObject()) {
                ProcessResult processResult = processField(fieldName, fieldValue.asText(),components);
                isGroupRepeatable.set(processResult.isGroupRepeatable());
                pgSegmentTracker.add(processResult.getPgSegment());
            } else if (fieldValue.isArray() && isGroupRepeatable.get()) {
                Set<String> strings = processInnerArray(fieldValue, components);
                pgSegmentTracker.addAll(strings);
            } else if (fieldValue.isArray() && !isGroupRepeatable.get() ) {
                processArray(fieldValue,components);
            }
        });
        return pgSegmentTracker;
    }
    private void processSegment(List<String> pgSegment,Components components) {
        List<String> sortedValues = pgSegment.stream()
                .sorted(Comparator.comparingInt(SORTING_ORDER::indexOf))
                .filter(Objects::nonNull)
                .toList();
        String firstPgSegment = sortedValues.get(0);
        sortedValues.forEach(segment -> {
            if (components.getSingleLineHolder().containsKey(segment)) {
                components.getGroupRepeatableLineHolder().computeIfAbsent(firstPgSegment, k -> new ArrayList<>())
                        .add(components.getSingleLineHolder().get(segment));
                components.getSingleLineHolder().remove(segment);
            } else if (components.getInnerListLineHolder().containsKey(segment)) {
                components.getGroupRepeatableLineHolder().computeIfAbsent(firstPgSegment, k -> new ArrayList<>())
                        .addAll(components.getInnerListLineHolder().get(segment));
                components.getInnerListLineHolder().remove(segment);
            }
        });
    }

    private ProcessResult processField(String field, String value,Components components) {
        String pgSegment = null;
        boolean isGroupRepeatable=false;
        try {
            if (!StringUtils.isBlank(value) && MAP_POSITIONS.containsKey(field)) {
                String[] mapping = MAP_POSITIONS.get(field).split("/");
                if (mapping.length == 7 && mapping[6].charAt(0) == 'F') {
                    value = value.replaceAll("[^a-zA-Z0-9]", "");
                }
                pgSegment = mapping[0];
                String repeatMode=mapping[5];
                int length = Integer.parseInt(mapping[1]);
                if (value.length() > length) {
                    throw new InvalidDataException("Length of field value '" + value + "' exceeds the maximum length for the key '" + field + "' ( expected " + length + " , actual " + value.length() + " )");
                }
                if (repeatMode.equals("G")) {
                    isGroupRepeatable=true;
                }
                int startPosition = Integer.parseInt(mapping[2]) - 1;
                char alignment = mapping[4].charAt(0);
                updateLine(value, pgSegment, alignment, length, startPosition,components);
            }
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Error parsing integer value in mapping for field " + field + ". Verify the mapping details for the key " + field + " provided.");
        }
        ProcessResult result=new ProcessResult();
        result.setGroupRepeatable(isGroupRepeatable);
        result.setPgSegment(pgSegment);
        return result;
    }

    private void updateLine(String fieldValue, String pgSegment, char alignment, int length, int startPosition,Components components) {
        components.getSingleLineHolder().computeIfAbsent(pgSegment, key -> {
            StringBuilder sb = new StringBuilder();
            sb.append(pgSegment);
            sb.append(" ".repeat(80 - pgSegment.length()));
            return sb;
        });
        StringBuilder currentLine = components.getSingleLineHolder().get(pgSegment);
        if (alignment == 'R') {
            fieldValue = String.format("%" + length + "s", fieldValue).replace(' ', '0');
        }
        currentLine.replace(startPosition, startPosition + fieldValue.length(), fieldValue);
        components.getSingleLineHolder().put(pgSegment, currentLine);
    }
}