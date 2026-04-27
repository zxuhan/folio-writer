# Database initialization (base tables)
# Note: this file only contains the base table schema.
# Other columns are added by incremental SQL files.

-- Set character set (avoids garbled text)
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Create the database
create database if not exists folio CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Switch to the database
use folio;

-- User table (base fields; quota and vipTime are added by incremental scripts)
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment 'Account',
    userPassword varchar(512)                           not null comment 'Password',
    userName     varchar(256)                           null comment 'User nickname',
    userAvatar   varchar(1024)                          null comment 'User avatar',
    userProfile  varchar(512)                           null comment 'User profile',
    userRole     varchar(256) default 'user'            not null comment 'User role: user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment 'Edit time',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment 'Create time',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update time',
    isDelete     tinyint      default 0                 not null comment 'Soft delete flag',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment 'User' collate = utf8mb4_unicode_ci;

-- Seed data
-- Password is 12345678 (MD5 with salt 'zxuhan')
INSERT INTO user (id, userAccount, userPassword, userName, userAvatar, userProfile, userRole) VALUES
(1, 'admin', 'a1162ae11f397fc2d3c90af0902d9452', 'Admin', '', 'System administrator', 'admin'),
(2, 'user', 'a1162ae11f397fc2d3c90af0902d9452', 'User', '', 'I am a regular user', 'user'),
(3, 'test', 'a1162ae11f397fc2d3c90af0902d9452', 'Test', '', 'This is a test account', 'user'),
(4, 'vip', 'a1162ae11f397fc2d3c90af0902d9452', 'VIP', '', 'I am a VIP demo account', 'vip');

-- Article table (base fields; style/phase/titleOptions/userDescription/enabledImageMethods are added by incremental scripts)
create table if not exists article
(
    id              bigint auto_increment comment 'id' primary key,
    taskId          varchar(64)                        not null comment 'Task ID (UUID)',
    userId          bigint                             not null comment 'User ID',
    topic           varchar(500)                       not null comment 'Topic',
    mainTitle       varchar(200)                       null comment 'Main title',
    subTitle        varchar(300)                       null comment 'Subtitle',
    outline         json                               null comment 'Outline (JSON)',
    content         text                               null comment 'Content (Markdown)',
    fullContent     text                               null comment 'Full illustrated content (Markdown, with images)',
    coverImage      varchar(512)                       null comment 'Cover image URL',
    images          json                               null comment 'Image list (JSON array; cover image has position=1)',
    status          varchar(20) default 'PENDING'      not null comment 'Status: PENDING/PROCESSING/COMPLETED/FAILED',
    errorMessage    text                               null comment 'Error message',
    createTime      datetime    default CURRENT_TIMESTAMP not null comment 'Create time',
    completedTime   datetime                           null comment 'Completion time',
    updateTime      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update time',
    isDelete        tinyint     default 0              not null comment 'Soft delete flag',
    UNIQUE KEY uk_taskId (taskId),
    INDEX idx_userId (userId),
    INDEX idx_status (status),
    INDEX idx_createTime (createTime),
    INDEX idx_userId_status (userId, status)
) comment 'Article' collate = utf8mb4_unicode_ci;

-- Agent execution log table
create table if not exists agent_log
(
    id              bigint auto_increment comment 'id' primary key,
    taskId          varchar(64)                        not null comment 'Task ID',
    agentName       varchar(50)                        not null comment 'Agent name',
    startTime       datetime                           not null comment 'Start time',
    endTime         datetime                           null comment 'End time',
    durationMs      int                                null comment 'Duration (ms)',
    status          varchar(20)                        not null comment 'Status: SUCCESS/FAILED',
    errorMessage    text                               null comment 'Error message',
    prompt          text                               null comment 'Prompt used',
    inputData       json                               null comment 'Input data (JSON)',
    outputData      json                               null comment 'Output data (JSON)',
    createTime      datetime    default CURRENT_TIMESTAMP not null comment 'Create time',
    updateTime      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update time',
    isDelete        tinyint     default 0              not null comment 'Soft delete flag',
    INDEX idx_taskId (taskId),
    INDEX idx_agentName (agentName),
    INDEX idx_status (status),
    INDEX idx_createTime (createTime)
) comment 'Agent execution log' collate = utf8mb4_unicode_ci;
