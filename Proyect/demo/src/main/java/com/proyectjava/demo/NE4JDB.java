package com.proyectjava.demo;


import java.util.ArrayList;
import java.util.List;

//import com.service.dto.PersonDTO;

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
            Result result = tx.run( "MATCH (p:USER {username: \"" + username + "\"}) RETURN p.password");
            List<Record> reco = result.list(); //lista de coincidencias
            if (reco.isEmpty()){
                return null;
            }else{
                PersonDTO usertemp = new PersonDTO();
                usertemp.setUsername(username);
                usertemp.setPassword(reco.get(0).get("p.password").asString());
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
            Result result = tx.run( "MATCH (p:USER {username: \"" + username + "\"}) RETURN p");
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
            String password = Person.getPassword();
            tx.run("CREATE (:USER {username: \"" + username + "\" , password: \"" + password + "\"})");
            Result result = tx.run( "MATCH (p:USER {username: \"" + username + "\"}) RETURN p");
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
//Obtnere los Juegos de una categoria
public List<juegos> getGames(String category){
    try(Session session = driver.session()){
        List<juegos> games = session.readTransaction(new TransactionWork<List<juegos>>() {
            @Override
            public List<juegos> execute(Transaction tx) {
                Result result = tx.run("MATCH (q:GAME)-[:IS]->(:CATEGORY{name: \"" + category + "\"}) RETURN DISTINCT q.name, q.ports");
                List<Record> reco = result.list();
                List<juegos> gaming = new ArrayList<juegos>();
                List<Category> categories = new ArrayList<Category>();
                categories.add(new Category(category));
                for(int i = 0; i < reco.size(); i++){
                    gaming.add((new juegos(reco.get(i).get("q.name").asString(), reco.get(i).get("q.ports").asString())));
                }
                return gaming;
            }
        });
        return games;
    }
}
//recomendar juegos de otras categorias
public List<juegos> GETNEWGAMES(String uname){
    try ( Session session = driver.session() ){
        List<juegos> games = session.readTransaction(new TransactionWork<List<juegos>>() {
            @Override
            public List<juegos> execute(Transaction tx) {
                Result result = tx.run("MATCH (s:USER{username: \"" + uname + "\"})-[:LIKES]->(u:CATEGORY)<-[:LIKES]-(:USER)-[:LIKES]->(p:CATEGORY)<-[:IS]-(q:GAME)  WHERE NOT (s)-[:LIKE]->(q) RETURN DISTINCT q.name ,q.ports LIMIT 10");
                List<Record> reco = result.list();
                List<juegos> gaming = new ArrayList<juegos>();
                for(int i = 0; i < reco.size(); i++){
                    gaming.add(new juegos(reco.get(i).get("q.name").asString(), reco.get(i).get("q.ports").asString()));
                }
                return gaming;
            }
        });
        return games;
    }

}
//obtener Juegos de las categories que le gusten
public List<juegos> GetlikedCatGame(String username){
    try ( Session session = driver.session() ){
        List<juegos> games = session.readTransaction(new TransactionWork<List<juegos>>() {
            @Override
            public List<juegos> execute(Transaction tx) {
                Result result = tx.run("MATCH (s:USER{username: \"" + username + "\"})-[:LIKES]->(u:CATEGORY)<-[:IS]-(q:GAME) WHERE NOT (s)-[:LIKE]->(q) RETURN DISTINCT q.name ,q.ports LIMIT 15");
                List<Record> reco = result.list();
                List<juegos> gaming = new ArrayList<juegos>();
                for(int i = 0; i < reco.size(); i++){
                    gaming.add(new juegos(reco.get(i).get("q.name").asString(),reco.get(i).get("q.ports").asString()));
                }
                return gaming;
            }
        });
        return games;
    }
}

//Obtener datos de un juego
public juegos getGame(String name){
    try ( Session session = driver.session() ){
        juegos games = session.readTransaction(new TransactionWork<juegos>() {
            @Override
            public juegos execute(Transaction tx) {
                Result result = tx.run("MATCH (q:GAME{name: \"" + name + "\"})-[:IS]->(c:CATEGORY) RETURN DISTINCT q.name ,q.logo, q.img1, q.img2, q.img3, q.img4, q.img5,q.meta, q.us, q.desc,q.req, q.autor,q.edit ");
                List<Record> reco = result.list();
                int i = 0;
                String gamename = reco.get(i).get("q.name").asString();
                Result result2 = tx.run( "MATCH (:GAME{name: \"" + gamename +"\"})-[:IS]->(c:CATEGORY) RETURN DISTINCT c.name");
                List<Record> reco2 = result2.list();
                List<Category> categories = new ArrayList<Category>();
                for(int j = 0; j < reco2.size(); j++){
                    categories.add(new Category(reco2.get(j).get("c.name").asString()));
                }
                juegos gaming =(new juegos(reco.get(i).get("q.name").asString(), reco.get(i).get("q.img1").asString(), reco.get(i).get("q.img2").asString(), reco.get(i).get("q.img3").asString(),
                reco.get(i).get("q.img4").asString(), reco.get(i).get("q.img5").asString(),reco.get(i).get("q.logo").asString(),reco.get(i).get("q.meta").asString() ,reco.get(i).get("q.us").asString(),categories,
                reco.get(i).get("q.desc").asString(), reco.get(i).get("q.req").asString(),reco.get(i).get("q.autor").asString(),reco.get(i).get("q.edit").asString()));
                
                return gaming;
            }
        });
        return games;
    }

}
public void addGameLiked(String username, String gameid) {
    try ( Session session = driver.session() ){
        session.writeTransaction(new TransactionWork<String> () {

            @Override
            public String execute(Transaction tx) {
                tx.run("MATCH (u:USER{username: \"" + username + "\"}),(g:GAME{name: \""+gameid + "\"}) MERGE (u)-[:LIKE]->(g)");
                return null;
            }

        });

        }
    }
public void addGameDisliked(String username, String gameid) {
        try ( Session session = driver.session() ){
            session.writeTransaction(new TransactionWork<String> () {
    
                @Override
                public String execute(Transaction tx) {
                    tx.run("MATCH (u:USER{username: \"" + username + "\"})-[r:LIKE]->(g:GAME{name: \""+gameid + "\"}) DELETE r");
                    return null;
                }
    
            });
    
            }
        }
public List<juegos> getLikedGames(String username) {//obtener los juegos favoritos del usuario
    try ( Session session = driver.session() ){
        List<juegos> games = session.readTransaction(new TransactionWork<List<juegos>>() {
            @Override
            public List<juegos> execute( Transaction tx ){
                Result result = tx.run("MATCH (:USER{username: \"" + username + "\"})-[:LIKE]->(q:GAME) RETURN DISTINCT q.name ,q.ports LIMIT 10");
                List<Record> reco = result.list();
                List<juegos> gaming = new ArrayList<juegos>();
                for(int i = 0; i < reco.size(); i++){
                    String gamename = reco.get(i).get("q.name").asString();
                    Result result2 = tx.run( "MATCH (:GAME{name: \"" + gamename +"\"})-[:IS]->(c:CATEGORY) RETURN DISTINCT c.name");
                    List<Record> reco2 = result2.list();
                    List<Category> categories = new ArrayList<Category>();

                    gaming.add((new juegos(reco.get(i).get("q.name").asString(), reco.get(i).get("q.ports").asString())));
                } 
                return gaming;
            }
        });
        return games;
    }
}

public void addCatLiked(String username, String gameid) {
    try ( Session session = driver.session() ){
        session.writeTransaction(new TransactionWork<String> () {

            @Override
            public String execute(Transaction tx) {
                tx.run("MATCH (u:USER{username: \"" + username + "\"}),(g:CATEGORY{name: \""+gameid + "\"}) MERGE (u)-[:LIKES]->(g)");
                return null;
            }

        });

        }
    }
public boolean CheckCategories(String username){
    try ( Session session = driver.session() ){
        Boolean atleast1cat = session.readTransaction(new TransactionWork<Boolean>(){
            @Override
            public Boolean execute(Transaction tx){
                Result result = tx.run("MATCH (:USER{username: \"" + username + "\"})-[:LIKES]->(c:CATEGORY) RETURN DISTINCT c.name");
                List<Record> reco = result.list();
                List<Category> categories = new ArrayList<Category>();
                for(int i = 0; i < reco.size(); i++){
                    categories.add(new Category(reco.get(i).get("c.name").asString()));
                }
                if(categories.size() > 1){
                    return true;
                }
                return false;
            }
        });
        return atleast1cat;
}
}
}




