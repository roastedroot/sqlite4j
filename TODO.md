Keeping track of the loose ends:

NOTES:
- WAL journal mode is not supported, tests are commented with // TODO: WAL
- github720_Incorrect_Update_Count_After_Deleting_Many_Rows is really slow compared to the JNI version: this test is causing the memory_grow opcode to be called a lot of times -> will be improved by the new memory allocation strategy in Chicory
- disabled most of the ErrorMessageTest tests as they rely on dynamically moving files around, which is not supported
- disabled the tests relying on multiple threads as it's not supported and shared_cache -> attempts to share the same instance show issues with the threading model

- write a new Memory that will bulk grow -> alternatively we can explore looking at: SQLITE_ENABLE_MEMSYS3 -> initial proposal
- jimfs without Guava?
