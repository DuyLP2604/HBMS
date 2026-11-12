-- Select the existing HBMS database in SSMS, then run this script.
DECLARE @TargetDatabase SYSNAME = N'QLKS';
IF DB_NAME() <> @TargetDatabase
    THROW 51600, 'Select the HBMS database before running this migration.', 1;

IF OBJECT_ID(N'dbo.USERS', N'U') IS NULL OR OBJECT_ID(N'dbo.EMPLOYEE', N'U') IS NULL
    THROW 51601, 'This migration requires the HBMS USERS and EMPLOYEE tables.', 1;

IF COL_LENGTH(N'dbo.USERS', N'IsActive') IS NULL
    ALTER TABLE dbo.USERS ADD IsActive BIT NOT NULL CONSTRAINT DF_USERS_IsActive DEFAULT (1);
