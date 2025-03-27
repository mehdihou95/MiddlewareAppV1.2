package com.xml.processor.service;

import com.xml.processor.model.Client;
import com.xml.processor.model.ClientStatus;
import com.xml.processor.model.AsnHeader;
import com.xml.processor.model.AsnLine;
import com.xml.processor.model.ProcessedFile;
import com.xml.processor.model.MappingRule;
import com.xml.processor.model.Interface;
import com.xml.processor.repository.ClientRepository;
import com.xml.processor.repository.AsnHeaderRepository;
import com.xml.processor.repository.AsnLineRepository;
import com.xml.processor.repository.ProcessedFileRepository;
import com.xml.processor.repository.MappingRuleRepository;
import com.xml.processor.repository.InterfaceRepository;
import com.xml.processor.service.interfaces.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    private InterfaceRepository interfaceRepository;

    private Client client1;
    private Client client2;

    @BeforeEach
    void setUp() {
        // Create test clients
        client1 = new Client();
        client1.setName("TEST_CLIENT_1");
        client1.setCode("TC1");
        client1.setDescription("Test Client 1");
        client1.setStatus(ClientStatus.ACTIVE);
        client1 = clientService.saveClient(client1);

        client2 = new Client();
        client2.setName("TEST_CLIENT_2");
        client2.setCode("TC2");
        client2.setDescription("Test Client 2");
        client2.setStatus(ClientStatus.ACTIVE);
        client2 = clientService.saveClient(client2);
    }

    @Test
    void testClientIsolation() {
        // Create test data for client1
        AsnHeader header1 = new AsnHeader();
        header1.setClient(client1);
        header1.setDocumentNumber("DOC001");
        header1.setDocumentDate(LocalDateTime.now().toString());
        header1.setStatus("ACTIVE");
        asnHeaderRepository.save(header1);

        // Create test data for client2
        AsnHeader header2 = new AsnHeader();
        header2.setClient(client2);
        header2.setDocumentNumber("DOC002");
        header2.setDocumentDate(LocalDateTime.now().toString());
        header2.setStatus("ACTIVE");
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
    public void testCrossClientAccess() {
        // Create an interface for client1
        Interface interface1 = new Interface();
        interface1.setName("TEST_INTERFACE_1");
        interface1.setType("XML");
        interface1.setActive(true);
        interface1.setClient(client1);
        interface1.setRootElement("root");
        interfaceRepository.save(interface1);

        // Create a processed file for client1
        ProcessedFile file1 = new ProcessedFile();
        file1.setFileName("test1.xml");
        file1.setStatus("PENDING");
        file1.setProcessedAt(LocalDateTime.now());
        file1.setClient(client1);
        file1.setInterfaceEntity(interface1);
        processedFileRepository.save(file1);

        // Try to access client1's data through client2's context
        List<ProcessedFile> client2Files = processedFileRepository.findByClient_Id(client2.getId());
        assertTrue(client2Files.isEmpty());
    }

    @Test
    void testClientDeletion() {
        // Create test data for client1
        MappingRule rule1 = new MappingRule();
        rule1.setName("Test Rule 1");
        rule1.setXmlPath("/root/element");
        rule1.setDatabaseField("target");
        rule1.setSourceField("source");
        rule1.setTargetField("target");
        rule1.setTableName("test_table");
        rule1.setDataType("String");
        rule1.setClient(client1);
        mappingRuleRepository.save(rule1);

        // Add the rule to client1's mapping rules set
        client1.getMappingRules().add(rule1);
        clientService.saveClient(client1);

        // Delete client1
        clientService.deleteClient(client1.getId());

        // Verify data is deleted
        List<MappingRule> client1Rules = mappingRuleRepository.findByClient_Id(client1.getId());
        assertTrue(client1Rules.isEmpty());
    }

    @Test
    void testClientStatusChange() {
        // Create test data for client1
        AsnHeader header = new AsnHeader();
        header.setClient(client1);
        header.setDocumentNumber("DOC001");
        header.setDocumentDate(LocalDateTime.now().toString());
        header.setStatus("ACTIVE");
        header = asnHeaderRepository.save(header);

        AsnLine line1 = new AsnLine();
        line1.setClient(client1);
        line1.setLineNumber("LINE001");
        line1.setHeader(header);
        line1.setStatus("ACTIVE");
        asnLineRepository.save(line1);

        // Suspend client1
        client1.setStatus(ClientStatus.SUSPENDED);
        client1 = clientService.saveClient(client1);

        // Verify data is still accessible but client is suspended
        List<AsnLine> client1Lines = asnLineRepository.findByClient_Id(client1.getId());
        assertEquals(1, client1Lines.size());
        assertEquals(ClientStatus.SUSPENDED, client1.getStatus());
    }
} 