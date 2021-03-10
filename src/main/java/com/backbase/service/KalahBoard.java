package com.backbase.service;

import com.backbase.exception.KalahValidationException;
import com.backbase.model.Entity;
import com.backbase.model.EntityType;
import com.backbase.model.Game;
import com.backbase.model.Kalah;
import com.backbase.model.Pit;
import com.backbase.model.Player;
import com.backbase.repository.KalahRespository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author nandk on 06/03/2021.
 * Main service class that handles the different requests for game initialization and pit movements.
 */
@Service
public class KalahBoard {

    Logger logger = LoggerFactory.getLogger(KalahBoard.class);

    private static final Integer gameIdentifierLength=6;

    @Resource
    KalahRespository kalahRespository;

    @Value("${application.kalah.initialize.stones}")
    Integer initialStones;

    @Value("${application.kalah.end.point}")
    String kalahApplicationEndPoint;

    /**
     * Game On. Method that initializes the kalah board, the players ,and the stones available in each pit.
     *  This can be configured using property application.kalah.initialize.stones
     * @return Game Object that holds the Kalah Board attributes.
     */
    public Game initializeNewBoardGame(){
       Game newGame = new Game(RandomStringUtils.randomAlphanumeric(gameIdentifierLength),initializePlayersPitAndKalah(),Player.ANY,kalahApplicationEndPoint);
       return kalahRespository.save(newGame);
    }

    /**
     * Make moves using the below method for a game by giving the pit id from which this move should initiate.
     * Method also makes validation of the game and pit used.
     * @param gameId - Initialized game
     * @param pitId - pitId from where the movement begins.
     * @return Game object updated with the current move.
     */
    public Game makeMyMove(String gameId,Integer pitId) {
        Game onGoingGame = validGameParameters(gameId, pitId);
        Player currentPlayer = onGoingGame.getNextPlayer() != Player.ANY ? onGoingGame.getNextPlayer() : Player.PLAYER1;//Default start with player 1.
        List<Entity> currentBoard = onGoingGame.getCurrentGameStats();
        pitId = pitId - 1;//Convert to our indexing
        Entity chosenEntity = currentBoard.get(pitId);
        int noOfMoves = isMoveValid(chosenEntity, currentPlayer);
        ((Pit) chosenEntity).setEntityCount(0);
        // Make moves to release stones on each pit/kalah
        while (noOfMoves != 0) {
            pitId = pitId == currentBoard.size() - 1 ? 0 : pitId + 1;
            chosenEntity = currentBoard.get(pitId);
            //Stone will be release to current Player's owned Kalah only.
            if (chosenEntity instanceof Kalah && chosenEntity.getOwnerPlayer() == currentPlayer) {
                ((Kalah) chosenEntity).setEntityCount(chosenEntity.getEntityCount() == null ? 1 : chosenEntity.getEntityCount() + 1);
            } else if (chosenEntity instanceof Kalah && chosenEntity.getOwnerPlayer() != currentPlayer) {
                continue;
            } else {
                ((Pit) chosenEntity).setEntityCount(chosenEntity.getEntityCount() + 1);
            }
            noOfMoves--;
        }
        //If the move ends at players own empty pit  opposite players stone are also captured and moved to current players house/kalah.
        if (currentBoard.get(pitId) instanceof Pit && currentBoard.get(pitId).getOwnerPlayer() == currentPlayer && currentBoard.get(pitId).getEntityCount() == 1) {
            Pit oppositePit = (Pit) currentBoard.get(currentBoard.size() - pitId - 2);
            Kalah currentPlayersKalah = (Kalah) currentBoard.stream()
                    .filter(e -> e.getEntityType() == EntityType.KALAH && e.getOwnerPlayer() == currentPlayer)
                    .findFirst()
                    .get();
            currentPlayersKalah.setEntityCount(currentPlayersKalah.getEntityCount() + oppositePit.getEntityCount() + currentBoard.get(pitId).getEntityCount());
            ((Pit) currentBoard.get(pitId)).setEntityCount(0);
            oppositePit.setEntityCount(0);
            onGoingGame.setNextPlayer(currentPlayer != Player.PLAYER1 ? Player.PLAYER1 : Player.PLAYER2);
        }
        //If the players last stone lands in his own Kalah/House, he gets another turn
        else if(currentBoard.get(pitId) instanceof Kalah && currentBoard.get(pitId).getOwnerPlayer()==currentPlayer){
            onGoingGame.setNextPlayer(currentPlayer);
        }else{
            onGoingGame.setNextPlayer(currentPlayer!=Player.PLAYER1?Player.PLAYER1:Player.PLAYER2);
        }
        //After the move is complete check if we already have a winner
        Player winnerPlayer=doWeHaveAWinner(currentBoard);
        if (winnerPlayer!=null){
            onGoingGame.setNextPlayer(null);
            onGoingGame.setWinnerPlayer(winnerPlayer);
            logger.info("We have a winner for Game : "+ gameId + " Winner is : "+winnerPlayer);
        }
        return kalahRespository.save(onGoingGame);
    }

