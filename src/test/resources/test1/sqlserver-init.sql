CREATE DATABASE TestDB;

USE TestDB;

CREATE TABLE TestTable
(
    ID      INT PRIMARY KEY,
    Message NVARCHAR(100)
);

INSERT INTO TestTable (ID, Message)
VALUES (1, 'foo');

EXEC sys.sp_cdc_enable_db;

EXEC sys.sp_cdc_enable_table
     @source_schema = 'dbo',
     @source_name   = 'TestTable',
     @role_name     = NULL;


