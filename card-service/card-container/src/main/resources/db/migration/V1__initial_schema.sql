--------------------------------------------------------
-- Sequences
--------------------------------------------------------

CREATE SEQUENCE BATCH_PARTY_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20;
CREATE SEQUENCE DEACTIVATION_REASONS_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20;
CREATE SEQUENCE HISTORY_DEACTIVATION_REASONS_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20;
CREATE SEQUENCE PARTY_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 21 CACHE 20;
CREATE SEQUENCE card_status_seq MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 221 CACHE 20;
CREATE SEQUENCE REVINFO_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 50 START WITH 1001 CACHE 20;

--------------------------------------------------------
-- Tables
--------------------------------------------------------

CREATE TABLE REVINFO (
                         REV NUMBER(10,0),
                         REVTSTMP NUMBER(19,0)
);

CREATE TABLE BATCH_PARTY (
                                 FAILURE_COUNT NUMBER(10,0),
                                 SUCCESS_COUNT NUMBER(10,0),
                                 TOTAL_COUNT NUMBER(10,0),
                                 CREATED_DATE TIMESTAMP(6),
                                 ID NUMBER(19,0),
                                 LAST_MODIFIED_DATE TIMESTAMP(6),
                                 CREATED_BY VARCHAR2(255 CHAR),
                                 FAILED_FILE_PATH VARCHAR2(255 CHAR),
                                 FILE_NAME VARCHAR2(255 CHAR),
                                 FILE_PATH VARCHAR2(255 CHAR),
                                 FILE_TYPE VARCHAR2(255 CHAR),
                                 INPUT_NAME VARCHAR2(255 CHAR),
                                 LAST_MODIFIED_BY VARCHAR2(255 CHAR),
                                 STATUS VARCHAR2(255 CHAR),
                                 TYPE VARCHAR2(255 CHAR)
);

CREATE TABLE DEACTIVATION_REASONS (
                                          ACTIVE NUMBER(1,0),
                                          CODE NUMBER(10,0),
                                          CREATED_DATE TIMESTAMP(6),
                                          ID NUMBER(19,0),
                                          LAST_MODIFIED_DATE TIMESTAMP(6),
                                          TITLE VARCHAR2(200 CHAR),
                                          CREATED_BY VARCHAR2(255 CHAR),
                                          LAST_MODIFIED_BY VARCHAR2(255 CHAR)
);

CREATE TABLE DEACTIVATION_REASONS_AUD (
                                              ACTIVE NUMBER(1,0),
                                              CODE NUMBER(10,0),
                                              REV NUMBER(10,0),
                                              REVTYPE NUMBER(3,0),
                                              CREATED_DATE TIMESTAMP(6),
                                              ID NUMBER(19,0),
                                              LAST_MODIFIED_DATE TIMESTAMP(6),
                                              TITLE VARCHAR2(200 CHAR),
                                              CREATED_BY VARCHAR2(255 CHAR),
                                              LAST_MODIFIED_BY VARCHAR2(255 CHAR)
);

CREATE TABLE HISTORY_DEACTIVATION_REASONS (
                                                  DEACTIVATION_STATUS NUMBER(1,0),
                                                  CREATED_DATE TIMESTAMP(6),
                                                  DEACTIVATION_REASONS_ID NUMBER(19,0),
                                                  ID NUMBER(19,0),
                                                  LAST_MODIFIED_DATE TIMESTAMP(6),
                                                  PARTY_ID NUMBER(19,0),
                                                  CREATED_BY VARCHAR2(255 CHAR),
                                                  DESCRIPTION VARCHAR2(255 CHAR),
                                                  LAST_MODIFIED_BY VARCHAR2(255 CHAR)
);

CREATE TABLE HISTORY_DEACTIVATION_REASONS_AUD (
                                                      DEACTIVATION_STATUS NUMBER(1,0),
                                                      REV NUMBER(10,0),
                                                      REVTYPE NUMBER(3,0),
                                                      CREATED_DATE TIMESTAMP(6),
                                                      DEACTIVATION_REASONS_ID NUMBER(19,0),
                                                      ID NUMBER(19,0),
                                                      LAST_MODIFIED_DATE TIMESTAMP(6),
                                                      PARTY_ID NUMBER(19,0),
                                                      CREATED_BY VARCHAR2(255 CHAR),
                                                      DESCRIPTION VARCHAR2(255 CHAR),
                                                      LAST_MODIFIED_BY VARCHAR2(255 CHAR)
);

