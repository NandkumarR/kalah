package com.backbase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author nandk on 06/03/2021.
 * Game object that holds the data params of the game. Every move of the game is stored in the NoSQL DB so as to have
 * a future reference of the game and also use for game analytics.
 * @id  - game id that stores the primary identifier of any game
 * @url - url that retrieves the current game status.
 * @currentGameStats - At any given point of time, maintains the current moves of the players.
 * @status - Current status of the game in readable format. Not stored in DB
 * @nextPlayer - Next Player who can make a move.
 */
@Document
@Data
@NoArgsConstructor
public class Game implements Serializable {
    @Id
    private String id;
    private String url;
    private List<Entity> currentGameStats;
    @Transient
    private String status;
    private Player nextPlayer;
    private Player winnerPlayer;

    public Game(String id, List<Entity> currentGameStats, Player nextPlayer, String url){
        this.id=id;
        this.currentGameStats=currentGameStats;
        this.nextPlayer=nextPlayer;
        this.url=url.contains(id)?url:url.concat(id);
        this.winnerPlayer=null;
    }


    public Player getNextPlayer(){
        return this.nextPlayer;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Player getWinnerPlayer(){
        return this.winnerPlayer;
    }

    @JsonIgnore
    public List<Entity> getCurrentGameStats(){
        return this.currentGameStats;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getStatus(){
        return currentGameStats.stream()
                               .collect(Collectors.toMap(e->currentGameStats.indexOf(e)+1,e->e.getEntityCount()))
                               .toString();
    }

}
