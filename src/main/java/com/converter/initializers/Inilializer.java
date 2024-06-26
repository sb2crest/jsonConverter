package com.converter.initializers;

import com.converter.objects.MappingInfo;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Inilializer {
    public static MappingInfo getFDA() {
        /*
          recordType/length/startPosition/endPosition/justification/repeat-mode/formating-options(F/AI);
          F -> This option will remove all the special characters from the value
          AI -> Auto increment ( use -  for the starting position ) -under construction
          **/
        List<String> segmentOrder = Arrays.asList("PE10","PE15","OI","PG01", "PG02", "PG04", "PG06", "PG07", "PG10", "PG13", "PG14", "PG19", "PG20", "PG21", "PG23", "PG24", "PG25", "PG26", "PG27", "PG28", "PG30", "PG60", "PG00","PE90");
        List<String> criticalFields =Arrays.asList("partyDetails","tradeOrBrandNameInfo");
        Map<String, String> fieldMappingInfo = new HashMap<>();
        fieldMappingInfo.put("actionCode","PE10/1/5/5/L/N");
        fieldMappingInfo.put("filingType","PE10/1/6/6/L/N");
        fieldMappingInfo.put("referenceQualifierCode","PE10/3/7/9/L/N");
        fieldMappingInfo.put("issuerCodeForReferenceIdentifier","PE10/4/10/13/L/N");
        fieldMappingInfo.put("referenceIdentifierNo","PE10/36/14/49/L/N");
        fieldMappingInfo.put("filerDefinedReferenceNo","PE10/9/50/58/L/N");
        fieldMappingInfo.put("billTypeIndicator","PE10/1/60/60/L/N");
        fieldMappingInfo.put("carrier","PE10/4/61/64/L/N");
        fieldMappingInfo.put("entryType","PE10/2/65/66/L/N");
        fieldMappingInfo.put("modeOfTransportation","PE10/2/67/68/L/N");
        fieldMappingInfo.put("envelopeNumber","PE10/12/69/80/L/N");
        fieldMappingInfo.put("billTypeIndicatorPE15","PE15/1/5/5/L/I");
        fieldMappingInfo.put("issuerCodeOfBillOfLadingNumber","PE15/4/6/9/L/I");
        fieldMappingInfo.put("billOfLadingNumber","PE15/50/10/59/L/I");
        fieldMappingInfo.put("priorNoticeConfirmationNumber","PE15/21/60/80/L/I");
        fieldMappingInfo.put("narrativeMessageTypeCode","PE90/2/5/6/R/I");
        fieldMappingInfo.put("narrativeMessageIdentifier","PE90/3/7/9/L/I");
        fieldMappingInfo.put("narrativeMessage","PE90/40/10/49/L/I");
        fieldMappingInfo.put("outputEnvelopNumber","PE90/20/50/69/L/I");

        fieldMappingInfo.put("commercialDesc","OI/70/11/80/L/N");
        fieldMappingInfo.put("pgaLineNumber", "PG01/3/5/7/L/N/AI-000");
        fieldMappingInfo.put("governmentAgencyCode", "PG01/3/8/10/L/N");
        fieldMappingInfo.put("governmentAgencyProgramCode", "PG01/3/11/13/L/N");
        fieldMappingInfo.put("governmentAgencyProcessingCode", "PG01/3/14/16/L/N");
        fieldMappingInfo.put("intendedUseCode", "PG01/16/42/57/L/N");
        fieldMappingInfo.put("intendedUseDescription", "PG01/21/58/78/L/N");
        fieldMappingInfo.put("correctionIndicator", "PG01/1/79/79/L/N");
        fieldMappingInfo.put("disclaimer", "PG01/1/80/80/L/N");
        fieldMappingInfo.put("itemType", "PG02/1/5/5/L/N");
        fieldMappingInfo.put("productCodeQualifier", "PG02/4/6/9/L/N");
        fieldMappingInfo.put("productCodeNumber", "PG02/19/10/28/L/N");
        fieldMappingInfo.put("constituentActiveIngredientQualifier", "PG04/1/5/5/L/I");
        fieldMappingInfo.put("constituentElementName", "PG04/51/6/56/L/I");
        fieldMappingInfo.put("constituentElementQuantity", "PG04/12/57/68/R/I");
        fieldMappingInfo.put("constituentElementUnitOfMeasure", "PG04/5/69/73/L/I");
        fieldMappingInfo.put("percentOfConstituentElement", "PG04/7/74/80/R/I");
        fieldMappingInfo.put("sourceTypeCode", "PG06/3/5/7/L/I");
        fieldMappingInfo.put("countryCode", "PG06/2/8/9/L/I");
        fieldMappingInfo.put("tradeOrBrandName", "PG07/35/5/39/L/N");
        fieldMappingInfo.put("commodityDesc", "PG10/57/24/80/L/N");
        fieldMappingInfo.put("issuerOfLPCO", "PG13/35/5/39/L/N");
        fieldMappingInfo.put("governmentGeographicCodeQualifier", "PG13/3/40/42/L/N");
        fieldMappingInfo.put("locationOfIssuerOfTheLPCO", "PG13/3/43/45/L/N");
        fieldMappingInfo.put("issuingAgencyLocation", "PG13/25/46/70/L/N");
        fieldMappingInfo.put("transactionType", "PG14/1/5/5/L/N");
        fieldMappingInfo.put("lpcoOrCodeType", "PG14/3/6/8/L/N");
        fieldMappingInfo.put("lpcoOrPncNumber", "PG14/33/9/41/L/N");
        fieldMappingInfo.put("partyType", "PG19/3/5/7/L/G");
        fieldMappingInfo.put("partyIdentifierType", "PG19/3/8/10/L/G");
        fieldMappingInfo.put("partyIdentifierNumber", "PG19/15/11/25/L/G");
        fieldMappingInfo.put("partyName", "PG19/32/26/57/L/G");
        fieldMappingInfo.put("address1", "PG19/23/58/80/L/G");
        fieldMappingInfo.put("address2", "PG20/32/5/36/L/G");
        fieldMappingInfo.put("apartmentOrSuiteNo", "PG20/5/37/41/L/G");
        fieldMappingInfo.put("city", "PG20/21/42/62/L/G");
        fieldMappingInfo.put("stateOrProvince", "PG20/3/63/65/L/G");
        fieldMappingInfo.put("country", "PG20/2/66/67/L/G");
        fieldMappingInfo.put("postalCode", "PG20/9/68/76/L/G");
        fieldMappingInfo.put("individualQualifier", "PG21/3/5/7/L/G");
        fieldMappingInfo.put("contactPerson", "PG21/23/8/30/L/G");
        fieldMappingInfo.put("telephoneNumber", "PG21/15/31/45/L/G");
        fieldMappingInfo.put("email", "PG21/35/46/80/L/G");
        fieldMappingInfo.put("affirmationComplianceCode", "PG23/5/5/9/L/I");
        fieldMappingInfo.put("affirmationComplianceQualifier", "PG23/30/10/39/L/I");
        fieldMappingInfo.put("remarksTypeCode", "PG24/3/5/7/L/N");
        fieldMappingInfo.put("remarksText", "PG24/68/13/80/L/N");
        fieldMappingInfo.put("temperatureQualifier", "PG25/1/5/5/L/I");
        fieldMappingInfo.put("degreeType","PG25/1/6/6/L/I");
        fieldMappingInfo.put("negativeNumber","PG25/1/7/7/L/I");
        fieldMappingInfo.put("actualTemperature","PG25/6/8/13/R/I");
        fieldMappingInfo.put("locationOfTemperatureRecording","PG25/1/14/14/L/I");
        fieldMappingInfo.put("lotNumberQualifier", "PG25/1/15/15/L/I");
        fieldMappingInfo.put("lotNumber", "PG25/25/16/40/L/I");
        fieldMappingInfo.put("productionStartDate","PG25/8/41/48/L/I/F");
        fieldMappingInfo.put("productionEndDate","PG25/8/49/56/L/I/F");
        fieldMappingInfo.put("pgaLineValue", "PG25/12/57/68/R/I");
        fieldMappingInfo.put("packagingQualifier", "PG26/1/5/5/R/I");
        fieldMappingInfo.put("quantity", "PG26/12/6/17/R/I");
        fieldMappingInfo.put("uom", "PG26/5/18/22/L/I");
        fieldMappingInfo.put("containerNumberOne", "PG27/20/5/24/L/I");
        fieldMappingInfo.put("containerNumberTwo", "PG27/20/28/47/L/I");
        fieldMappingInfo.put("containerNumberThree", "PG27/20/51/70/L/I");
        fieldMappingInfo.put("containerDimensionsOne", "PG28/4/5/8/L/N");
        fieldMappingInfo.put("containerDimensionsTwo", "PG28/4/9/12/L/N");
        fieldMappingInfo.put("containerDimensionsThree", "PG28/4/13/16/L/N");
        fieldMappingInfo.put("packageTrackingNumberCode", "PG28/4/17/20/L/N");
        fieldMappingInfo.put("packageTrackingNumber", "PG28/50/21/70/L/N");
        fieldMappingInfo.put("anticipatedArrivalInformation", "PG30/1/5/5/L/I");
        fieldMappingInfo.put("anticipatedArrivalDate", "PG30/8/6/13/L/I/F");
        fieldMappingInfo.put("anticipatedArrivalTime", "PG30/4/14/17/L/I");
        fieldMappingInfo.put("inspectionOrArrivalLocationCode", "PG30/4/18/21/L/I");
        fieldMappingInfo.put("inspectionOrArrivalLocation", "PG30/50/22/71/L/I");
        fieldMappingInfo.put("additionalInformationQualifierCode", "PG60/3/5/7/L/G");
        fieldMappingInfo.put("additionalInformation", "PG60/73/8/80/L/G");
        fieldMappingInfo.put("substitutionIndicator", "PG00/1/5/5/L/N");
        fieldMappingInfo.put("substitutionNumber", "PG00/4/6/9/L/N");


        MappingInfo mappingInfo=new MappingInfo();
        mappingInfo.setSegmentOrder(segmentOrder);
        mappingInfo.setFieldMappingInformations(fieldMappingInfo);
        mappingInfo.setCriticalFields(criticalFields);
        return mappingInfo;
    }
}
