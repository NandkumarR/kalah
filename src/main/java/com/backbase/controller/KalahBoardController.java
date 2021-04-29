package com.backbase.controller;

import com.backbase.model.Game;
import com.backbase.service.KalahBoard;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author nandk on 06/03/2021.
 * Controller class that intercepts all "/games" requests for multiple Http Resquest type
 */
@RestController
@RequestMapping(value = "/games")
public class KalahBoardController {

    @Resource
    KalahBoard kalahBoard;

    /**
     * POST call made while starting a new game with Player.PLAYER1 and Player.PLAYER2
     * @return Game object that contains board parameters and initial stone sets.
     */
    @PostMapping
    public Game startNewBoardGame(){
        return kalahBoard.initializeNewBoardGame();
    }

    /**
     * PUT call made while making a move on the Kalah Board. Move can be made by either player
     * @param gameId - gameId initilised .
     * @param pitId - pit picked for the move.
     * @return Game object that contains board parameters post the move was completed.
     */
    @PutMapping(value = "/{gameId}/pits/{pitId}")
    public Game makeMyMove(@PathVariable("gameId") String gameId, @PathVariable("pitId") Integer pitId){
        return kalahBoard.makeMyMove(gameId, pitId);
    }

    /**
     * GET call to fetch the current board stats. ( Current pits/house scores )
     * @param gameId - gameId for which the current board values are being fetched.
     * @return Game object that contains the required params.
     */
    @GetMapping(value = "/{gameId}")
    public Game getKalahBoard(@PathVariable("gameId") String gameId){
        return kalahBoard.fetchKalahBoardForGameId(gameId);
    }

    /**
     * DELETE call to remove a game.
     * @param gameId - gameId to be removed.
     */
    @DeleteMapping(value = "/{gameId}")
    public void cleanUpKalahBoard(@PathVariable("gameId") String gameId){
        kalahBoard.cleanUpKalahBoard(gameId);
    }

}
