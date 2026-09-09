-- V6__insert_bank_sample_data.sql
-- Sample data for Bank table

-- Insert comprehensive sample data for Iranian banks
-- Note: DESCRIPTION column removed as it doesn't exist in BankCommandEntity
INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '123456', 'بانک ملی ایران', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '234567', 'بانک صادرات ایران', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '345678', 'بانک تجارت', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '456789', 'بانک ملت', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '567890', 'بانک پارسیان', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '678901', 'بانک پاسارگاد', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '789012', 'بانک سامان', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '890123', 'بانک اقتصاد نوین', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '901234', 'بانک شهر', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '012345', 'بانک دی', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '111111', 'بانک کشاورزی', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '222222', 'بانک صنعت و معدن', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '333333', 'بانک مسکن', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '444444', 'بانک توسعه تعاون', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '555555', 'بانک کارآفرین', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '666666', 'بانک آینده', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '777777', 'بانک انصار', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '888888', 'بانک گردشگری', 1, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '999999', 'بانک سینا', 1, 'SYSTEM', 'SYSTEM');

-- Insert some inactive banks for testing
INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '000001', 'بانک منحل شده 1', 0, 'SYSTEM', 'SYSTEM');

INSERT INTO banks (ID, BIN_CODE, bank_name, IS_ACTIVE, created_by, updated_by) VALUES
(BANK_SEQ.NEXTVAL, '000002', 'بانک منحل شده 2', 0, 'SYSTEM', 'SYSTEM');

-- Commit the transaction
COMMIT;
