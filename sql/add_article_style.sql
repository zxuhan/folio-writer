# Add article style column

use folio;

-- Add the style column to the article table
ALTER TABLE article
    ADD COLUMN style VARCHAR(20) NULL COMMENT 'Article style: tech/emotional/educational/humorous' AFTER topic;
