package com.service.Data;


import java.util.ArrayList;
import java.util.List;

import com.service.dto.PersonDTO;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import static org.neo4j.driver.Values.parameters;
public class NE4JDB implements AutoCloseable{
    private final Driver driver;
    public NE4JDB( String uri, String user, String password )
    { //coneccion con la base de datos
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }
@Override
public void close() throws Exception {
    driver.close(); //cerrar coneccion
}
public void printGreeting( final String message )
    {
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run( "CREATE (a:Greeting) " +
                                                     "SET a.message = $message " +
                                                     "RETURN a.message + ', from node ' + id(a)",
                            parameters( "message", message ) );
                    return result.single().get( 0 ).asString();
                }
            } );
            System.out.println( greeting );
        }
    }

public PersonDTO Login( String username){ //obtener el usuario
    try(Session session = driver.session()){
        PersonDTO user = session.readTransaction( new TransactionWork<PersonDTO>() 
        {
            @Override
        public PersonDTO execute (Transaction tx)
        {
            Result result = tx.run( "MATCH (p:Person {name: \"" + username + "\"}) RETURN p");
            List<Record> reco = result.list(); //lista de coincidencias
            if (reco.isEmpty()){
                return null;
            }else{
                PersonDTO usertemp = new PersonDTO();
                usertemp.setUsername(username);
                usertemp.setPassword(reco.get(0).get("Person.password").asString());
                usertemp.setName(reco.get(0).get("Person.name").asString());
                return usertemp;
            }
        }
        });
        return user;

    }
   
}
public Boolean usernamaeTaken( String username){ //verificar si esta disponible el nombre de usuario
    try(Session session = driver.session()){
        Boolean user = session.readTransaction( new TransactionWork<Boolean>() 
        {
            @Override
        public Boolean execute (Transaction tx)
        {
            Result result = tx.run( "MATCH (p:Person {name: \"" + username + "\"}) RETURN p");
            List<Record> reco = result.list(); //lista de coincidencias
            if (reco.isEmpty()){
                return false;
            }else{
                return true;
            }
        }
        });
        return user;
    }
    
}
public Boolean AddUser(PersonDTO Person){ //Añadir el usuario true:añadido correctamente
    try(Session session = driver.session()){
        Boolean user = session.writeTransaction( new TransactionWork<Boolean>() 
        {
            @Override
        public Boolean execute (Transaction tx)
        {
            String username = Person.getUsername();
            String name = Person.getName();
            String password = Person.getPassword();
            tx.run("CREATE (:Person {username: \"" + username + "\" , name: \"" + name + "\" , password: \"" + password + "\"})");
            Result result = tx.run( "MATCH (p:Person {username: \"" + username + "\"}) RETURN p");
            List<Record> reco = result.list(); //lista de coincidencias
            if (reco.isEmpty()){
                return false;
            }else{
                return true;
            }
        }
        });
        return user;
    }
    
}

}

