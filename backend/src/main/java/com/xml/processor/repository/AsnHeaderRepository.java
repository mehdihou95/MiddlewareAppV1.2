package com.xml.processor.repository;

import com.xml.processor.model.AsnHeader;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsnHeaderRepository extends BaseRepository<AsnHeader> {
    
    @Query("SELECT h FROM AsnHeader h WHERE h.client.id = ?1 AND h.documentNumber = ?2")
    Optional<AsnHeader> findByDocumentNumberAndClientId(String documentNumber, Long clientId);
    
    List<AsnHeader> findByClient_IdAndStatus(Long clientId, String status);
    
    @Query("SELECT h FROM AsnHeader h WHERE h.client.id = ?1 AND h.documentDate BETWEEN ?2 AND ?3")
    List<AsnHeader> findByClient_IdAndShipmentDateBetween(Long clientId, LocalDate startDate, LocalDate endDate);
    
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
} 