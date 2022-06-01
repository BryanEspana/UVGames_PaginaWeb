package com.proyectjava.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.servlet.ModelAndView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class MVCController {
    private final NE4JDB base = new NE4JDB("bolt://localhost:7687","neo4j","1234");
    private PersonDTO USER;
    @GetMapping("")
    public ModelAndView root() {
        return new ModelAndView("redirect:/main");
    }
    @GetMapping("/search")
    public ModelAndView search(){
        return new ModelAndView("buscar");
    }
    @GetMapping("/main")
    public ModelAndView mainpage() {
        return new ModelAndView("main");
    }
    /*--Acciones de usuario--*/
    
    @GetMapping("/user")
    public ModelAndView checkSession2(){
        if(this.USER == null){
            return new ModelAndView("redirect:/user/login");
        }
        return new ModelAndView("redirect:/user/dashboard");
    }
    @GetMapping("/user/dashboard")
    public ModelAndView UserInterface(){
        var params = new HashMap<String,Object>();
        params.put("listGames", base.GETNEWGAMES(USER.getUsername()));
        params.put("listGamesCat",base.GetlikedCatGame(USER.getUsername()));
        params.put("GamesFAV", base.getLikedGames(USER.getUsername()));
        return new ModelAndView("Perfil", params);
    }

    @GetMapping("/user/login")
    public ModelAndView UserLogInterface(){
        var params = new HashMap<String,Object>();
        params.put("user", (new PersonDTO()));
        return new ModelAndView("login",params);
    }
    @PostMapping("/user/login/check")
    public ModelAndView loginCheck(PersonDTO user){
        System.out.println(user.getUsername());
        PersonDTO DTO = base.Login(user.getUsername());
        if(DTO == null){
            return new ModelAndView("redirect:/user/login");
        }
        if (DTO.getPassword().equals(user.getPassword())){
            this.USER = DTO;
            return new ModelAndView("redirect:/user/dashboard");
        }
        return new ModelAndView("redirect:/user/login");
    }
    @GetMapping("/user/new")
    public ModelAndView UserNewInterface(){
        var params = new HashMap<String,Object>();
        params.put("user", (new PersonDTO()));
        return new ModelAndView("register", params);
    } 
    @PostMapping("/user/new/check")
    public ModelAndView UserNewCheck(PersonDTO user){
        System.out.println(user.getUsername());
        if(base.usernamaeTaken(user.getUsername())){
            return new ModelAndView("redirect:/user/new");
        }
        if(base.AddUser(user)){
            this.USER = user;
            return new ModelAndView("redirect:/gamefav"); /*-solicitarlle los generos favoritos y luego los juegos*/
        }
        return new ModelAndView("redirect:/user/new");
    } 
    @GetMapping("/gamefav")
    public ModelAndView newUsernameGame(){
        return new ModelAndView("gamesfav");
    }
    @GetMapping("/user/new/next")
    public ModelAndView Checker1() {
        return new ModelAndView("redirect:/catfav");
    }
    @GetMapping("/catfav")
    public ModelAndView newUsernameCat(){
        return new ModelAndView("categoryfav");
    }
    @GetMapping("/user/new/next2")
    public ModelAndView Checker2() {
        if(base.CheckCategories(this.USER.getUsername())){
            return new ModelAndView("redirect:/user/dashboard");
        }
        return new ModelAndView("redirect:/catfav");
    }
    @GetMapping("/game/{name}")
    public ModelAndView GameInterface(@ModelAttribute("name") String id){
        var params = new HashMap<String, Object>();
        params.put("game", base.getGame(id));
        return new ModelAndView("gameTem", params);
    }
    @GetMapping("/game")
    public ModelAndView showGameS(){
        return new ModelAndView("juegos");
    }
    @GetMapping("/like/{name}")
    public ModelAndView addLiked(@ModelAttribute("name") String gameid){
        base.addGameLiked(USER.getUsername(), gameid);
        return new ModelAndView("redirect:/gamefav");

    }
    @GetMapping("/likes/{name}")
    public ModelAndView addLikeds(@ModelAttribute("name") String catid){
        base.addCatLiked(USER.getUsername(), catid);
        return new ModelAndView("redirect:/catfav");
    }
    @GetMapping("/Disliked/{name}")
    public ModelAndView Disliked(@ModelAttribute("name") String catid){
        base.addGameDisliked(USER.getUsername(), catid);
        return new ModelAndView("redirect:/user/dashboard");
    }
    @GetMapping("/user/end") //cerrar sesión
    public ModelAndView end(){
        this.USER = null;
        return new ModelAndView("redirect:/main");
    }
    @GetMapping("/addLiked/{name}")
    public ModelAndView add(@ModelAttribute("name") String gameid){
        base.addGameLiked(USER.getUsername(), gameid);
        return new ModelAndView("redirect:/user/dashboard");
    }
    @GetMapping("/error")
    public ModelAndView error(){
        return new ModelAndView("error");
    }
    
}