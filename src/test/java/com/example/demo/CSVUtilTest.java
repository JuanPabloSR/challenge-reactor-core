package com.example.demo;

import com.example.demo.Models.Player;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

public class CSVUtilTest {

    @Test
    void converterData(){
        List<Player> list = CsvUtilFile.getPlayers();
        assert list.size() == 18207;
    }

    @Test
    void stream_filtrarJugadoresMayoresA35(){
        List<Player> list = CsvUtilFile.getPlayers();
        Map<String, List<Player>> listFilter = list.parallelStream()
                .filter(player -> player.age >= 35)
                .map(player -> {
                    player.name = player.name.toUpperCase(Locale.ROOT);
                    return player;
                })
                .flatMap(playerA -> list.parallelStream()
                        .filter(playerB -> playerA.club.equals(playerB.club))
                )
                .distinct()
                .collect(Collectors.groupingBy(Player::getClub));

        assert listFilter.size() == 322;
    }


    @Test
    void reactive_filtrarJugadoresMayoresA35(){
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listFilter = listFlux
                .filter(player -> player.age >= 35)
                .map(player -> {
                    player.name = player.name.toUpperCase(Locale.ROOT);
                    return player;
                })
                .buffer(100)
                .flatMap(playerA -> listFlux
                        .filter(playerB -> playerA.stream()
                                .anyMatch(a ->  a.club.equals(playerB.club)))
                )
                .distinct()
                .collectMultimap(Player::getClub);

        assert listFilter.block().size() == 322;
    }

    @Test
    void filtrarJugadoresMayores34() {
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listFilter = listFlux
                .filter(player -> player.age >= 34 && player.club.equals("FC Schalke 04"))
                .distinct()
                .collectMultimap(Player::getClub);

        System.out.println("Equipo");
        listFilter.block().forEach((equipo, players) -> {
            System.out.println(equipo);
            players.forEach(player -> {
                System.out.println(player.name + " " + player.age);
                assert player.club.equals("FC Schalke 04");
            });
        });
        assert listFilter.block().size() == 1;
    }

    @Test
    void filtrarRankingVictoriasNacionalidad() {
        List<Player> list = CsvUtilFile.getPlayers();
        Flux<Player> listFlux = Flux.fromStream(list.parallelStream()).cache();
        Mono<Map<String, Collection<Player>>> listFilter = listFlux
                .buffer(100)
                .flatMap(playerA -> listFlux
                        .filter(playerB -> playerA.stream()
                                .anyMatch(a -> a.national.equals(playerB.national)))
                ).distinct()
                .sort((k, player) -> player.winners)
                .collectMultimap(Player::getNational);

        System.out.println("Por Nacionalidad: ");
        System.out.println(listFilter.block().size());
        listFilter.block().forEach((pais, players) -> {
            System.out.println("Pais: " + pais);
            players.forEach(player -> {
                System.out.println(player.name + " victorias: " + player.winners);
            });
        });
    }

}