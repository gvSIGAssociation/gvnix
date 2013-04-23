
Oracle JDBC driver must be downloaded manually from http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html before build the wrapping.
Wrapping is internally configured for use com.oracle.ojdbc14 10.2.0.5 driver, use another at your own risk.
Then run lib/install-lib.sh to install the driver in your maven local repository.
Finally build the wrapping with maven.
