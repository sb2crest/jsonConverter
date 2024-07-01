package com.converter.service;

import com.converter.exceptions.InvalidDataException;
import com.converter.initializers.Inilializer;
import com.converter.objects.Components;
import com.converter.objects.EdiRequest;
import com.converter.objects.EdiResponse;
import com.converter.objects.MappingInfo;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JsonToEdi {
    private static final Logger log = LoggerFactory.getLogger(JsonToEdi.class);
    private static final List<String> SEGMENT_ORDER = new ArrayList<>();
    private static final Map<String, String> FIELD_MAPPING_INFORMATIONS = new HashMap<>();
    private static final List<String> CRITICAL_FIELDS = new ArrayList<>();

    public JsonToEdi(String organization) {
        log.info("Feeding Information for organization {}",organization);
        feedMappingDetails(organization);
    }

    @Autowired
    public JsonToEdi() {
    }

    public EdiResponse convert(EdiRequest request) {
        feedMappingDetails("FDA");
        validateInput(request);

        Components components = new Components();
        try {
            String ediFile = map(request.getSubject(), components);
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

    private String map(JsonNode jsonData, Components components) {
        components.getFinalOutputFile().addAll(List.of("A0901SY2SECFIL111715     PE                                361634    361634     ", "B  0901ME0PE                                               CRIMSONTEST1         "));
        processJsonData(jsonData, components);
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
        if (SEGMENT_ORDER .isEmpty() || FIELD_MAPPING_INFORMATIONS.isEmpty()) {
            throw new InvalidDataException("Please Configure organization name in your configuration file");
        }
    }

    private static void feedMappingDetails(String agencyCode) {
        if (agencyCode.equals("FDA")) {
            MappingInfo mappingInfo = Inilializer.getFDA();
            SEGMENT_ORDER.addAll(mappingInfo.getSegmentOrder());
            FIELD_MAPPING_INFORMATIONS.putAll(mappingInfo.getFieldMappingInformations());
            CRITICAL_FIELDS.addAll(mappingInfo.getCriticalFields());
        } else {
            throw new InvalidDataException("No mapping information found for the entered Agency code " + agencyCode);
        }
    }

    private void sortLines(Components components) {
        List<String> sortedKeys = new ArrayList<>(components.getSingleLineHolder().keySet());
        sortedKeys.addAll(components.getGroupRepeatableLineHolder().keySet());
        sortedKeys = sortedKeys.stream().distinct().filter(key -> !key.isEmpty()).sorted(Comparator.comparingInt(SEGMENT_ORDER::indexOf)).collect(Collectors.toList());
        sortedKeys.forEach(entry -> {
            if (components.getSingleLineHolder().containsKey(entry)) {
                components.getAllEntries().add(components.getSingleLineHolder().get(entry));
            } else if (components.getGroupRepeatableLineHolder().containsKey(entry)) {
                components.getAllEntries().addAll(components.getGroupRepeatableLineHolder().get(entry));
            }
        });
    }

    private void processJsonData(JsonNode jsonData, Components components) {
        jsonData.fields().forEachRemaining(data -> {
            if (!data.getValue().isArray() && !data.getValue().isObject()) {
                String pgSegment = processField(data.getKey(), data.getValue().asText(), components);
                components.getKeySet().add(pgSegment);
            } else if (data.getValue().isArray()) {
                processRootArray(data.getValue(), components);
            }
        });
    }

    private void processRootArray(JsonNode jsonNode, Components components) {
        jsonNode.forEach(element -> {
            if (element.isObject()) {
                processObject(element, components);
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

    private void processArray(JsonNode arrayNode, Components components) {
        arrayNode.forEach(element -> {
            if (element.isObject()) {
                Set<String> pgSegment = processObject(element, components);
                transferLineToGroupRepeatableLineHolder(pgSegment, components);
            } else if (element.isArray()) {
                processArray(element, components);
            }
        });
    }

    private Set<String> processInnerArray(JsonNode arrayNode, Components components) {
        Set<String> pgSegmentList = new HashSet<>();
        arrayNode.forEach(element -> {
            if (element.isObject()) {
                Set<String> pgSegment = processCFObject(element, components);
                transferLinesToInnerListLineHolder(pgSegment, components);
                pgSegmentList.addAll(pgSegment);
            } else if (element.isArray()) {
                processInnerArray(element, components);
            }
        });
        return pgSegmentList;
    }

    private void processCFArray(JsonNode arrayNode, Components components) {
        arrayNode.forEach(element -> {
            if (element.isObject()) {
                Set<String> pgSegment = processCFObject(element, components);
                transferLineToGroupRepeatableLineHolder(pgSegment, components);
            } else if (element.isArray()) {
                processInnerArray(element, components);
            }
        });
    }

    private Set<String> processObject(JsonNode element, Components components) {
        Set<String> pgSegmentTracker = new HashSet<>();
        element.fields().forEachRemaining(field -> {
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            if (CRITICAL_FIELDS.contains(fieldName)) {
                executeCriticalField(fieldValue, components);
            }else if (!fieldValue.isArray() && !fieldValue.isObject()) {
                String pgSegment = processField(fieldName, fieldValue.asText(), components);
                pgSegmentTracker.add(pgSegment);
            } else if (fieldValue.isArray()) {
                processArray(fieldValue, components);
            } else if (fieldValue.isObject()) {
                Set<String> pgSegment = processObject(fieldValue, components);
                transferLineToGroupRepeatableLineHolder(pgSegment, components);
            }
        });
        return pgSegmentTracker;
    }
    private void executeCriticalField(JsonNode data,Components components){
        if(data.isObject()){
            Set<String> pgSegments = processCFObject(data, components);
            transferLineToGroupRepeatableLineHolder(pgSegments, components);
        }else if(data.isArray()){
            processCFArray(data, components);
        }
    }

    private Set<String> processCFObject(JsonNode element, Components components) {
        Set<String> pgSegmentTracker = new HashSet<>();
        element.fields().forEachRemaining(field -> {
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            if (!fieldValue.isArray() && !fieldValue.isObject()) {
                String pgSegment = processField(fieldName, fieldValue.asText(), components);
                pgSegmentTracker.add(pgSegment);
            } else if (fieldValue.isArray()) {
                Set<String> segments = processInnerArray(fieldValue, components);
                pgSegmentTracker.addAll(segments);
                transferLineToTemporaryLineHolder(pgSegmentTracker,components);
            } else if (fieldValue.isObject()) {
                Set<String> pgSegment = processCFObject(fieldValue, components);
                pgSegmentTracker.addAll(pgSegment);
            }
        });
        return pgSegmentTracker;
    }

    private void transferLinesToInnerListLineHolder(Set<String> pgSegments, Components components){
        if(pgSegments.isEmpty())
            return;
        List<String> sortedList=getSortingOrder(pgSegments);
        String firstSegment=sortedList.get(0);
        sortedList.forEach(segment->{
            if(components.getSingleLineHolder().containsKey(segment)){
                components.getInnerListLineHolder().computeIfAbsent(firstSegment,k->new ArrayList<>())
                        .add(components.getSingleLineHolder().get(segment));
                components.getSingleLineHolder().remove(segment);
            }
        });
    }
    private void transferLineToTemporaryLineHolder(Set<String> pgSegments, Components components){
         if(pgSegments.isEmpty())
            return;
        List<String> sortedList=getSortingOrder(pgSegments);
        String firstSegment=sortedList.get(0);
        sortedList.forEach(segment->{
            if (components.getInnerListLineHolder().containsKey(segment)) {
                components.getTemporaryLineHolder().computeIfAbsent(firstSegment,k->new ArrayList<>())
                        .addAll(components.getInnerListLineHolder().get(segment));
                components.getInnerListLineHolder().remove(segment);
            }
            if(components.getSingleLineHolder().containsKey(segment)){
                components.getTemporaryLineHolder().computeIfAbsent(firstSegment,k->new ArrayList<>())
                        .add(components.getSingleLineHolder().get(segment));
                components.getSingleLineHolder().remove(segment);
            }
        });
    }
    private void transferLineToGroupRepeatableLineHolder(Set<String> pgSegment, Components components) {
        if (pgSegment.isEmpty())
            return;
        List<String> sortedValues = getSortingOrder(pgSegment);
        String firstPgSegment = sortedValues.get(0);
        sortedValues.forEach(segment -> {
            if (components.getSingleLineHolder().containsKey(segment)) {
                components.getGroupRepeatableLineHolder().computeIfAbsent(firstPgSegment, k -> new ArrayList<>())
                        .add(components.getSingleLineHolder().get(segment));
                components.getSingleLineHolder().remove(segment);
            } else if (components.getTemporaryLineHolder().containsKey(segment)) {
                components.getGroupRepeatableLineHolder().computeIfAbsent(firstPgSegment, k -> new ArrayList<>())
                        .addAll(components.getTemporaryLineHolder().get(segment));
                components.getTemporaryLineHolder().remove(segment);
            }
        });
    }

    private List<String> getSortingOrder(Collection<String> segments) {
        return segments.stream()
                .sorted(Comparator.comparingInt(SEGMENT_ORDER::indexOf))
                .filter(Objects::nonNull)
                .toList();
    }

    private String processField(String field, String value, Components components) {
        String pgSegment = null;
        try {
            if (!StringUtils.isBlank(value) && FIELD_MAPPING_INFORMATIONS.containsKey(field)) {
                String[] mapping = FIELD_MAPPING_INFORMATIONS.get(field).split("/");
                if (mapping.length == 7 && mapping[6].charAt(0) == 'F') {
                    value = value.replaceAll("[^a-zA-Z0-9]", "");
                }
                pgSegment = mapping[0];
                int length = Integer.parseInt(mapping[1]);
                if (value.length() > length) {
                    throw new InvalidDataException("Length of field value '" + value + "' exceeds the maximum length for the key '" + field + "' ( expected " + length + " , actual " + value.length() + " )");
                }
                int startPosition = Integer.parseInt(mapping[2]) - 1;
                char alignment = mapping[4].charAt(0);
                updateLine(value, pgSegment, alignment, length, startPosition, components);
            }
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Error parsing integer value in mapping for field " + field + ". Verify the mapping details for the key " + field + " provided.");
        }
        return pgSegment;
    }

    private void updateLine(String fieldValue, String pgSegment, char alignment, int length,
                            int startPosition, Components components) {
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