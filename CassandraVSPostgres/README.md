# Cassandra VS PostgreSQL

The purpose of this project is to test the feasibility of using PostgreSQL instead of Cassandra:

* The test was made with a CSV file around 10k records (the results would probably change with a larger dataset, in favor of Cassandra)
* It was executed in a virtual machine with 4084MB of RAM with Fedora 26 
* The average time of saving on Cassandra was 30 seconds and on PostgreSQL 20 seconds

I can conclude that is safe to change to PostgreSQL to save the interactions.
