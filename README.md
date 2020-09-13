# gradu
gradukoodi

Steps:

1. Download the source code

2. mvn compile

3. mvn exec:java -Dexec.mainClass=com.cypherTest.cypherTest.App -Dexec.args="[PARAMETRIT]".

Example of command nr 2: mvn exec:java -Dexec.mainClass=com.cypherTest.cypherTest.App -Dexec.args="* mydatabase mypassword bolt://localhost:1234".
