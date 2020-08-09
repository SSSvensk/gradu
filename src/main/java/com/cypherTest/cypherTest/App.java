package com.cypherTest.cypherTest;

import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import static org.neo4j.driver.v1.Values.parameters;
/**
 * Hello world!
 *
 */
public class App 
{
    @SuppressWarnings("deprecation")
	public static void main( String[] args ) {
    	try {
    		String inputCmd = null;
    		String databaseName = "";
    		String databasePassword = "";
            if ( args.length>2 ) {
            	inputCmd = args[0];
            	databaseName = args[1];
            	databasePassword = args[2];
            } else {
            	throw new Exception("Parameters not given correctly!\nThere should be 3 string parameters:\n[XPath expression, db name, db password]");
            }
            
            System.out.println("XPath query");
            System.out.println(inputCmd);
            System.out.println();
            System.out.println("Messages from Xypher translator:");
            // create a CharStream that reads from standard input
            ANTLRInputStream input = new ANTLRInputStream(inputCmd);

            // create a lexer that feeds off of input CharStream
            xpathLexer lexer = new xpathLexer(input);

            // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // create a parser that feeds off the tokens buffer
            xpathParser parser = new xpathParser(tokens);

            MyListener mylistener = new MyListener();

            parser.addParseListener(mylistener);
            ParseTree tree = parser.main();    // begin parsing at rule main
            System.out.println("Connecting to Neo4J database " + databaseName + "...");
            
            System.out.println();
            StringBuilder sb = new StringBuilder();
            String cypQ = sb.append(mylistener.getQuery()).toString();
    		Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( databaseName, databasePassword ) );
        	Session session = driver.session();
        	StatementResult stre = session.run(cypQ);
        	List<Record> lr = stre.list();
        	System.out.println("Results of query");
        	System.out.println();
        	Gson gson = new GsonBuilder().setPrettyPrinting().create();
        	int withoutPrint = 0;
        	for (int i = 0; i < lr.size(); i++) {
        		if (i == 10) {
        			withoutPrint = lr.size() - i;
        			break;
        		}
        		String json = gson.toJson(lr.get(i).asMap());
        		System.out.println(json);
        	}
        	if (withoutPrint == 1) {
        		System.out.println("(and " + withoutPrint + " more result)");
        	} else if (withoutPrint > 1) {
        		System.out.println("(and " + withoutPrint + " more results)");
        	}
        	
        	driver.close();
    	} catch (IllegalArgumentException e) {
    		System.out.println(e);
    		System.out.println("Translation process interrupted");
    	} catch (Exception e) {
    		System.out.println("virhe!");
    		System.out.println(e);
    	}
    }
}
