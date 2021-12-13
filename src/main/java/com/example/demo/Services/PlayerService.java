package com.example.demo.Services;

import com.example.demo.Models.Player;
import com.example.demo.Repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;


    public Mono<Player> save(Player player){
        return playerRepository.save(player);
    }

    public Flux<Player> getAll(){
        return playerRepository
                .findAll()
                .buffer(50)
                .flatMap(p -> Flux.fromStream(p.stream()));
    }

    public Flux<Player> filterOlderThan(int age){
        return getAll()
                .buffer(50)
                .flatMap(player -> Flux.fromStream(player.parallelStream()))
                .filter(player ->{
                    try {
                        return player.getAge() > age;
                    }
                    catch (Exception e){
                        return false;
                    }
                });
    }

    public Flux<Player> filterByClub(String club){
        return getAll()
                .buffer(50)
                .flatMap(p -> Flux.fromStream(p.parallelStream()))
                .filter(player -> {
                    try {
                        return player.getClub().equals(club);
                    }
                    catch (Exception e){
                        return false;
                    }
                });
    }

    public Flux<List<Player>> getRanking(){
        return getAll()
                .buffer(50)
                .flatMap(player -> Flux.fromStream(player.parallelStream()))
                .distinct()
                .groupBy(Player::getNational)
                .flatMap(Flux::collectList)
                .sort();
    }
}