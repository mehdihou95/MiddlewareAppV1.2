package com.xml.processor.repository;

import com.xml.processor.model.AsnLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AsnLine entities.
 */
@Repository
public interface AsnLineRepository extends JpaRepository<AsnLine, Long> {
    
    Page<AsnLine> findByHeader_Id(Long headerId, Pageable pageable);
    
    Page<AsnLine> findByClient_Id(Long clientId, Pageable pageable);
    
    Page<AsnLine> findByClient_IdAndHeader_Id(Long clientId, Long headerId, Pageable pageable);
    
    Page<AsnLine> findByClient_IdAndItemNumber(Long clientId, String itemNumber, Pageable pageable);
    
    Page<AsnLine> findByClient_IdAndLotNumber(Long clientId, String lotNumber, Pageable pageable);
    
    Page<AsnLine> findByClient_IdAndStatus(Long clientId, String status, Pageable pageable);
    
    Page<AsnLine> findByClient_IdAndQuantityGreaterThan(Long clientId, Integer quantity, Pageable pageable);

    /**
     * Find ASN line by ID and client ID
     */
    Optional<AsnLine> findByIdAndClient_Id(Long id, Long clientId);

    /**
     * Find ASN lines by client ID
     */
    List<AsnLine> findByClient_Id(Long clientId);

    /**
     * Find ASN lines by client ID and header ID
     */
    List<AsnLine> findByClient_IdAndHeader_Id(Long clientId, Long headerId);

    /**
     * Find ASN lines by client ID and item number
     */
    List<AsnLine> findByClient_IdAndItemNumber(Long clientId, String itemNumber);

    /**
     * Find ASN lines by client ID and lot number
     */
    List<AsnLine> findByClient_IdAndLotNumber(Long clientId, String lotNumber);

    /**
     * Find ASN lines by client ID and status
     */
    List<AsnLine> findByClient_IdAndStatus(Long clientId, String status);

    /**
     * Find ASN lines by client ID and quantity greater than
     */
    List<AsnLine> findByClient_IdAndQuantityGreaterThan(Long clientId, Integer quantity);

    /**
     * Get all ASN lines with pagination
     */
    @Override
    Page<AsnLine> findAll(Pageable pageable);
} 