package com.converter.objects;

import java.util.List;
import java.util.Map;

public class MappingInfo {
    private List<String> segmentOrder;
    private Map<String, String> fieldMappingInformations;
    private List<String> criticalFields;

    public List<String> getSegmentOrder() {
        return segmentOrder;
    }

    public void setSegmentOrder(List<String> segmentOrder) {
        this.segmentOrder = segmentOrder;
    }

    public Map<String, String> getFieldMappingInformations() {
        return fieldMappingInformations;
    }

    public void setFieldMappingInformations(Map<String, String> fieldMappingInformations) {
        this.fieldMappingInformations = fieldMappingInformations;
    }

    public List<String> getCriticalFields() {
        return criticalFields;
    }

    public void setCriticalFields(List<String> criticalFields) {
        this.criticalFields = criticalFields;
    }
}