CREATE TABLE PARTY (
                           BLACK_LIST NUMBER(1,0),
                           IS_ACTIVE NUMBER(1,0),
                           ISSUE_SPECT NUMBER(1,0),
                           BIRTH_DATE TIMESTAMP(6),
                           CREATED_DATE TIMESTAMP(6),
                           DEACTIVATION_REASONS_ID NUMBER(19,0),
                           DEATH_DATE TIMESTAMP(6),
                           ID NUMBER(19,0),
                           LAST_MODIFIED_DATE TIMESTAMP(6),
                           REFERENCE_VERIFIED_DATE TIMESTAMP(6),
                           NATIONAL_CODE VARCHAR2(20 CHAR),
                           OPERATION_CODE VARCHAR2(50 CHAR),
                           OPERATION_NAME VARCHAR2(100 CHAR),
                           SANCTION_TYPE VARCHAR2(100 CHAR),
                           SHAHAB_CODE VARCHAR2(100 CHAR),
                           EMAIL VARCHAR2(150 CHAR),
                           CREATED_BY VARCHAR2(255 CHAR),
                           DEACTIVATION_REASONS_DESCRIPTION VARCHAR2(255 CHAR),
                           LAST_MODIFIED_BY VARCHAR2(255 CHAR),
                           TYPE VARCHAR2(255 CHAR)
);

CREATE TABLE PARTY_AUD (
                               BLACK_LIST NUMBER(1,0),
                               IS_ACTIVE NUMBER(1,0),
                               ISSUE_SPECT NUMBER(1,0),
                               REV NUMBER(10,0),
                               REVTYPE NUMBER(3,0),
                               BIRTH_DATE TIMESTAMP(6),
                               CREATED_DATE TIMESTAMP(6),
                               DEACTIVATION_REASONS_ID NUMBER(19,0),
                               DEATH_DATE TIMESTAMP(6),
                               ID NUMBER(19,0),
                               LAST_MODIFIED_DATE TIMESTAMP(6),
                               REFERENCE_VERIFIED_DATE TIMESTAMP(6),
                               NATIONAL_CODE VARCHAR2(20 CHAR),
                               OPERATION_CODE VARCHAR2(50 CHAR),
                               OPERATION_NAME VARCHAR2(100 CHAR),
                               SANCTION_TYPE VARCHAR2(100 CHAR),
                               SHAHAB_CODE VARCHAR2(100 CHAR),
                               EMAIL VARCHAR2(150 CHAR),
                               CREATED_BY VARCHAR2(255 CHAR),
                               DEACTIVATION_REASONS_DESCRIPTION VARCHAR2(255 CHAR),
                               LAST_MODIFIED_BY VARCHAR2(255 CHAR),
                               TYPE VARCHAR2(255 CHAR)
);

CREATE TABLE PARTY_BUSINESS (
                                    ECONOMIC_ID NUMBER(19,0),
                                    FOUNDED_DATE TIMESTAMP(6),
                                    LAST_CHANGE_DATE TIMESTAMP(6),
                                    PARTY_ID NUMBER(19,0),
                                    REGISTER_ID NUMBER(19,0),
                                    REGISTERED_CAPITAL NUMBER(19,0),
                                    TAX_CODE NUMBER(19,0),
                                    FISCAL_YEAR VARCHAR2(10 CHAR),
                                    REG_DATE VARCHAR2(10 CHAR),
                                    REP_NATIONAL_CODE VARCHAR2(10 CHAR),
                                    GUILD_CODE VARCHAR2(50 CHAR),
                                    LIFE_STATUS VARCHAR2(50 CHAR),
                                    OFFICIAL_NEWSPAPER_NO VARCHAR2(50 CHAR),
                                    ECONOMIC_SECTOR VARCHAR2(100 CHAR),
                                    ECONOMIC_SUB_SECTOR VARCHAR2(100 CHAR),
                                    ID_CITY VARCHAR2(100 CHAR),
                                    ID_COUNTRY VARCHAR2(100 CHAR),
                                    LAW_TYPE VARCHAR2(100 CHAR),
                                    LEGAL_STRUCTURE VARCHAR2(100 CHAR),
                                    REP_POSITION VARCHAR2(100 CHAR),
                                    ACTIVITY_FIELD VARCHAR2(200 CHAR),
                                    COMPANY_NAME VARCHAR2(200 CHAR),
                                    REP_NAME VARCHAR2(200 CHAR),
                                    WEB_ADDRESS VARCHAR2(255 CHAR)
);

