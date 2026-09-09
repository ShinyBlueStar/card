INSERT INTO STATUS (id, code, description, persian_description, created_by, created_date, last_modified_by, last_modified_date)
VALUES (party_status_seq.NEXTVAL, '23', 'BATCH_FILE_SIZE_ERROR','سایز فایل گروهی بیشتر از مقدار تعیین شده است', 'system', SYSTIMESTAMP, 'system', SYSTIMESTAMP);
commit;