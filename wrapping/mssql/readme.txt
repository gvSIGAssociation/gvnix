
Microsoft SQL JDBC driver must be downloaded manually from http://msdn.microsoft.com/sqlserver before build the wrapping.
Wrapping is internally configured for use mssql.mssql 4.0.2206.100 driver, use another at your own risk.
Then run lib/install-lib.sh to install the driver in your maven local repository.
Finally build the wrapping with maven.
