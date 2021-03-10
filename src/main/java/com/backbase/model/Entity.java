package com.backbase.model;

/**
 * @author nandk on 06/03/2021.
 * Umbrella Entity that enforces behaviour of different sub-entities (Pit/Kalah).
 * In the Kalah Board every entity is either of type Kalah/House or Pit and they
 * have certain properties like who owns it (Player 1/2) or what is the count of stones it holds.
 */
public interface Entity {
    EntityType getEntityType();
    Player getOwnerPlayer();
    Integer getEntityCount();
}
