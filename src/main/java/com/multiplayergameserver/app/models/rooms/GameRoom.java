package com.multiplayergameserver.app.models.rooms;

import com.multiplayergameserver.app.models.game.AbstractGame;
import com.multiplayergameserver.app.models.game.GameFactory;
import com.multiplayergameserver.app.models.game.PlayerFactory;
import com.multiplayergameserver.app.models.messages.Action;
import com.multiplayergameserver.app.models.messages.Message;

import com.multiplayergameserver.app.models.messages.WarnMessage;
import lombok.Getter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Getter
public class GameRoom extends AbstractRoom {

    private final String host;
    private final AbstractGame game;

    public GameRoom(String roomId,
                    String title,
                    boolean active,
                    String host,
                    SimpMessagingTemplate template,
                    GameFactory gameFactory,
                    PlayerFactory playerFactory) {
        super(roomId, title, active, template);
        this.host = host;
        this.game = gameFactory.createGame(this, playerFactory);
    }

    public void start() {
        game.start();
    }

    public void end() {
        game.end();
    }

    @Override
    public void process(String username, Message message) {

        if (message instanceof Action)
            game.process(username, (Action) message);
        else
            sendUser(username, new WarnMessage("warning"));
    }
}
