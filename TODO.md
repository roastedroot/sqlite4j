Keeping track of the loose ends:

NOTES:
- WAL journal mode is not supported, tests are commented with // TODO: WAL
- github720_Incorrect_Update_Count_After_Deleting_Many_Rows is really slow compared to the JNI version: this test is causing the memory_grow opcode to be called a lot of times
- disabled most of the ErrorMessageTest tests as they rely on dynamically moving files around, which is not supported
- disabled the tests relying on multiple threads as it's not supported and shared_cache

- write a new Memory that will bulk grow -> alternatively we can explore looking at: SQLITE_ENABLE_MEMSYS3
- update the README file
- jimfs without Guava