CREATE TABLE PARTY_BUSINESS_AUD (
                                        REV NUMBER(10,0),
                                        ECONOMIC_ID NUMBER(19,0),
                                        FOUNDED_DATE TIMESTAMP(6),
                                        LAST_CHANGE_DATE TIMESTAMP(6),
                                        PARTY_ID NUMBER(19,0),
                                        REGISTER_ID NUMBER(19,0),
                                        REGISTERED_CAPITAL NUMBER(19,0),
                                        TAX_CODE NUMBER(19,0),
                                        FISCAL_YEAR VARCHAR2(10 CHAR),
                                        REG_DATE VARCHAR2(10 CHAR),
                                        REP_NATIONAL_CODE VARCHAR2(10 CHAR),
                                        GUILD_CODE VARCHAR2(50 CHAR),
                                        LIFE_STATUS VARCHAR2(50 CHAR),
                                        OFFICIAL_NEWSPAPER_NO VARCHAR2(50 CHAR),
                                        ECONOMIC_SECTOR VARCHAR2(100 CHAR),
                                        ECONOMIC_SUB_SECTOR VARCHAR2(100 CHAR),
                                        ID_CITY VARCHAR2(100 CHAR),
                                        ID_COUNTRY VARCHAR2(100 CHAR),
                                        LAW_TYPE VARCHAR2(100 CHAR),
                                        LEGAL_STRUCTURE VARCHAR2(100 CHAR),
                                        REP_POSITION VARCHAR2(100 CHAR),
                                        ACTIVITY_FIELD VARCHAR2(200 CHAR),
                                        COMPANY_NAME VARCHAR2(200 CHAR),
                                        REP_NAME VARCHAR2(200 CHAR),
                                        WEB_ADDRESS VARCHAR2(255 CHAR)
);

CREATE TABLE PARTY_CUSTOMER_BANK_ACCOUNT (
                                                 CUSTOMER_DEFINITION_DATE TIMESTAMP(6),
                                                 PARTY_ID NUMBER(19,0),
                                                 CUSTOMER_DEFINITION_BRANCH_CODE VARCHAR2(20 CHAR),
                                                 CUSTOMER_NO VARCHAR2(50 CHAR),
                                                 CUSTOMER_DEFINITION_BRANCH_TITLE VARCHAR2(100 CHAR),
                                                 CUSTOMER_SERVICE VARCHAR2(100 CHAR)
);

CREATE TABLE PARTY_INDIVIDUAL (
                                      PARTY_ID NUMBER(19,0),
                                      BIRTH_CERT_NUMBER VARCHAR2(20 CHAR),
                                      BIRTH_CERT_TYPE VARCHAR2(20 CHAR),
                                      GENDER_ID VARCHAR2(20 CHAR),
                                      LIFE_STATUS VARCHAR2(20 CHAR),
                                      MARITAL_STATUS VARCHAR2(50 CHAR),
                                      MILITARY_STATUS VARCHAR2(20 CHAR),
                                      DEGREE_LEVEL VARCHAR2(30 CHAR),
                                      BIRTH_CERT_SERIAL VARCHAR2(50 CHAR),
                                      BIRTH_CERT_SERIES VARCHAR2(50 CHAR),
                                      LANGUAGE VARCHAR2(50 CHAR),
                                      RESIDENCY_STATUS VARCHAR2(50 CHAR),
                                      BIRTH_CITY VARCHAR2(100 CHAR),
                                      BIRTH_PLACE_OF_ISSUE VARCHAR2(100 CHAR),
                                      FATHER_NAME VARCHAR2(100 CHAR),
                                      FIRST_NAME VARCHAR2(100 CHAR),
                                      LAST_NAME VARCHAR2(100 CHAR),
                                      LATIN_FIRST_NAME VARCHAR2(100 CHAR),
                                      LATIN_LAST_NAME VARCHAR2(100 CHAR),
                                      JOB VARCHAR2(150 CHAR)
);

