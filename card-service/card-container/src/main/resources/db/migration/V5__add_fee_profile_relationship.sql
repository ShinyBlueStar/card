-- V5__add_fee_profile_relationship.sql
-- Migration for FeeProfile relationship with CardProfile

-- Add FEE_PROFILE_ID column to CardProfile table
ALTER TABLE card_types ADD FEE_PROFILE_ID NUMBER(19);

-- Add foreign key constraint for FeeProfile relationship
ALTER TABLE card_types ADD CONSTRAINT fk_card_profile_fee_profile
    FOREIGN KEY (FEE_PROFILE_ID) REFERENCES FEE_PROFILES(FEE_PROFILE_ID);

-- Create index for better performance
CREATE INDEX idx_card_profile_fee_profile ON card_types(FEE_PROFILE_ID);

-- Add comment for documentation
COMMENT ON COLUMN card_types.FEE_PROFILE_ID IS 'Reference to FeeProfile (optional)';
