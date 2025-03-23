package com.xml.processor.repository;

import com.xml.processor.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    List<T> findByClient_Id(Long clientId);
    Optional<T> findByIdAndClient_Id(Long id, Long clientId);
    void deleteByClient_Id(Long clientId);
    boolean existsByClient_IdAndId(Long clientId, Long id);
} 