package com.microservices.demo.elastic.indexclient.service;

import com.microservices.demo.elastic.indexclient.repository.TwitterElasticsearchIndexRepository;
import com.microservices.demo.elastic.model.index.TwitterIndexModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


//@Primary //primary implementation
@ConditionalOnProperty(name="elastic-config.is-repository", havingValue = "true", matchIfMissing = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class TwitterElasticRepositoryIndexClient implements ElasticIndexClient<TwitterIndexModel> {


    private final TwitterElasticsearchIndexRepository repository;

    /**
     * Index (insert into elasticsearch) list of twitter index model objects (documents)
     * using TwitterElasticsearchIndexRepository
     * @param documents twitter index model objects
     * @return list of indexed documents ids
     */
    @Override
    public List<String> save(List<TwitterIndexModel> documents) {
        List<TwitterIndexModel> repositoryResponse = (List<TwitterIndexModel>) repository.saveAll(documents);
        List<String> documentIds = repositoryResponse.stream()
                .map(TwitterIndexModel::getId)
                .collect(Collectors.toList());

        log.info("Documents indexed successfully with type: {} and ids: {}", TwitterIndexModel.class.getName(),
                documentIds);

        return documentIds;
    }
}
