package org.controllers.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.controllers.model.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    List<DocumentEntity> findByFileExtension(String fileExtension);

    List<DocumentEntity> findByIndexedFalse();

    Optional<DocumentEntity> findByOriginalFilename(String originalFilename);

    boolean existsByStoredFilename(String storedFilename);

}
