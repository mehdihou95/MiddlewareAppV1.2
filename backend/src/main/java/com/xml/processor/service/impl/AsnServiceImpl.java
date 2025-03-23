package com.xml.processor.service.impl;

import com.xml.processor.config.ClientContextHolder;
import com.xml.processor.model.AsnHeader;
import com.xml.processor.model.AsnLine;
import com.xml.processor.repository.AsnHeaderRepository;
import com.xml.processor.repository.AsnLineRepository;
import com.xml.processor.service.interfaces.AsnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AsnServiceImpl implements AsnService {

    @Autowired
    private AsnHeaderRepository asnHeaderRepository;

    @Autowired
    private AsnLineRepository asnLineRepository;

    // ASN Header operations
    @Override
    @Transactional
    public AsnHeader createAsnHeader(AsnHeader header) {
        return asnHeaderRepository.save(header);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsnHeader> getAsnHeaderById(Long id) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return asnHeaderRepository.findByIdAndClient_Id(id, clientId);
        }
        return asnHeaderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnHeader> getAllAsnHeaders() {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return asnHeaderRepository.findByClient_Id(clientId);
        }
        return asnHeaderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnHeader> getAsnHeadersByClient_Id(Long clientId) {
        return asnHeaderRepository.findByClient_Id(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsnHeader> findByDocumentNumberAndClientId(String documentNumber, Long clientId) {
        return asnHeaderRepository.findByDocumentNumberAndClientId(documentNumber, clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnHeader> findByClient_IdAndShipmentDateBetween(Long clientId, LocalDate startDate, LocalDate endDate) {
        return asnHeaderRepository.findByClient_IdAndShipmentDateBetween(clientId, startDate, endDate);
    }

    @Override
    @Transactional
    public AsnHeader updateAsnHeader(Long id, AsnHeader headerDetails) {
        Long clientId = ClientContextHolder.getClientId();
        AsnHeader header;
        
        if (clientId != null) {
            header = asnHeaderRepository.findByIdAndClient_Id(id, clientId)
                .orElseThrow(() -> new RuntimeException("ASN Header not found with id: " + id));
        } else {
            header = asnHeaderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ASN Header not found with id: " + id));
        }

        // Update fields from headerDetails
        header.setDocumentNumber(headerDetails.getDocumentNumber());
        header.setDocumentType(headerDetails.getDocumentType());
        header.setSenderId(headerDetails.getSenderId());
        header.setReceiverId(headerDetails.getReceiverId());
        header.setDocumentDate(headerDetails.getDocumentDate());
        header.setDocumentTime(headerDetails.getDocumentTime());
        header.setStatus(headerDetails.getStatus());
        header.setNotes(headerDetails.getNotes());

        return asnHeaderRepository.save(header);
    }

    @Override
    @Transactional
    public void deleteAsnHeader(Long id) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            asnHeaderRepository.findByIdAndClient_Id(id, clientId)
                .ifPresent(header -> asnHeaderRepository.deleteById(id));
        } else {
            asnHeaderRepository.deleteById(id);
        }
    }

    // ASN Line operations
    @Override
    @Transactional
    public AsnLine createAsnLine(AsnLine line) {
        return asnLineRepository.save(line);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsnLine> getAsnLineById(Long id) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return asnLineRepository.findByIdAndClient_Id(id, clientId);
        }
        return asnLineRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnLine> getAllAsnLines() {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return asnLineRepository.findByClient_Id(clientId);
        }
        return asnLineRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnLine> getAsnLinesByHeaderId(Long headerId) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return asnLineRepository.findByClient_IdAndHeaderId(clientId, headerId);
        }
        
        // This is a simplified implementation; in a real system, you'd need to 
        // ensure proper security checks for non-client-specific access
        return asnLineRepository.findAll().stream()
            .filter(line -> line.getHeader() != null && line.getHeader().getId().equals(headerId))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnLine> getAsnLinesByClient_Id(Long clientId) {
        return asnLineRepository.findByClient_Id(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnLine> findByClient_IdAndHeaderId(Long clientId, Long headerId) {
        return asnLineRepository.findByClient_IdAndHeaderId(clientId, headerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnLine> findByClient_IdAndItemNumber(Long clientId, String itemNumber) {
        return asnLineRepository.findByClient_IdAndItemNumber(clientId, itemNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnLine> findByClient_IdAndLotNumber(Long clientId, String lotNumber) {
        return asnLineRepository.findByClient_IdAndLotNumber(clientId, lotNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnLine> findByClient_IdAndStatus(Long clientId, String status) {
        return asnLineRepository.findByClient_IdAndStatus(clientId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsnLine> findByClient_IdAndQuantityGreaterThan(Long clientId, Integer quantity) {
        return asnLineRepository.findByClient_IdAndQuantityGreaterThan(clientId, quantity);
    }

    @Override
    @Transactional
    public AsnLine updateAsnLine(Long id, AsnLine lineDetails) {
        Long clientId = ClientContextHolder.getClientId();
        AsnLine line;
        
        if (clientId != null) {
            line = asnLineRepository.findByIdAndClient_Id(id, clientId)
                .orElseThrow(() -> new RuntimeException("ASN Line not found with id: " + id));
        } else {
            line = asnLineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ASN Line not found with id: " + id));
        }

        // Update fields from lineDetails
        if (lineDetails.getLineNumber() != null) {
            line.setLineNumber(lineDetails.getLineNumber().toString());
        }
        line.setItemNumber(lineDetails.getItemNumber());
        line.setItemDescription(lineDetails.getItemDescription());
        line.setQuantity(lineDetails.getQuantity());
        line.setUnitOfMeasure(lineDetails.getUnitOfMeasure());
        line.setLotNumber(lineDetails.getLotNumber());
        line.setSerialNumber(lineDetails.getSerialNumber());
        line.setStatus(lineDetails.getStatus());
        line.setNotes(lineDetails.getNotes());

        // Only update header if it's explicitly set in lineDetails
        if (lineDetails.getHeader() != null) {
            line.setHeader(lineDetails.getHeader());
        }

        return asnLineRepository.save(line);
    }

    @Override
    @Transactional
    public void deleteAsnLine(Long id) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            asnLineRepository.findByIdAndClient_Id(id, clientId)
                .ifPresent(line -> asnLineRepository.deleteById(id));
        } else {
            asnLineRepository.deleteById(id);
        }
    }
} 