package com.microservices.demo.elastic.indexclient.service;

import com.microservices.demo.config.ElasticConfigData;
import com.microservices.demo.elastic.indexclient.util.ElasticIndexUtil;
import com.microservices.demo.elastic.model.index.TwitterIndexModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name="elastic-config.is-repository", havingValue = "false")
@Slf4j
@RequiredArgsConstructor
public class TwitterElasticIndexClient implements ElasticIndexClient<TwitterIndexModel> {

    private final ElasticConfigData elasticConfigData;

    private final ElasticsearchOperations elasticsearchOperations; //allows working with low level queries

    private final ElasticIndexUtil<TwitterIndexModel> elasticIndexUtil;

    /**
     * Index (insert into elasticsearch) list of twitter index model objects (documents)
     * using ElasticsearchOperations and ElasticIndexUtil
     * @param documents twitter index model objects
     * @return list of indexed documents ids
     */
    @Override
    public List<String> save(List<TwitterIndexModel> documents) {
        List<IndexQuery> indexQueries = elasticIndexUtil.getIndexQueries(documents);
        List<String> documentIds = elasticsearchOperations.bulkIndex(
                indexQueries,
                IndexCoordinates.of(elasticConfigData.getIndexName())
        ).stream().map(IndexedObjectInformation::getId)
                .collect(Collectors.toList());

        log.info("Documents indexed successfully with type: {} and ids: {}", TwitterIndexModel.class.getName(),
                documentIds);

        return documentIds;
    }
}
