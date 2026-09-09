-- V8__add_card_id_uuid_to_cards.sql
-- Add CARD_ID column (UUID) to cards table for SSM integration

ALTER TABLE cards ADD CARD_ID VARCHAR2(36 CHAR);

CREATE UNIQUE INDEX idx_cards_card_id ON cards(CARD_ID);

COMMENT ON COLUMN cards.CARD_ID IS 'Unique card identifier (UUID) for SSM; generated on card creation';
