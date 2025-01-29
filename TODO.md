Keeping track of the loose ends:

- WAL journal mode is not supported, tests are commented with // TODO: WAL
- github720_Incorrect_Update_Count_After_Deleting_Many_Rows is really slow compared to the JNI version: this test is causing the memory_grow opcode to be called a lot of times
- disabling BusyHandlerTest.testMultiThreaded as it's not clear how it should work, get back to it
- disabled most of the ErrorMessageTest tests as they rely on dynamically moving files around, which is not supported

- write a new Memory that will bulk grow -> alternatively we can explore looking at: SQLITE_ENABLE_MEMSYS3
- fix the last remaining 8 tests, how should the connection be shared?
