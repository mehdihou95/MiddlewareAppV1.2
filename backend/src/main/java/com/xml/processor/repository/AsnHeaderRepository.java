package com.xml.processor.repository;

import com.xml.processor.model.AsnHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AsnHeader entities.
 */
@Repository
public interface AsnHeaderRepository extends JpaRepository<AsnHeader, Long> {
    
    @Query("SELECT h FROM AsnHeader h WHERE h.client.id = ?1")
    Page<AsnHeader> findByClient_Id(Long clientId, Pageable pageable);
    
    @Query("SELECT h FROM AsnHeader h WHERE h.documentNumber = ?1 AND h.client.id = ?2")
    Optional<AsnHeader> findByDocumentNumberAndClient_Id(String documentNumber, Long clientId);
    
    @Query("SELECT h FROM AsnHeader h WHERE h.client.id = ?1 AND h.documentDate BETWEEN ?2 AND ?3")
    Page<AsnHeader> findByClient_IdAndDocumentDateBetween(Long clientId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    List<AsnHeader> findByClient_IdAndStatus(Long clientId, String status);
    
    @Query("SELECT h FROM AsnHeader h WHERE h.client.id = ?1 AND h.documentDate BETWEEN ?2 AND ?3")
    List<AsnHeader> findByClient_IdAndDocumentDateBetween(Long clientId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT h FROM AsnHeader h WHERE h.client.id = ?1 ORDER BY h.createdAt DESC")
    List<AsnHeader> findLatestHeaders(Long clientId);

    /**
     * Find all ASN headers for a specific client
     *
     * @param clientId The ID of the client
     * @return List of ASN headers belonging to the client
     */
    List<AsnHeader> findByClient_Id(Long clientId);

    /**
     * Find ASN headers by client ID and document date
     */
    List<AsnHeader> findByClient_IdAndDocumentDate(Long clientId, String documentDate);

    /**
     * Find ASN header by ID and client ID
     */
    Optional<AsnHeader> findByIdAndClient_Id(Long id, Long clientId);

    /**
     * Get all ASN headers with pagination
     */
    @Override
    Page<AsnHeader> findAll(Pageable pageable);
} 