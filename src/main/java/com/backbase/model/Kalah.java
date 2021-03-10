package com.backbase.model;

import lombok.Data;

import java.util.List;

/**
 * @author nandk on 06/03/2021.
 * Entity type Kalah/House, that is owned by a player and holds the score
 * of the player.
 */
@Data
public class Kalah implements Entity {

    private Integer entityCount = 0;

    private Player ownerPlayer;


    public Kalah(Player ownerPlayer) {
        this.ownerPlayer = ownerPlayer;
    }

    public EntityType getEntityType() {
        return EntityType.KALAH;
    }

    public Player getOwnerPlayer() {
        return this.ownerPlayer;
    }

    public Integer getEntityCount() {
        return this.entityCount;
    }

    public void setEntityCount(Integer setValue) {
        this.entityCount = setValue;
    }

    /**
     * Builder class to build Kalah/House entity for players.
     */
    public static class KalahBuilder {
        private Player ownerPlayer;

        KalahBuilder ownerPlayer(Player ownerPlayer) {
            this.ownerPlayer = ownerPlayer;
            return this;
        }

        Kalah build() {
            return new Kalah(this.ownerPlayer);
        }

    }

    /**
     * Builder class to build the KalahBoard for players with their own Pits and Kalah/Houses
     */
    public static class KalahBoardBuilder {
        Player ownerPlayer;
        Integer initialStones;

        public KalahBoardBuilder ownerPlayer(Player ownerPlayer) {
            this.ownerPlayer = ownerPlayer;
            return this;
        }

        public KalahBoardBuilder initialStones(Integer initialStones) {
            this.initialStones = initialStones;
            return this;
        }

        public List<Entity> build() {
            List<Entity> buildMyBoard = new Pit.PitBuilder()
                    .ownerPlayer(this.ownerPlayer)
                    .initialStones(this.initialStones)
                    .build();
            buildMyBoard.add(new KalahBuilder()
                    .ownerPlayer(this.ownerPlayer)
                    .build());
            return buildMyBoard;
        }
    }
}
