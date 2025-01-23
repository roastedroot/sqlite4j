Keeping track of the loose ends:

- WAL journal mode is not supported, tests are commented with 

- review the usage of UDFStore and progressHandlerStore to avoid using 0 (which is == NULL) as userData
