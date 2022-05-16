package com.microservices.demo.elastic.indexclient.util;

import com.microservices.demo.elastic.model.index.IndexModel;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticIndexUtil<T extends IndexModel> {

    /**
     * Convert list of index model objects (documents)
     * to a list of index queries
     * to be able to send them to ElasticSearch
     * @param documents index model objects
     */
    public List<IndexQuery> getIndexQueries(List<T> documents) {
        return documents.stream()
                .map(doc -> new IndexQueryBuilder()
                        .withId(doc.getId())
                        .withObject(doc)
                        .build())
                .collect(Collectors.toList());
    }
}
