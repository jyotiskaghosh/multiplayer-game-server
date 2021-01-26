package com.multiplayergameserver.app.controllers;

import com.multiplayergameserver.app.game.GameFactory;
import com.multiplayergameserver.app.models.messages.CreateGameMessage;
import com.multiplayergameserver.app.models.messages.Message;
import com.multiplayergameserver.app.models.rooms.GameRoom;
import com.multiplayergameserver.app.models.rooms.AbstractRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class RoomController {

    @Autowired
    private GameFactory gameFactory;

    private final Set<AbstractRoom> rooms = new HashSet<>();

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/new/game")
    public Mono<String> newGame(@RequestBody CreateGameMessage message, Principal principal) {
        String roomId = UUID.randomUUID().toString();
        rooms.add(new GameRoom(
                roomId,
                message.getTitle(),
                principal.getName(),
                gameFactory));
        return Mono.just(roomId);
    }

    @GetMapping("/rooms/games")
    public Map.Entry<String, List<GameRoom>> getGames() {
        return Map.entry("games",
                rooms
                .stream()
                .filter(room -> room instanceof GameRoom)
                .map(room -> (GameRoom) room)
                .collect(Collectors.toList())
        );
    }

    @PreAuthorize("hasAuthority('USER')")
    @MessageMapping("rooms.{roomId}")
    public Flux<Message> processGame(@DestinationVariable String roomId,
                                     Flux<Message> messages,
                                     Principal principal,
                                     RSocketRequester requester) {
        AbstractRoom room = rooms
                .stream()
                .filter(r -> r.getRoomId().equals(roomId))
                .findAny()
                .orElseThrow();
        messages.subscribe(message -> room.process(principal.getName(), requester, message));
        return Flux.from(room);
    }
}
