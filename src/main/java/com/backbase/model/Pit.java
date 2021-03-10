package com.backbase.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nandk on 06/03/2021.
 * Entity of type Pit that is owned by a player and also
 * where the game begins.
 */
public class Pit implements Entity {

    private static final Integer INDIVIDUAL_PITS = 6;

    private Integer entityCount;

    private Player ownerPlayer;

    public Pit(Player ownerPlayer, Integer entityCount) {
        this.ownerPlayer = ownerPlayer;
        this.entityCount = entityCount;
    }

    public EntityType getEntityType() {
        return EntityType.PIT;
    }

    public Player getOwnerPlayer() {
        return this.ownerPlayer;
    }

    public Integer getEntityCount() {
        return this.entityCount;
    }

    public void setEntityCount(Integer setCount) {
        this.entityCount = setCount;
    }

    /**
     * Builder class to create Pit entity.
     */
    public static class PitBuilder {

        Integer initialStones;
        Player player;

        PitBuilder initialStones(Integer initialStones) {
            this.initialStones = initialStones;
            return this;
        }

        PitBuilder ownerPlayer(Player ownerPlayer) {
            this.player = ownerPlayer;
            return this;
        }

        List<Entity> build() {
            List<Entity> createList = new ArrayList<>();
            while (createList.size() != INDIVIDUAL_PITS) {
                createList.add(new Pit(this.player, this.initialStones));
            }
            return createList;
        }
    }
}
