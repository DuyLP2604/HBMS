-- Select the existing HBMS database in SSMS, then run this entire script.
DECLARE @TargetDatabase SYSNAME = N'QLKS';
IF DB_NAME() <> @TargetDatabase
    THROW 51700, 'Select the HBMS database before running this migration.', 1;

IF OBJECT_ID(N'dbo.USERS', N'U') IS NULL OR OBJECT_ID(N'dbo.EMPLOYEE', N'U') IS NULL
    THROW 51701, 'This migration requires the HBMS USERS and EMPLOYEE tables.', 1;

IF COL_LENGTH(N'dbo.USERS', N'IsActive') IS NULL
    ALTER TABLE dbo.USERS ADD IsActive BIT NOT NULL CONSTRAINT DF_USERS_IsActive DEFAULT (1);

-- Existing accounts start at version 0. Each access change invalidates prior sessions.
IF COL_LENGTH(N'dbo.USERS', N'AccessVersion') IS NULL
    ALTER TABLE dbo.USERS ADD AccessVersion INT NOT NULL CONSTRAINT DF_USERS_AccessVersion DEFAULT (0);
