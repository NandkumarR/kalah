package com.backbase.service;

import com.backbase.GameOnApplication;
import com.backbase.model.Game;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import java.util.HashMap;

/**
 * @author nandk on 09/03/2021.
 * Integration tests that test all validations and a board game play. Integration runs with a random port assigned and NoSQL MongoDB.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = GameOnApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,properties = { "application.kalah.initialize.stones=6" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KalahBoardTest {

    private static final String START_GAME_ENDPOINT="/games";

    private String DELETE_GAME_ENDPOINT="/games/{{gameId}}";

    private String MAKE_MOVE_ENDPOINT="/games/{{gameId}}/pits/{{pitId}}";

    @LocalServerPort
    private Integer randomPort;

    //A playing schema, that defines the pit movement for a game with initial stone value 2
    private static final String playingSchema="{\n" +
            "\"1\":\"5\",\n" +
            "\"2\":\"4\",\n" +
            "\"3\":\"12\",\n" +
            "\"4\":\"11\",\n" +
            "\"5\":\"2\",\n" +
            "\"6\":\"9\",\n" +
            "\"7\":\"1\",\n" +
            "\"8\":\"10\",\n" +
            "\"9\":\"2\",\n" +
            "\"10\":\"8\",\n" +
            "\"11\":\"6\",\n" +
            "\"12\":\"10\",\n" +
            "\"13\":\"5\"\n" +
            "}";

    private static final String playingSchema1="{\n" +
            "\"1\":\"1\",\n" +
            "\"2\":\"2\",\n" +
            "\"3\":\"8\",\n" +
            "\"4\":\"1\",\n" +
            "\"5\":\"9\",\n" +
            "\"6\":\"3\",\n" +
            "\"7\":\"12\",\n" +
            "\"8\":\"13\",\n" +
            "\"9\":\"4\",\n" +
            "\"10\":\"13\",\n" +
            "\"11\":\"12\",\n" +
            "\"12\":\"3\",\n" +
            "\"13\":\"9\",\n" +
            "\"14\":\"5\",\n" +
            "\"15\":\"13\",\n" +
            "\"16\":\"12\",\n" +
            "\"17\":\"6\",\n" +
            "\"18\":\"13\",\n" +
            "\"19\":\"8\",\n" +
            "\"20\":\"4\",\n" +
            "\"21\":\"10\",\n" +
            "\"22\":\"11\",\n" +
            "\"23\":\"12\",\n" +
            "\"24\":\"3\",\n" +
            "\"25\":\"13\"\n" +
            "}";
    private static final String WINNER_IS_1="PLAYER1";
    // Winner attached to above playing schema.
    private static final String WINNER_IS="PLAYER2";

    private String gameIdForIntegrationPlay;

    TestRestTemplate testRestTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    HashMap<String,String> schemaMap;

    /**
     * Set up the integration tests.
     * @throws JsonProcessingException
     */
    @BeforeAll
    public void initialsetUpBeforeIntegrationPlay() throws JsonProcessingException{
        gameIdForIntegrationPlay=createNewGameForIntegrationTest();
        schemaMap= new ObjectMapper().readValue(playingSchema1, HashMap.class);
    }

    /**
     * Test the Kalah board initialization is complete with proper gameId generated.
     */
    @Test
    @Order(1)
    public void setUpKalahBoard(){
        Assert.notNull(gameIdForIntegrationPlay,"GameId cannot be null");
    }

    /**
     * Below test case validates if an invalid gameId is chosen to play .The application throws an exception of HttpCode 400.
     */
    @Test
    @Order(2)
    public void makeAMoveWithInvalidGameId(){
        String makeMoveInvalid=MAKE_MOVE_ENDPOINT.replace("{{gameId}}","invalid").replace("{{pitId}}","1");
        ResponseEntity<Game> responseEntity =  testRestTemplate.exchange(createURLWithPort(makeMoveInvalid), HttpMethod.PUT, entity,Game.class);
        Assert.isTrue(responseEntity.getStatusCode()== HttpStatus.BAD_REQUEST,"Game Id Validations Failed");
    }

    /**
     * Below test case validates if an invalid pitid (not owned by current player) is chosen to play. The application throws an exception of HttpCode 400.
     */
    @Test
    @Order(3)
    public void invalidMoveFromOppositePit(){
        String makeInValidMove=MAKE_MOVE_ENDPOINT.replace("{{gameId}}",gameIdForIntegrationPlay).replace("{{pitId}}","10");// Pit 10 doesnt belong to Player 1.
        ResponseEntity<Game> invalidResponseEntity =  testRestTemplate.exchange(createURLWithPort(makeInValidMove), HttpMethod.PUT, entity,Game.class);
        Assert.isTrue(invalidResponseEntity.getStatusCode()== HttpStatus.BAD_REQUEST,"Pit Owner Validations Failed");
    }

    /**
     * Below method runs a complete board game as per playingSchema with Player2 emerging as a winner. The initial stone size is 2 to keep the moves minimal.
     * Every move has to result in a HttpStatus 200 OK.
     */
    @Test
    @Order(4)
    public void completeBoardGame_initialStonesIsTwo(){
        ResponseEntity<Game> responseEntity=null;
        for (int i=1;i<=schemaMap.size();i++){
            String makeInValidMove=MAKE_MOVE_ENDPOINT.replace("{{gameId}}",gameIdForIntegrationPlay).replace("{{pitId}}",schemaMap.get(String.valueOf(i)));
            responseEntity =  testRestTemplate.exchange(createURLWithPort(makeInValidMove), HttpMethod.PUT, entity,Game.class);
            Assert.isTrue(responseEntity.getStatusCode()==HttpStatus.OK,"Invalid status code");
        }
        Assert.isTrue(responseEntity.getBody().getWinnerPlayer().toString().equals(WINNER_IS_1),"Game gone wrong");
    }

    /**
     * Remove the game created for integration tests
     */
    @AfterAll
    public void cleanUpPostIntegrationPlay(){
        removeGameIdPostIntegrationTest(gameIdForIntegrationPlay);
    }

    /**
     * Create the end point url based on random port assigned by SpringBootTest.
     * @param uri
     * @return String endpoint to be called.
     */
    private String createURLWithPort(String uri) {
        return "http://localhost:" + randomPort + uri;
    }

    /**
     * Make Rest call to remove the gameId created for integrations tests.
     * @param gameId
     */
    private void removeGameIdPostIntegrationTest(String gameId){
        DELETE_GAME_ENDPOINT=DELETE_GAME_ENDPOINT.replace("{{gameId}}",gameId);
        testRestTemplate.exchange(createURLWithPort(DELETE_GAME_ENDPOINT), HttpMethod.DELETE, entity,String.class);
    }

    /**
     * Create a new game to run the integration tests on the application.
     * @return String of gameId.
     */
    private String createNewGameForIntegrationTest(){
        ResponseEntity<HashMap> responseEntity = testRestTemplate.exchange(createURLWithPort(START_GAME_ENDPOINT), HttpMethod.POST, entity,HashMap.class);
        return (String) responseEntity.getBody().get("id");
    }

}