CREATE TABLE PARTY_INDIVIDUAL_AUD (
                                          REV NUMBER(10,0),
                                          PARTY_ID NUMBER(19,0),
                                          BIRTH_CERT_NUMBER VARCHAR2(20 CHAR),
                                          BIRTH_CERT_TYPE VARCHAR2(20 CHAR),
                                          GENDER_ID VARCHAR2(20 CHAR),
                                          LIFE_STATUS VARCHAR2(20 CHAR),
                                          MARITAL_STATUS VARCHAR2(50 CHAR),
                                          MILITARY_STATUS VARCHAR2(20 CHAR),
                                          DEGREE_LEVEL VARCHAR2(30 CHAR),
                                          BIRTH_CERT_SERIAL VARCHAR2(50 CHAR),
                                          BIRTH_CERT_SERIES VARCHAR2(50 CHAR),
                                          LANGUAGE VARCHAR2(50 CHAR),
                                          RESIDENCY_STATUS VARCHAR2(50 CHAR),
                                          BIRTH_CITY VARCHAR2(100 CHAR),
                                          BIRTH_PLACE_OF_ISSUE VARCHAR2(100 CHAR),
                                          FATHER_NAME VARCHAR2(100 CHAR),
                                          FIRST_NAME VARCHAR2(100 CHAR),
                                          LAST_NAME VARCHAR2(100 CHAR),
                                          LATIN_FIRST_NAME VARCHAR2(100 CHAR),
                                          LATIN_LAST_NAME VARCHAR2(100 CHAR),
                                          JOB VARCHAR2(150 CHAR)
);

CREATE TABLE PARTY_ADDRESS (
                                   PARTY_ID NUMBER(19,0),
                                   ZIP_CODE VARCHAR2(20 CHAR),
                                   CONTACT_POINT_TYPE VARCHAR2(50 CHAR),
                                   VALIDATION_STATUS VARCHAR2(70 CHAR),
                                   ADDRESS VARCHAR2(255 CHAR),
                                   CODE VARCHAR2(255 CHAR),
                                   TITLE VARCHAR2(255 CHAR)
);


CREATE TABLE PARTY_PHONE (
                                 PARTY_ID NUMBER(19,0),
                                 MOBILE_NUMBER VARCHAR2(20 CHAR),
                                 PHONE_TYPE VARCHAR2(50 CHAR),
                                 IS_VALID VARCHAR2(255 CHAR)
);

CREATE TABLE STATUS (
                            CREATED_DATE TIMESTAMP(6),
                            ID NUMBER(19,0),
                            LAST_MODIFIED_DATE TIMESTAMP(6),
                            CODE VARCHAR2(255 CHAR),
                            CREATED_BY VARCHAR2(255 CHAR),
                            DESCRIPTION VARCHAR2(255 CHAR),
                            LAST_MODIFIED_BY VARCHAR2(255 CHAR),
                            PERSIAN_DESCRIPTION VARCHAR2(255 CHAR)
);

--------------------------------------------------------
-- Constraints
--------------------------------------------------------

-- Primary Keys
ALTER TABLE PARTY            ADD CONSTRAINT PK_PARTY PRIMARY KEY (ID);
ALTER TABLE PARTY_BUSINESS   ADD CONSTRAINT PK_PARTY_BUSINESS PRIMARY KEY (PARTY_ID);
ALTER TABLE PARTY_INDIVIDUAL ADD CONSTRAINT PK_PARTY_INDIVIDUAL PRIMARY KEY (PARTY_ID);
ALTER TABLE BATCH_PARTY      ADD CONSTRAINT PK_BATCH_PARTY PRIMARY KEY (ID);
ALTER TABLE DEACTIVATION_REASONS ADD CONSTRAINT PK_DEACTIVATION_REASONS PRIMARY KEY (ID);
ALTER TABLE HISTORY_DEACTIVATION_REASONS ADD CONSTRAINT PK_HISTORY_DEACTIVATION_REASONS PRIMARY KEY (ID);
ALTER TABLE REVINFO              ADD CONSTRAINT PK_REVINFO PRIMARY KEY (REV);
ALTER TABLE STATUS           ADD CONSTRAINT PK_STATUS PRIMARY KEY (ID);

-- Unique Constraints
ALTER TABLE PARTY ADD CONSTRAINT UQ_PARTY_NATIONAL_CODE UNIQUE (NATIONAL_CODE);
ALTER TABLE DEACTIVATION_REASONS ADD CONSTRAINT UQ_DEACTIVATION_REASONS_CODE UNIQUE (CODE);

