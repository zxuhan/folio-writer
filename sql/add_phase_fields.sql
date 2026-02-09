# Add phase-related columns

use folio;

-- Add phase-related columns to the article table
ALTER TABLE article
    ADD COLUMN phase VARCHAR(50) DEFAULT 'PENDING' COMMENT 'Current phase: PENDING/TITLE_GENERATING/TITLE_SELECTING/OUTLINE_GENERATING/OUTLINE_EDITING/CONTENT_GENERATING' AFTER status,
    ADD COLUMN titleOptions JSON NULL COMMENT 'Title options (3-5 candidates)' AFTER subTitle,
    ADD COLUMN userDescription TEXT NULL COMMENT 'Additional user description' AFTER topic,
    ADD COLUMN enabledImageMethods JSON NULL COMMENT 'Allowed illustration methods' AFTER userDescription;
