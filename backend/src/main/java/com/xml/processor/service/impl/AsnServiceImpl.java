package com.xml.processor.service.impl;

import com.xml.processor.config.ClientContextHolder;
import com.xml.processor.model.AsnHeader;
import com.xml.processor.model.AsnLine;
import com.xml.processor.repository.AsnHeaderRepository;
import com.xml.processor.repository.AsnLineRepository;
import com.xml.processor.service.interfaces.AsnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<AsnHeader> getAllAsnHeaders(Pageable pageable) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return asnHeaderRepository.findByClient_Id(clientId, pageable);
        }
        return asnHeaderRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsnHeader> getAsnHeadersByClient_Id(Long clientId, Pageable pageable) {
        return asnHeaderRepository.findByClient_Id(clientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AsnHeader> findByDocumentNumberAndClient_Id(String documentNumber, Long clientId) {
        return asnHeaderRepository.findByDocumentNumberAndClient_Id(documentNumber, clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsnHeader> findByClient_IdAndShipmentDateBetween(Long clientId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return asnHeaderRepository.findByClient_IdAndDocumentDateBetween(clientId, startDate, endDate, pageable);
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
    public Page<AsnLine> getAllAsnLines(Pageable pageable) {
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            return asnLineRepository.findByClient_Id(clientId, pageable);
        }
        return asnLineRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsnLine> getAsnLinesByHeader_Id(Long headerId, Pageable pageable) {
        return asnLineRepository.findByHeader_Id(headerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsnLine> getAsnLinesByClient_Id(Long clientId, Pageable pageable) {
        return asnLineRepository.findByClient_Id(clientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsnLine> findByClient_IdAndHeader_Id(Long clientId, Long headerId, Pageable pageable) {
        return asnLineRepository.findByClient_IdAndHeader_Id(clientId, headerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsnLine> findByClient_IdAndItemNumber(Long clientId, String itemNumber, Pageable pageable) {
        return asnLineRepository.findByClient_IdAndItemNumber(clientId, itemNumber, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsnLine> findByClient_IdAndLotNumber(Long clientId, String lotNumber, Pageable pageable) {
        return asnLineRepository.findByClient_IdAndLotNumber(clientId, lotNumber, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsnLine> findByClient_IdAndStatus(Long clientId, String status, Pageable pageable) {
        return asnLineRepository.findByClient_IdAndStatus(clientId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AsnLine> findByClient_IdAndQuantityGreaterThan(Long clientId, Integer quantity, Pageable pageable) {
        return asnLineRepository.findByClient_IdAndQuantityGreaterThan(clientId, quantity, pageable);
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