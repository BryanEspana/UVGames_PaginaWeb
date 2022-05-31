package com.proyectjava.demo;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class juegos {
    private String name;
    //contenido multimedia del juego
    private String img1;
    private String img2;
    private String img3;
    private String img4;
    private String img5;
    private String logo;
    private String met; //meta score
    private String us;  //user score
    private List<Category> categories = new ArrayList<Category>();
    private String desc;//descripcion
    private String minReq; //requisitos minimos
    private String author;
    private String editor;
    //constructor simple: nombre y logo, empleado para mostrar en swippers, etc
    public juegos(String name, String logo) {
        this.name = name;
        this.logo = logo;
    }
}