-- Check Constraints
ALTER TABLE PARTY ADD CONSTRAINT CHK_PARTY_TYPE CHECK (TYPE IN ('BUSINESS','INDIVIDUAL'));
ALTER TABLE PARTY_AUD ADD CONSTRAINT CHK_PARTY_AUD_TYPE CHECK (TYPE IN ('BUSINESS','INDIVIDUAL'));
ALTER TABLE DEACTIVATION_REASONS ADD CONSTRAINT CHK_DEACTIVATION_REASONS_ACTIVE CHECK (ACTIVE IN (0,1));
ALTER TABLE DEACTIVATION_REASONS_AUD ADD CONSTRAINT CHK_DEACTIVATION_REASONS_AUD_ACTIVE CHECK (ACTIVE IN (0,1));
ALTER TABLE HISTORY_DEACTIVATION_REASONS ADD CONSTRAINT CHK_HISTORY_DEACT_STATUS CHECK (DEACTIVATION_STATUS IN (0,1));
ALTER TABLE HISTORY_DEACTIVATION_REASONS_AUD ADD CONSTRAINT CHK_HISTORY_DEACT_STATUS_AUD CHECK (DEACTIVATION_STATUS IN (0,1));
ALTER TABLE PARTY_AUD ADD CONSTRAINT CHK_PARTY_AUD_ISACTIVE CHECK (IS_ACTIVE IN (0,1));
ALTER TABLE PARTY_AUD ADD CONSTRAINT CHK_PARTY_AUD_BLACKLIST CHECK (BLACK_LIST IN (0,1));
ALTER TABLE PARTY_AUD ADD CONSTRAINT CHK_PARTY_AUD_ISSUESPECT CHECK (ISSUE_SPECT IN (0,1));
ALTER TABLE BATCH_PARTY ADD CONSTRAINT CHK_BATCH_FILETYPE CHECK (FILE_TYPE IN ('INSERT','UPDATE'));
ALTER TABLE BATCH_PARTY ADD CONSTRAINT CHK_BATCH_STATUS CHECK (STATUS IN ('SAVED','FINISHED','IN_PROGRESS'));
ALTER TABLE BATCH_PARTY ADD CONSTRAINT CHK_BATCH_TYPE CHECK (TYPE IN ('INDIVIDUAL','BUSINESS'));

--------------------------------------------------------
-- Foreign Keys
--------------------------------------------------------

ALTER TABLE PARTY ADD CONSTRAINT FK_PARTY_DEACT_REASON FOREIGN KEY (DEACTIVATION_REASONS_ID)
    REFERENCES DEACTIVATION_REASONS (ID);

ALTER TABLE PARTY_INDIVIDUAL ADD CONSTRAINT FK_PARTY_INDIVIDUAL FOREIGN KEY (PARTY_ID)
    REFERENCES PARTY (ID);

ALTER TABLE PARTY_BUSINESS ADD CONSTRAINT FK_PARTY_BUSINESS FOREIGN KEY (PARTY_ID)
    REFERENCES PARTY (ID);

ALTER TABLE PARTY_PHONE ADD CONSTRAINT FK_PARTY_PHONE FOREIGN KEY (PARTY_ID)
    REFERENCES PARTY (ID);

ALTER TABLE PARTY_CUSTOMER_BANK_ACCOUNT ADD CONSTRAINT FK_PARTY_CUSTBANK FOREIGN KEY (PARTY_ID)
    REFERENCES PARTY (ID);

ALTER TABLE PARTY_ADDRESS ADD CONSTRAINT FK_PARTY_ADDRESS FOREIGN KEY (PARTY_ID)
    REFERENCES PARTY (ID);

ALTER TABLE HISTORY_DEACTIVATION_REASONS ADD CONSTRAINT FK_HISTORY_PARTY FOREIGN KEY (PARTY_ID)
    REFERENCES PARTY (ID);

ALTER TABLE HISTORY_DEACTIVATION_REASONS ADD CONSTRAINT FK_HISTORY_REASON FOREIGN KEY (DEACTIVATION_REASONS_ID)
    REFERENCES DEACTIVATION_REASONS (ID);

-- Audit Table Foreign Keys
ALTER TABLE DEACTIVATION_REASONS_AUD ADD CONSTRAINT FK_DEACT_REASON_AUD_REV FOREIGN KEY (REV)
    REFERENCES REVINFO (REV);

ALTER TABLE HISTORY_DEACTIVATION_REASONS_AUD ADD CONSTRAINT FK_HISTORY_REASON_AUD_REV FOREIGN KEY (REV)
    REFERENCES REVINFO (REV);

ALTER TABLE PARTY_AUD ADD CONSTRAINT FK_PARTY_AUD_REV FOREIGN KEY (REV)
    REFERENCES REVINFO (REV);

