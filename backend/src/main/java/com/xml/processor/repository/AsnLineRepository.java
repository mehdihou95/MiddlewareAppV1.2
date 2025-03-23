package com.xml.processor.repository;

import com.xml.processor.model.AsnLine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsnLineRepository extends BaseRepository<AsnLine> {
    
    /**
     * Find all ASN lines for a specific client
     *
     * @param clientId The ID of the client
     * @return List of ASN lines belonging to the client
     */
    List<AsnLine> findByClient_Id(Long clientId);
    
    @Query("SELECT l FROM AsnLine l WHERE l.client.id = ?1 AND l.header.id = ?2")
    List<AsnLine> findByClient_IdAndHeaderId(Long clientId, Long headerId);
    
    @Query("SELECT l FROM AsnLine l WHERE l.client.id = ?1 AND l.itemNumber = ?2")
    List<AsnLine> findByClient_IdAndItemNumber(Long clientId, String itemNumber);
    
    @Query("SELECT l FROM AsnLine l WHERE l.client.id = ?1 AND l.lotNumber = ?2")
    List<AsnLine> findByClient_IdAndLotNumber(Long clientId, String lotNumber);
    
    List<AsnLine> findByClient_IdAndStatus(Long clientId, String status);
    
    @Query("SELECT l FROM AsnLine l WHERE l.client.id = ?1 AND l.quantity > ?2")
    List<AsnLine> findByClient_IdAndQuantityGreaterThan(Long clientId, Integer quantity);
} 