Keeping track of the loose ends:

- WAL journal mode is not supported, tests are commented with 
- github720_Incorrect_Update_Count_After_Deleting_Many_Rows is really slow compared to the JNI version: this test is causing the memory_grow opcode to be called a lot of times
- disabling BusyHandlerTest.testMultiThreaded as it's not clear how it should work, get back to it
- disabled most of the ErrorMessageTest tests as they rely on dynamically moving files around, which is not supported
- SerializeTest.testMultiDeserialize is growing the memory too much, a lower cardinality works as expected ...
