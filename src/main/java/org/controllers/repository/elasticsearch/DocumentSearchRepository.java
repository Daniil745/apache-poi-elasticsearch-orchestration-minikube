package org.controllers.repository.elasticsearch;

import org.controllers.model.elastic.DocumentIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentIndex, String> {
    List<DocumentIndex> findByFileType(String fileType);

    List<DocumentIndex> findByDocumentId(Long documentId);

    void deleteById(Long documentId);
}
