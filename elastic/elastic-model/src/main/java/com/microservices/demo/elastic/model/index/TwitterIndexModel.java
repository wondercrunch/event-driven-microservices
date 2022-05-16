package com.microservices.demo.elastic.model.index;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder
@Document(indexName = "#{@elasticConfigData.indexName}") //SpEL template (indexName is parsed with SpEL Expression Parser)
public class TwitterIndexModel implements IndexModel {

    @JsonProperty
    private String id;

    @JsonProperty
    private Long userId;

    @JsonProperty
    private String text;

    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ssZZ")
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ssZZ") //to convert to ElasticSearch field
    private ZonedDateTime createdAt;
}