    /**
     * Validates if valid Game parameters are supplied.
     * 1. Already initialised on the board
     * 2. Has not ended already.
     * 3. pitId is within range.
     * @param gameId
     * @return Game object with current game stats and attributes. Else throws an {@link KalahValidationException}.
     */
    private Game validGameParameters(String gameId,Integer pitId){
        Optional<Game> validateGame= kalahRespository.findById(gameId);
        if (!validateGame.isPresent()){
            throw new KalahValidationException("GameID does exist : "+gameId);
        }
        if (validateGame.get().getCurrentGameStats().stream()
                                                .filter(p->p.getEntityType()==EntityType.PIT)
                                                .map(e->e.getEntityCount())
                                                .reduce(Integer::sum).get()==0){
            logger.info("Game "+ gameId+ "has ended already. Please start a new game ");
            throw new KalahValidationException("Game has ended already ");
        }
        if (pitId < 0 || pitId >14 ){
            throw new KalahValidationException("Invalid Pit Id used.");
        }
        return validateGame.get();
    }

    /**
     * Validates the move that is being made.
     * 1. Current player owns the Pit chosen for the move.
     * 2. The move begins from a Pit and NOT from Kalah.
     * 3. If the Pit contains stones for any movement to happen.
     * @param chosenEntity
     * @param currentPlayer
     * @return number of stones (number of moves) that are available in the chosen Pit.
     */
    private Integer isMoveValid(Entity chosenEntity,Player currentPlayer){
        if(chosenEntity.getOwnerPlayer()!=currentPlayer){
            throw new KalahValidationException("Current Player doesn't own the pit : "+currentPlayer);
        }
        if(chosenEntity.getEntityType()!=EntityType.PIT){
            throw new KalahValidationException("Move not allowed from Kalah : "+currentPlayer);
        }
        int noOfMoves=chosenEntity.getEntityCount();
        if (noOfMoves==0){
            throw new KalahValidationException("Move not allowed from Pit.No stones available ");
        }
        return noOfMoves;
    }

    /**
     * After a move has been completed the board is checked for identifying if we already have a winner.
     * Validates the PIT stones for any player has all become 0 and then compares their Kalah/House scores.
     * @param currentBoard
     * @return Player that may have won else null object is returned.
     */
    private Player doWeHaveAWinner(List<Entity> currentBoard){
        Player winner=null;
        Map<Player,Integer> playersPitScore=currentBoard.stream().filter(k->k.getEntityType()==EntityType.PIT)
                .collect(Collectors.groupingBy(Entity::getOwnerPlayer,Collectors.summingInt(Entity::getEntityCount)));
        Map<Player,Integer> playersHouseScore=currentBoard.stream().filter(k->k.getEntityType()==EntityType.KALAH)
                .collect(Collectors.groupingBy(Entity::getOwnerPlayer,Collectors.summingInt(Entity::getEntityCount)));
        //Default a winner(if available) for identifying the top scorer.
        if (playersPitScore.get(Player.PLAYER1)==0){
            winner=Player.PLAYER2;
        }else if(playersPitScore.get(Player.PLAYER2)==0){
            winner=Player.PLAYER1;
        }
        if (winner!=null){
            playersHouseScore.put(winner,playersHouseScore.get(winner)+playersPitScore.get(winner));
            for (Entity entity:currentBoard){
                if (entity.getEntityType().equals(EntityType.KALAH) && entity.getOwnerPlayer().equals(winner)){
                    ((Kalah)entity).setEntityCount(playersHouseScore.get(winner));
                }else if (entity.getEntityType().equals(EntityType.PIT) && entity.getOwnerPlayer().equals(winner)){
                    ((Pit)entity).setEntityCount(0);
                }
            }
            //top scorer.
            winner=playersHouseScore.get(Player.PLAYER1)>playersHouseScore.get(Player.PLAYER2)?Player.PLAYER1:Player.PLAYER2;
        }
        return winner;
    }

    /**
     * Fetch the Game object for a gameId given
     * @param gameId
     * @return Game object with current stats.
     */
    public Game fetchKalahBoardForGameId(String gameId){
        Optional<Game> fetchedGame= kalahRespository.findById(gameId);
        if(!fetchedGame.isPresent()){
            throw new KalahValidationException("GameID does exist : "+gameId);
        }
        return fetchedGame.get();
    }

    /**
     * Remove a game from backend.
     * @param gameId
     */
    public void cleanUpKalahBoard(String gameId){
        kalahRespository.deleteById(gameId);
    }

    /**
     * Initializes Pits and Kalah for Player 1 and Player 2
     * using the respective Builder methods.
     * @return List of Entities (Pits/Kalah)
     */
    private List<Entity> initializePlayersPitAndKalah(){
        List<Entity> boardEntity = new Kalah.KalahBoardBuilder()
                               .ownerPlayer(Player.PLAYER1)
                               .initialStones(initialStones)
                               .build();
        boardEntity.addAll(new Kalah.KalahBoardBuilder()
                                    .ownerPlayer(Player.PLAYER2)
                                    .initialStones(initialStones)
                                    .build());
        return boardEntity;

    }
}
