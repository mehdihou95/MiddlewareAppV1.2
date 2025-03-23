package com.xml.processor.service;

import com.xml.processor.model.Client;
import com.xml.processor.model.AsnHeader;
import com.xml.processor.model.AsnLine;
import com.xml.processor.model.ProcessedFile;
import com.xml.processor.model.MappingRule;
import com.xml.processor.repository.ClientRepository;
import com.xml.processor.repository.AsnHeaderRepository;
import com.xml.processor.repository.AsnLineRepository;
import com.xml.processor.repository.ProcessedFileRepository;
import com.xml.processor.repository.MappingRuleRepository;
import com.xml.processor.service.interfaces.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MultiTenantServiceTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private AsnHeaderRepository asnHeaderRepository;

    @Autowired
    private AsnLineRepository asnLineRepository;

    @Autowired
    private ProcessedFileRepository processedFileRepository;

    @Autowired
    private MappingRuleRepository mappingRuleRepository;

    private Client client1;
    private Client client2;

    @BeforeEach
    void setUp() {
        // Create test clients
        client1 = new Client();
        client1.setName("TEST_CLIENT_1");
        client1.setDescription("Test Client 1");
        client1.setStatus(Client.ClientStatus.ACTIVE);
        client1 = clientService.saveClient(client1);

        client2 = new Client();
        client2.setName("TEST_CLIENT_2");
        client2.setDescription("Test Client 2");
        client2.setStatus(Client.ClientStatus.ACTIVE);
        client2 = clientService.saveClient(client2);
    }

    @Test
    void testClientIsolation() {
        // Create test data for client1
        AsnHeader header1 = new AsnHeader();
        header1.setClient(client1);
        header1.setDocumentNumber("DOC001");
        asnHeaderRepository.save(header1);

        // Create test data for client2
        AsnHeader header2 = new AsnHeader();
        header2.setClient(client2);
        header2.setDocumentNumber("DOC002");
        asnHeaderRepository.save(header2);

        // Verify data isolation
        List<AsnHeader> client1Headers = asnHeaderRepository.findByClient_Id(client1.getId());
        List<AsnHeader> client2Headers = asnHeaderRepository.findByClient_Id(client2.getId());

        assertEquals(1, client1Headers.size());
        assertEquals(1, client2Headers.size());
        assertEquals("DOC001", client1Headers.get(0).getDocumentNumber());
        assertEquals("DOC002", client2Headers.get(0).getDocumentNumber());
    }

    @Test
    void testCrossClientAccess() {
        // Create test data for client1
        ProcessedFile file1 = new ProcessedFile();
        file1.setClient(client1);
        file1.setFileName("test1.xml");
        processedFileRepository.save(file1);

        // Attempt to access client1's data through client2's context
        List<ProcessedFile> client2Files = processedFileRepository.findByClient_Id(client2.getId());
        assertTrue(client2Files.isEmpty());
    }

    @Test
    void testClientDeletion() {
        // Create test data for client1
        MappingRule rule1 = new MappingRule();
        rule1.setClient(client1);
        rule1.setName("Test Rule 1");
        mappingRuleRepository.save(rule1);

        // Delete client1
        clientService.deleteClient(client1.getId());

        // Verify data is deleted
        List<MappingRule> client1Rules = mappingRuleRepository.findByClient_Id(client1.getId());
        assertTrue(client1Rules.isEmpty());
    }

    @Test
    void testClientStatusChange() {
        // Create test data for client1
        AsnLine line1 = new AsnLine();
        line1.setClient(client1);
        line1.setLineNumber("LINE001");
        asnLineRepository.save(line1);

        // Suspend client1
        client1.setStatus(Client.ClientStatus.SUSPENDED);
        client1 = clientService.saveClient(client1);

        // Verify data is still accessible but client is suspended
        List<AsnLine> client1Lines = asnLineRepository.findByClient_Id(client1.getId());
        assertEquals(1, client1Lines.size());
        assertEquals(Client.ClientStatus.SUSPENDED, client1.getStatus());
    }
} 