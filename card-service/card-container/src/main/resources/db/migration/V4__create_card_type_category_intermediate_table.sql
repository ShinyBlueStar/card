-- V4__create_card_type_category_intermediate_table.sql
-- Migration for CardTypeCategory intermediate entity

-- Create CardTypeCategory intermediate table
CREATE TABLE CARD_TYPE_CATEGORY (
    ID NUMBER(19) NOT NULL,
    CARD_TYPE_ID NUMBER(19) NOT NULL,
    CARD_CATEGORY_ID NUMBER(19) NOT NULL,
    IS_ACTIVE NUMBER(1) DEFAULT 1,
    PRIORITY NUMBER(10),
    VALID_FROM DATE,
    VALID_TO DATE,
    BUSINESS_RULES VARCHAR2(500),
    DESCRIPTION VARCHAR2(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR2(50),
    updated_by VARCHAR2(50),
    CONSTRAINT pk_card_type_category PRIMARY KEY (ID),
    CONSTRAINT fk_ctc_type FOREIGN KEY (CARD_TYPE_ID) REFERENCES CARD_TYPE(ID),
    CONSTRAINT fk_ctc_category FOREIGN KEY (CARD_CATEGORY_ID) REFERENCES CARD_CATEGORY(ID),
    CONSTRAINT uk_ctc_unique UNIQUE (CARD_TYPE_ID, CARD_CATEGORY_ID)
);

-- Create sequence for CardTypeCategory
CREATE SEQUENCE CARD_TYPE_CATEGORY_SEQ START WITH 1 INCREMENT BY 1;

-- Add CARD_TYPE_CATEGORY_ID column to CardProfile table
ALTER TABLE card_types ADD CARD_TYPE_CATEGORY_ID NUMBER(19);

-- Add ISSUING_BANK_ID column to CardProfile table (rename from BANK_ID)
ALTER TABLE card_types ADD ISSUING_BANK_ID NUMBER(19);

-- Add IS_ACTIVE column to CardProfile table (replace STATUS field)
ALTER TABLE card_types ADD IS_ACTIVE NUMBER(1) DEFAULT 1;

-- Add foreign key constraint for CardProfile
ALTER TABLE card_types ADD CONSTRAINT fk_card_profile_type_category
    FOREIGN KEY (CARD_TYPE_CATEGORY_ID) REFERENCES CARD_TYPE_CATEGORY(ID);

ALTER TABLE card_types ADD CONSTRAINT fk_card_profile_issuing_bank
    FOREIGN KEY (ISSUING_BANK_ID) REFERENCES banks(ID);

-- Create indexes for better performance
CREATE INDEX idx_ctc_type ON CARD_TYPE_CATEGORY(CARD_TYPE_ID);
CREATE INDEX idx_ctc_category ON CARD_TYPE_CATEGORY(CARD_CATEGORY_ID);
CREATE INDEX idx_ctc_active ON CARD_TYPE_CATEGORY(IS_ACTIVE);
CREATE INDEX idx_ctc_priority ON CARD_TYPE_CATEGORY(PRIORITY);
CREATE INDEX idx_ctc_validity ON CARD_TYPE_CATEGORY(VALID_FROM, VALID_TO);
CREATE INDEX idx_card_profile_type_category ON card_types(CARD_TYPE_CATEGORY_ID);
CREATE INDEX idx_card_profile_issuing_bank ON card_types(ISSUING_BANK_ID);
CREATE INDEX idx_card_profile_is_active ON card_types(IS_ACTIVE);

-- Insert sample data for CardTypeCategory relationships
-- Smart Credit Card
INSERT INTO CARD_TYPE_CATEGORY (ID, CARD_TYPE_ID, CARD_CATEGORY_ID, IS_ACTIVE, PRIORITY, DESCRIPTION, BUSINESS_RULES)
VALUES (CARD_TYPE_CATEGORY_SEQ.NEXTVAL, 2, 1, 1, 1, 'Smart Credit Card', 'High security, chip-based transactions');

-- Smart Debit Card
INSERT INTO CARD_TYPE_CATEGORY (ID, CARD_TYPE_ID, CARD_CATEGORY_ID, IS_ACTIVE, PRIORITY, DESCRIPTION, BUSINESS_RULES)
VALUES (CARD_TYPE_CATEGORY_SEQ.NEXTVAL, 2, 2, 1, 2, 'Smart Debit Card', 'Direct account access, chip-based');

-- Magnetic Credit Card
INSERT INTO CARD_TYPE_CATEGORY (ID, CARD_TYPE_ID, CARD_CATEGORY_ID, IS_ACTIVE, PRIORITY, DESCRIPTION, BUSINESS_RULES)
VALUES (CARD_TYPE_CATEGORY_SEQ.NEXTVAL, 1, 1, 1, 3, 'Magnetic Credit Card', 'Traditional magnetic stripe, legacy support');

-- Magnetic Debit Card
INSERT INTO CARD_TYPE_CATEGORY (ID, CARD_TYPE_ID, CARD_CATEGORY_ID, IS_ACTIVE, PRIORITY, DESCRIPTION, BUSINESS_RULES)
VALUES (CARD_TYPE_CATEGORY_SEQ.NEXTVAL, 1, 2, 1, 4, 'Magnetic Debit Card', 'Traditional magnetic stripe, legacy support');

-- Hybrid Credit Card
INSERT INTO CARD_TYPE_CATEGORY (ID, CARD_TYPE_ID, CARD_CATEGORY_ID, IS_ACTIVE, PRIORITY, DESCRIPTION, BUSINESS_RULES)
VALUES (CARD_TYPE_CATEGORY_SEQ.NEXTVAL, 3, 1, 1, 5, 'Hybrid Credit Card', 'Both chip and magnetic stripe support');

-- Hybrid Debit Card
INSERT INTO CARD_TYPE_CATEGORY (ID, CARD_TYPE_ID, CARD_CATEGORY_ID, IS_ACTIVE, PRIORITY, DESCRIPTION, BUSINESS_RULES)
VALUES (CARD_TYPE_CATEGORY_SEQ.NEXTVAL, 3, 2, 1, 6, 'Hybrid Debit Card', 'Both chip and magnetic stripe support');

-- Update existing CardProfile records to reference CardTypeCategory and IssuingBank
-- Assuming we have some existing card profiles, update them to reference the new relationships
-- This is a sample update - adjust based on your actual data
UPDATE card_types SET CARD_TYPE_CATEGORY_ID = 1, ISSUING_BANK_ID = 1 WHERE ID = 1; -- First card profile -> Smart Credit, Bank 1
UPDATE card_types SET CARD_TYPE_CATEGORY_ID = 2, ISSUING_BANK_ID = 2 WHERE ID = 2; -- Second card profile -> Smart Debit, Bank 2
UPDATE card_types SET CARD_TYPE_CATEGORY_ID = 3, ISSUING_BANK_ID = 3 WHERE ID = 3; -- Third card profile -> Magnetic Credit, Bank 3

-- Add comments for documentation
COMMENT ON TABLE CARD_TYPE_CATEGORY IS 'Intermediate table for many-to-many relationship between CardType and CardCategory';
COMMENT ON COLUMN CARD_TYPE_CATEGORY.CARD_TYPE_ID IS 'Reference to CardType';
COMMENT ON COLUMN CARD_TYPE_CATEGORY.CARD_CATEGORY_ID IS 'Reference to CardCategory';
COMMENT ON COLUMN CARD_TYPE_CATEGORY.PRIORITY IS 'Priority order for this type-category combination';
COMMENT ON COLUMN CARD_TYPE_CATEGORY.VALID_FROM IS 'Start date for this relationship validity';
COMMENT ON COLUMN CARD_TYPE_CATEGORY.VALID_TO IS 'End date for this relationship validity';
COMMENT ON COLUMN CARD_TYPE_CATEGORY.BUSINESS_RULES IS 'Business rules specific to this type-category combination';
COMMENT ON COLUMN card_types.CARD_TYPE_CATEGORY_ID IS 'Reference to CardTypeCategory relationship';
COMMENT ON COLUMN card_types.ISSUING_BANK_ID IS 'Reference to Bank (issuing bank)';
COMMENT ON COLUMN card_types.IS_ACTIVE IS 'Active status of the card profile (1=Active, 0=Inactive)';
