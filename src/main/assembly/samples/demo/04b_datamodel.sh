
sudo su postgres

createuser -W -D -P tiendavirtual
# Enter password for new role: tiendavirtual 
# Enter it again: tiendavirtual
# Shall the new role be a superuser? (y/n) n
# Shall the new role be allowed to create more new roles? (y/n) n

createdb -E UTF-8 -O tiendavirtual tiendavirtualDB

# Load the database dump using command below:
psql -h localhost -f 04b_datamodel.sql -U tiendavirtual tiendavirtualDB
# Contraseña para usuario tiendavirtual: tiendavirtual

