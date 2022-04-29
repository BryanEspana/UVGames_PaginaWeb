package com.proyectjava.demo;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.Set;

import org.springframework.data.neo4j.core.schema.*;

@Node
public class Users {
    @Relationship(type = "category")
    private Set<category> gustoCategory;   

    @Relationship(type = "juegos")
    private Set<juegos> gustoGames;

    private String username;
    private String password;
    private Set<fav> GamesFavs;
    private Set<listDeseos> ListaDeseos;
    

    
}
