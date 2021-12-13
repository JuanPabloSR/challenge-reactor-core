package com.example.demo.Controllers;

import com.example.demo.Models.Player;
import com.example.demo.Services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("api")
public class PlayerController {

    @Autowired
    PlayerService playerService;

    @GetMapping
    public Flux<Player> getPlayers(){return playerService.getAll();}

    @GetMapping("/ranking")
    public Flux<List<Player>> getRanking(){
        return playerService.getRanking();
    }
}