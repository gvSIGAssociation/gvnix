
Oracle JDBC driver must be downloaded manually from http://www.oracle.com/technetwork/database/features/jdbc before build the wrapping.
Wrapping is internally configured for use com.oracle.ojdbc6 11.2.0.3 driver, use another at your own risk.
Then run lib/install-lib.sh to install the driver in your maven local repository.
Finally build the wrapping with maven.