ALTER TABLE PARTY_BUSINESS_AUD ADD CONSTRAINT FK_PARTY_BUSINESS_AUD FOREIGN KEY (REV)
    REFERENCES REVINFO (REV);

ALTER TABLE PARTY_INDIVIDUAL_AUD ADD CONSTRAINT FK_PARTY_INDIVIDUAL_AUD FOREIGN KEY (REV)
    REFERENCES REVINFO (REV);
--------------------------------------------------------
-- Initial Data Inserts
--------------------------------------------------------

-- STATUS

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '1',  'INPUT_PARAMETER_NOT_VALID','پارامتر ورودی نامعتبر', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '2',  'MORE_THAN_ONE_RECORD','بیش از یک رکورد یافت شد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '3',  'PARTY_NATURAL_EXIST','شخص حقیقی موجود است', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '4',  'PARTY_NATURAL_NOT_FOUND','شخص حقیقی یافت نشد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '5',  'TOKEN_NOT_VALID','توکن معتبر نیست', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '6',  'USER_NOT_PERMISSION','کاربر مجوز ندارد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '7',  'BATCH_FILE_ERROR','خطا در فایل دسته‌ای', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '8',  'BATCH_FILE_DUPLICATE_ROW','ردیف تکراری در فایل دسته‌ای', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '9',  'PARTY_BUSINESS_EXIST','شخص حقوقی موجود است', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '10', 'PARTY_BUSINESS_NOT_FOUND','شخص حقوقی یافت نشد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '11', 'PARTY_EXIST','شخص موجود است', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '12', 'PARTY_NOT_FOUND','شخص یافت نشد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '13', 'DEACTIVATED_REASON_NOT_FOUND','علت غیرفعالسازی یافت نشد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '14', 'SADAD_SERVICE_CALL_ERROR','خطا در فراخوانی سرویس سداد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '15', 'BATCH_FILE_FORMAT_AND_TYPE','خطا در فرمت و نوع فایل دسته‌ای', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '16', 'DEACTIVATION_REASON_IS_DUPLICATE','علت غیرفعالسازی قبلاً تعریف شده است', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '17', 'DEACTIVATION_REASON_NOT_FOUND','علت غیرفعالسازی موجود نیست', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '18', 'BATCH_FILE_STATUS_IS_NOT_VALID','وضعیت فایل ارسالی تمام شده است و امکان پردازش نیست', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '19', 'NATIONAL_CODE_INVALID_FROM_SADAD','کد ملی ارسال‌شده نامعتبر است', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '20', 'CUSTOMER_INFO_NOT_FOUND_FROM_SADAD','اطلاعات مشتری در سامانه سداد یافت نشد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '21', 'GET_UNKNOWN_ERROR_FROM_SADAD','خطای ناشناخته از سامانه سداد دریافت شد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '22', 'CUSTOMER_NOT_FOUND_FROM_SADAD','مشتری مورد نظر در سامانه سداد یافت نشد', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '999', 'GENERAL_ERROR','خطای عمومی', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (card_status_seq.NEXTVAL, '57', 'REASON_IS_MANDATORY','وارد کردن دلیل الزامی است', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);

-- DEACTIVATION_REASONS
INSERT INTO DEACTIVATION_REASONS (ACTIVE, CODE, CREATED_DATE, ID, TITLE, CREATED_BY)
VALUES (1, 1, SYSTIMESTAMP, 1, N'نامه قضایی', 'system');

INSERT INTO DEACTIVATION_REASONS (ACTIVE, CODE, CREATED_DATE, ID, TITLE, CREATED_BY)
VALUES (1, 2, SYSTIMESTAMP, 2, N'نامه بانک', 'system');

INSERT INTO DEACTIVATION_REASONS (ACTIVE, CODE, CREATED_DATE, ID, TITLE, CREATED_BY)
VALUES (1, 3, SYSTIMESTAMP, 3, N'نامه از سایر ارگان‌های ذیصلاح', 'system');

INSERT INTO DEACTIVATION_REASONS (ACTIVE, CODE, CREATED_DATE, ID, TITLE, CREATED_BY)
VALUES (1, 4, SYSTIMESTAMP, 4, N'درخواست مشتری', 'system');

INSERT INTO DEACTIVATION_REASONS (ACTIVE, CODE, CREATED_DATE, ID, TITLE, CREATED_BY)
VALUES (1, 5, SYSTIMESTAMP, 5, N'سایر', 'system');

COMMIT;
