-- V3__create_bank_card_type_card_category_tables.sql
-- Migration for Bank, CardType, and CardCategory tables

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
    CONSTRAINT uk_banks_bin_code UNIQUE (BIN_CODE)
);

-- Create sequence for Bank
CREATE SEQUENCE BANK_SEQ START WITH 1 INCREMENT BY 1;

-- Create CardType table
CREATE TABLE CARD_TYPE (
    ID NUMBER(19) NOT NULL,
    CODE VARCHAR2(30) NOT NULL,
    NAME VARCHAR2(100) NOT NULL,
    DESCRIPTION VARCHAR2(255),
    IS_ACTIVE NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR2(50),
    updated_by VARCHAR2(50),
    CONSTRAINT pk_card_type PRIMARY KEY (ID),
    CONSTRAINT uk_card_type_code UNIQUE (CODE)
);

-- Create sequence for CardType
CREATE SEQUENCE CARD_TYPE_SEQ START WITH 1 INCREMENT BY 1;

-- Create CardCategory table (without direct relationship to CardType)
CREATE TABLE CARD_CATEGORY (
    ID NUMBER(19) NOT NULL,
    CODE VARCHAR2(30) NOT NULL,
    NAME VARCHAR2(100) NOT NULL,
    DESCRIPTION VARCHAR2(255),
    IS_ACTIVE NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR2(50),
    updated_by VARCHAR2(50),
    CONSTRAINT pk_card_category PRIMARY KEY (ID),
    CONSTRAINT uk_card_category_code UNIQUE (CODE)
);

-- Create sequence for CardCategory
CREATE SEQUENCE CARD_CATEGORY_SEQ START WITH 1 INCREMENT BY 1;

-- Create indexes for better performance
CREATE INDEX idx_banks_bin_code ON banks(BIN_CODE);
CREATE INDEX idx_banks_active ON banks(IS_ACTIVE);
CREATE INDEX idx_card_type_code ON CARD_TYPE(CODE);
CREATE INDEX idx_card_type_active ON CARD_TYPE(IS_ACTIVE);
CREATE INDEX idx_card_category_code ON CARD_CATEGORY(CODE);
CREATE INDEX idx_card_category_active ON CARD_CATEGORY(IS_ACTIVE);

-- Insert sample data for CardType
INSERT INTO CARD_TYPE (ID, CODE, NAME, DESCRIPTION, IS_ACTIVE) VALUES (CARD_TYPE_SEQ.NEXTVAL, 'MAGNETIC', 'کارت مغناطیسی', 'کارت با نوار مغناطیسی', 1);
INSERT INTO CARD_TYPE (ID, CODE, NAME, DESCRIPTION, IS_ACTIVE) VALUES (CARD_TYPE_SEQ.NEXTVAL, 'SMART', 'کارت هوشمند', 'کارت با تراشه هوشمند', 1);
INSERT INTO CARD_TYPE (ID, CODE, NAME, DESCRIPTION, IS_ACTIVE) VALUES (CARD_TYPE_SEQ.NEXTVAL, 'HYBRID', 'کارت ترکیبی', 'کارت با نوار مغناطیسی و تراشه', 1);

-- Insert sample data for CardCategory
INSERT INTO CARD_CATEGORY (ID, CODE, NAME, DESCRIPTION, IS_ACTIVE) VALUES (CARD_CATEGORY_SEQ.NEXTVAL, 'CREDIT', 'کارت اعتباری', 'کارت اعتباری', 1);
INSERT INTO CARD_CATEGORY (ID, CODE, NAME, DESCRIPTION, IS_ACTIVE) VALUES (CARD_CATEGORY_SEQ.NEXTVAL, 'DEBIT', 'کارت نقدی', 'کارت نقدی', 1);
INSERT INTO CARD_CATEGORY (ID, CODE, NAME, DESCRIPTION, IS_ACTIVE) VALUES (CARD_CATEGORY_SEQ.NEXTVAL, 'PREPAID', 'کارت پیش پرداخت', 'کارت پیش پرداخت', 1);
INSERT INTO CARD_CATEGORY (ID, CODE, NAME, DESCRIPTION, IS_ACTIVE) VALUES (CARD_CATEGORY_SEQ.NEXTVAL, 'POSTPAID', 'کارت پس پرداخت', 'کارت پس پرداخت', 1);

-- Insert sample data for Bank
INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE) VALUES (BANK_SEQ.NEXTVAL, '123456', 'بانک ملی ایران', 1);
INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE) VALUES (BANK_SEQ.NEXTVAL, '234567', 'بانک صادرات ایران', 1);
INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE) VALUES (BANK_SEQ.NEXTVAL, '345678', 'بانک تجارت', 1);
