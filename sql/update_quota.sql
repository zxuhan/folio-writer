-- User quota upgrade script

use folio;

-- Add the quota column
ALTER TABLE user ADD COLUMN quota int default 5 not null comment 'Remaining quota' AFTER userRole;

-- Set the default quota for existing users
UPDATE user SET quota = 5 WHERE quota IS NULL;
