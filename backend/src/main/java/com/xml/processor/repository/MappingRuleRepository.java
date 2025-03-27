package com.xml.processor.repository;

import com.xml.processor.model.MappingRule;
import com.xml.processor.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MappingRule entities.
 */
@Repository
public interface MappingRuleRepository extends JpaRepository<MappingRule, Long> {
    
    @Query("SELECT m FROM MappingRule m WHERE m.client.id = ?1 AND m.name = ?2")
    Optional<MappingRule> findByClient_IdAndName(Long clientId, String name);
    
    List<MappingRule> findByClient_IdAndIsActive(Long clientId, Boolean isActive);
    
    @Query("SELECT m FROM MappingRule m WHERE m.client.id = ?1 AND m.sourceField = ?2")
    List<MappingRule> findByClient_IdAndSourceField(Long clientId, String sourceField);
    
    @Query("SELECT m FROM MappingRule m WHERE m.client.id = ?1 AND m.targetField = ?2")
    List<MappingRule> findByClient_IdAndTargetField(Long clientId, String targetField);
    
    @Query("SELECT m FROM MappingRule m WHERE m.client.id = ?1 ORDER BY m.priority ASC")
    List<MappingRule> findByClient_IdOrderByPriority(Long clientId);
    
    @Query("SELECT m FROM MappingRule m WHERE m.client.id = ?1 AND m.required = true")
    List<MappingRule> findRequiredRulesByClient_Id(Long clientId);
    
    @Query("SELECT m FROM MappingRule m WHERE m.id = ?1 AND m.client.id = ?2")
    Optional<MappingRule> findByIdAndClient_Id(Long id, Long clientId);
    
    @Query("DELETE FROM MappingRule m WHERE m.client.id = ?1")
    void deleteByClient_Id(Long clientId);

    /**
     * Find all mapping rules for a specific client
     *
     * @param clientId The ID of the client
     * @return List of mapping rules belonging to the client
     */
    List<MappingRule> findByClient_Id(Long clientId);
    
    /**
     * Find all mapping rules for a specific client with pagination
     */
    @Query("SELECT m FROM MappingRule m WHERE m.client.id = ?1")
    Page<MappingRule> findByClient_Id(Long clientId, Pageable pageable);

    /**
     * Find mapping rules by client ID and interface ID
     */
    List<MappingRule> findByClient_IdAndInterfaceEntity_Id(Long clientId, Long interfaceId);
    
    /**
     * Find mapping rules by client ID and interface ID with pagination
     */
    @Query("SELECT m FROM MappingRule m WHERE m.client.id = ?1 AND m.interfaceEntity.id = ?2")
    Page<MappingRule> findByClient_IdAndInterfaceEntity_Id(Long clientId, Long interfaceId, Pageable pageable);

    List<MappingRule> findByInterfaceId(Long interfaceId);
    Page<MappingRule> findByInterfaceId(Long interfaceId, Pageable pageable);
    Page<MappingRule> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<MappingRule> findByIsActive(boolean isActive, Pageable pageable);
    List<MappingRule> findByInterfaceIdAndIsActiveTrue(Long interfaceId);
    List<MappingRule> findByInterfaceIdAndIsActive(Long interfaceId, boolean isActive);
    boolean existsByNameAndInterfaceId(String name, Long interfaceId);
    boolean existsByNameAndInterfaceIdAndIdNot(String name, Long interfaceId, Long id);

    @Query("SELECT m FROM MappingRule m WHERE m.tableName = ?1 AND m.client.id = ?2")
    List<MappingRule> findByTableNameAndClient_Id(String tableName, Long clientId);
    
    @Query("DELETE FROM MappingRule m WHERE m.client.id = ?1 AND m.tableName = ?2")
    void deleteByClient_IdAndTableName(Long clientId, String tableName);

    List<MappingRule> findByIsDefaultTrue();
    List<MappingRule> findByClient(Client client);

    List<MappingRule> findByTableName(String tableName);
    
    @Query("SELECT m FROM MappingRule m WHERE m.client.id = ?1 AND m.isActive = true")
    Page<MappingRule> findByClient_IdAndIsActiveTrue(Long clientId, Pageable pageable);

    @Query("SELECT m FROM MappingRule m WHERE m.name LIKE %?1% AND m.isActive = ?2")
    Page<MappingRule> findByNameContainingIgnoreCaseAndIsActive(String name, boolean isActive, Pageable pageable);

    /**
     * Delete all mapping rules for a specific interface
     *
     * @param interfaceId The ID of the interface
     */
    @Query("DELETE FROM MappingRule m WHERE m.interfaceEntity.id = ?1")
    void deleteByInterfaceId(Long interfaceId);
} 