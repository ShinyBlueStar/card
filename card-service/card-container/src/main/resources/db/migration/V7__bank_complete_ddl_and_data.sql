-- Bank Table DDL and Sample Data
-- This file contains the complete DDL for Bank table and sample data

-- =============================================
-- DDL FOR BANK TABLE
-- =============================================

-- Drop table if exists (for testing purposes)
-- DROP TABLE banks CASCADE CONSTRAINTS;

-- Create Bank table
CREATE TABLE banks (
    ID NUMBER(19) NOT NULL,
    BIN_CODE VARCHAR2(6) NOT NULL,
    bank_name VARCHAR2(100) NOT NULL,
    IS_ACTIVE NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR2(50),
    updated_by VARCHAR2(50),
    CONSTRAINT pk_banks PRIMARY KEY (ID),
    CONSTRAINT uk_banks_bin_code UNIQUE (BIN_CODE),
    CONSTRAINT chk_banks_active CHECK (IS_ACTIVE IN (0, 1))
);

-- Create sequence for Bank
CREATE SEQUENCE BANK_SEQ START WITH 1 INCREMENT BY 1 CACHE 20;

-- Create indexes for better performance
CREATE INDEX idx_banks_bin_code ON banks(BIN_CODE);
CREATE INDEX idx_banks_active ON banks(IS_ACTIVE);
CREATE INDEX idx_banks_name ON banks(bank_name);

-- Create trigger for auto-updating updated_at
CREATE OR REPLACE TRIGGER trg_banks_updated_at
    BEFORE UPDATE ON banks
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- =============================================
-- SAMPLE DATA FOR BANK TABLE
-- =============================================

-- Insert major Iranian banks
INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '123456', 'بانک ملی ایران', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '234567', 'بانک صادرات ایران', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '345678', 'بانک تجارت', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '456789', 'بانک ملت', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '567890', 'بانک پارسیان', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '678901', 'بانک پاسارگاد', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '789012', 'بانک سامان', 'بانک خصوصی و مدرن', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '890123', 'بانک اقتصاد نوین', 'بانک خصوصی و دیجیتال', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '901234', 'بانک شهر', 'بانک شهرداری تهران', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '012345', 'بانک دی', 'بانک دیجیتال و آنلاین', 1, 'SYSTEM', 'SYSTEM');

-- Specialized banks
INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '111111', 'بانک کشاورزی', 'بانک تخصصی کشاورزی و روستایی', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '222222', 'بانک صنعت و معدن', 'بانک تخصصی صنعت و معدن', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '333333', 'بانک مسکن', 'بانک تخصصی مسکن و ساختمان', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '444444', 'بانک توسعه تعاون', 'بانک تخصصی تعاونی', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '555555', 'بانک کارآفرین', 'بانک تخصصی کارآفرینی', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '666666', 'بانک آینده', 'بانک آینده و نوآوری', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '777777', 'بانک انصار', 'بانک انصار و همیاری', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '888888', 'بانک گردشگری', 'بانک تخصصی گردشگری', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '999999', 'بانک سینا', 'بانک سینا و سلامت', 1, 'SYSTEM', 'SYSTEM');

-- Regional banks
INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '101010', 'بانک مهر اقتصاد', 'بانک منطقه‌ای مهر اقتصاد', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '202020', 'بانک قوامین', 'بانک قوامین و امنیت', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '303030', 'بانک رسالت', 'بانک رسالت و خدمت', 1, 'SYSTEM', 'SYSTEM');

-- Insert some inactive banks for testing
INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '000001', 'بانک منحل شده 1', 'بانک منحل شده برای تست', 0, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, , IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '000002', 'بانک منحل شده 2', 'بانک منحل شده برای تست', 0, 'SYSTEM', 'SYSTEM');

-- =============================================
-- VERIFICATION QUERIES
-- =============================================
