# gradu
gradukoodi

Steps:

1. Download the source code

2. mvn compile

3. mvn exec:java -Dexec.mainClass=com.cypherTest.cypherTest.App -Dexec.args="[XPATH_QUERY NEO4J_DB_NAME NEO4J_DB_PASSWORD (optional: NEO4J_DB_ADDRESS)]".

Examples of command nr 3:

mvn exec:java -Dexec.mainClass=com.cypherTest.cypherTest.App -Dexec.args="a/b/c mydatabase mypassword"

mvn exec:java -Dexec.mainClass=com.cypherTest.cypherTest.App -Dexec.args="* mydatabase mypassword bolt://localhost:1234"
