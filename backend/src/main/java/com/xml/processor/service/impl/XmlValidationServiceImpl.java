package com.xml.processor.service.impl;

import com.xml.processor.service.interfaces.XmlValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class XmlValidationServiceImpl implements XmlValidationService {

    private String validationErrorMessage;

    @Override
    public boolean validateXmlAgainstXsd(Document document, String xsdPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));
            validationErrorMessage = null;
            return true;
        } catch (SAXException | IOException e) {
            validationErrorMessage = "XML validation failed against XSD: " + e.getMessage();
            log.error(validationErrorMessage, e);
            return false;
        }
    }

    @Override
    public boolean validateXmlStructure(Document document) {
        try {
            // Check if document has a root element
            Element root = document.getDocumentElement();
            if (root == null) {
                validationErrorMessage = "XML document has no root element";
                return false;
            }

            // Check for required namespace declarations
            String namespaceURI = root.getNamespaceURI();
            if (namespaceURI == null || namespaceURI.isEmpty()) {
                validationErrorMessage = "XML document must have a namespace declaration";
                return false;
            }

            // Check for required attributes
            if (!root.hasAttribute("version")) {
                validationErrorMessage = "Root element must have a version attribute";
                return false;
            }

            validationErrorMessage = null;
            return true;
        } catch (Exception e) {
            validationErrorMessage = "XML structure validation failed: " + e.getMessage();
            log.error(validationErrorMessage, e);
            return false;
        }
    }

    @Override
    public boolean validateXmlContent(Document document, String interfaceType) {
        try {
            Element root = document.getDocumentElement();
            List<String> errors = new ArrayList<>();

            // Validate based on interface type
            switch (interfaceType.toUpperCase()) {
                case "INVOICE":
                    validateInvoiceContent(root, errors);
                    break;
                case "ORDER":
                    validateOrderContent(root, errors);
                    break;
                case "SHIPMENT":
                    validateShipmentContent(root, errors);
                    break;
                default:
                    validationErrorMessage = "Unknown interface type: " + interfaceType;
                    return false;
            }

            if (!errors.isEmpty()) {
                validationErrorMessage = String.join(", ", errors);
                return false;
            }

            validationErrorMessage = null;
            return true;
        } catch (Exception e) {
            validationErrorMessage = "XML content validation failed: " + e.getMessage();
            log.error(validationErrorMessage, e);
            return false;
        }
    }

    @Override
    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    private void validateInvoiceContent(Element root, List<String> errors) {
        // Check for required invoice elements
        checkRequiredElement(root, "InvoiceNumber", errors);
        checkRequiredElement(root, "InvoiceDate", errors);
        checkRequiredElement(root, "DueDate", errors);
        checkRequiredElement(root, "TotalAmount", errors);

        // Validate line items
        NodeList lineItems = root.getElementsByTagName("LineItem");
        if (lineItems.getLength() == 0) {
            errors.add("Invoice must contain at least one line item");
        } else {
            for (int i = 0; i < lineItems.getLength(); i++) {
                Element lineItem = (Element) lineItems.item(i);
                checkRequiredElement(lineItem, "ItemNumber", errors);
                checkRequiredElement(lineItem, "Quantity", errors);
                checkRequiredElement(lineItem, "UnitPrice", errors);
            }
        }
    }

    private void validateOrderContent(Element root, List<String> errors) {
        // Check for required order elements
        checkRequiredElement(root, "OrderNumber", errors);
        checkRequiredElement(root, "OrderDate", errors);
        checkRequiredElement(root, "CustomerNumber", errors);
        checkRequiredElement(root, "TotalAmount", errors);

        // Validate order items
        NodeList orderItems = root.getElementsByTagName("OrderItem");
        if (orderItems.getLength() == 0) {
            errors.add("Order must contain at least one order item");
        } else {
            for (int i = 0; i < orderItems.getLength(); i++) {
                Element orderItem = (Element) orderItems.item(i);
                checkRequiredElement(orderItem, "ProductCode", errors);
                checkRequiredElement(orderItem, "Quantity", errors);
                checkRequiredElement(orderItem, "UnitPrice", errors);
            }
        }
    }

    private void validateShipmentContent(Element root, List<String> errors) {
        // Check for required shipment elements
        checkRequiredElement(root, "ShipmentNumber", errors);
        checkRequiredElement(root, "ShipDate", errors);
        checkRequiredElement(root, "CarrierCode", errors);
        checkRequiredElement(root, "TrackingNumber", errors);

        // Validate shipment items
        NodeList shipmentItems = root.getElementsByTagName("ShipmentItem");
        if (shipmentItems.getLength() == 0) {
            errors.add("Shipment must contain at least one shipment item");
        } else {
            for (int i = 0; i < shipmentItems.getLength(); i++) {
                Element shipmentItem = (Element) shipmentItems.item(i);
                checkRequiredElement(shipmentItem, "ItemNumber", errors);
                checkRequiredElement(shipmentItem, "Quantity", errors);
                checkRequiredElement(shipmentItem, "Weight", errors);
            }
        }
    }

    private void checkRequiredElement(Element parent, String elementName, List<String> errors) {
        NodeList elements = parent.getElementsByTagName(elementName);
        if (elements.getLength() == 0) {
            errors.add("Required element '" + elementName + "' is missing");
        } else {
            Element element = (Element) elements.item(0);
            if (element.getTextContent().trim().isEmpty()) {
                errors.add("Element '" + elementName + "' cannot be empty");
            }
        }
    }
} 