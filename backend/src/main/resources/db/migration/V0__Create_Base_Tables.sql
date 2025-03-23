-- Create Users table first (no dependencies)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create User Roles table (depends on Users)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    roles VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create Clients table first (no dependencies)
CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Interfaces table (depends on CLIENTS)
CREATE TABLE interfaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    schema_path VARCHAR(255),
    root_element VARCHAR(255),
    namespace VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT uk_client_name UNIQUE (client_id, name)
);

-- Create ASN Headers table (depends on CLIENTS)
CREATE TABLE asn_headers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_number VARCHAR(50) NOT NULL,
    document_type VARCHAR(50),
    sender_id VARCHAR(50),
    receiver_id VARCHAR(50),
    document_date VARCHAR(255) NOT NULL,
    document_time VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(1000),
    client_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- Create ASN Lines table (depends on ASN_HEADERS and CLIENTS)
CREATE TABLE asn_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    header_id BIGINT NOT NULL,
    line_number INT,
    item_number VARCHAR(50),
    item_description VARCHAR(255),
    quantity INT,
    unit_of_measure VARCHAR(50),
    lot_number VARCHAR(50),
    serial_number VARCHAR(50),
    status VARCHAR(20),
    notes VARCHAR(500),
    client_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (header_id) REFERENCES asn_headers(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- Create Processed Files table (depends on INTERFACES and CLIENTS)
CREATE TABLE processed_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(1000),
    interface_id BIGINT,
    client_id BIGINT NOT NULL,
    processed_data JSON,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (interface_id) REFERENCES interfaces(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

-- Create Mapping Rules table (depends on CLIENTS and INTERFACES)
CREATE TABLE mapping_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    xml_path VARCHAR(255) NOT NULL,
    database_field VARCHAR(255) NOT NULL,
    transformation VARCHAR(255),
    is_required BOOLEAN DEFAULT FALSE,
    default_value VARCHAR(255),
    priority INT,
    interface_id BIGINT,
    client_id BIGINT NOT NULL,
    description VARCHAR(500),
    source_field VARCHAR(255),
    target_field VARCHAR(255),
    validation_rule VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    table_name VARCHAR(255),
    data_type VARCHAR(50),
    is_attribute BOOLEAN DEFAULT FALSE,
    xsd_element VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (interface_id) REFERENCES interfaces(id)
);

-- Create indexes for better query performance
CREATE INDEX idx_asn_headers_document_number ON asn_headers(document_number);
CREATE INDEX idx_asn_lines_item_number ON asn_lines(item_number);
CREATE INDEX idx_processed_files_file_name ON processed_files(file_name);
CREATE INDEX idx_mapping_rules_source_field ON mapping_rules(source_field);
CREATE INDEX idx_interfaces_type ON interfaces(type);
CREATE INDEX idx_interfaces_root_element ON interfaces(root_element);
CREATE INDEX idx_asn_headers_client_id ON asn_headers(client_id);
CREATE INDEX idx_asn_lines_client_id ON asn_lines(client_id);
CREATE INDEX idx_processed_files_client_id ON processed_files(client_id);
CREATE INDEX idx_mapping_rules_client_id ON mapping_rules(client_id);
CREATE INDEX idx_mapping_rules_interface_id ON mapping_rules(interface_id); 