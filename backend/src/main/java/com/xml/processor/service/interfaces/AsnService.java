package com.xml.processor.service.interfaces;
    
import com.xml.processor.model.AsnHeader;
import com.xml.processor.model.AsnLine;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
    
public interface AsnService {
    // ASN Header operations
    AsnHeader createAsnHeader(AsnHeader header);
    Optional<AsnHeader> getAsnHeaderById(Long id);
    List<AsnHeader> getAllAsnHeaders();
    List<AsnHeader> getAsnHeadersByClient_Id(Long clientId);
    Optional<AsnHeader> findByDocumentNumberAndClientId(String documentNumber, Long clientId);
    List<AsnHeader> findByClient_IdAndShipmentDateBetween(Long clientId, LocalDate startDate, LocalDate endDate);
    AsnHeader updateAsnHeader(Long id, AsnHeader header);
    void deleteAsnHeader(Long id);
    
    // ASN Line operations
    AsnLine createAsnLine(AsnLine line);
    Optional<AsnLine> getAsnLineById(Long id);
    List<AsnLine> getAllAsnLines();
    List<AsnLine> getAsnLinesByHeaderId(Long headerId);
    List<AsnLine> getAsnLinesByClient_Id(Long clientId);
    List<AsnLine> findByClient_IdAndHeaderId(Long clientId, Long headerId);
    List<AsnLine> findByClient_IdAndItemNumber(Long clientId, String itemNumber);
    List<AsnLine> findByClient_IdAndLotNumber(Long clientId, String lotNumber);
    List<AsnLine> findByClient_IdAndStatus(Long clientId, String status);
    List<AsnLine> findByClient_IdAndQuantityGreaterThan(Long clientId, Integer quantity);
    AsnLine updateAsnLine(Long id, AsnLine line);
    void deleteAsnLine(Long id);
} 