package msa.emaia.tools;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class JsonMapConverter implements AttributeConverter<Object, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object attribute) {

        if (attribute == null) {
            return null;
        }
        try {
            return  objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            if (dbData.startsWith("[") && dbData.endsWith("]")) {
                return objectMapper.readValue(dbData, new TypeReference<List<Object>>() {});
            }if (dbData.startsWith("{") && dbData.endsWith("}")) {
                return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
            }else {
                return objectMapper.readValue(dbData, new TypeReference<Object>() {});
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
