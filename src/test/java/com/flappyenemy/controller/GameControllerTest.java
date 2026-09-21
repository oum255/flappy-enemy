package com.flappyenemy.controller;

import com.flappyenemy.model.Game;
import com.flappyenemy.model.GameEvent;
import com.flappyenemy.model.HighScores;
import com.flappyenemy.model.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControllerTest {

    @TempDir
    Path directory;

    private HighScores highScores;

    @BeforeEach
    void prepareHighScores() {
        highScores = new HighScores(directory.resolve("scores.txt"), 5);
    }

    // Jeu classique, sans héros (la partie ne se termine donc pas toute seule)
    private GameController quietController() {
        Settings withoutHeroes = new Settings(120, 1.5, 240, 500, 900, 10, -300, 300, 300, 0.25, 1e9, 1e9, 0, 0.8);
        return new GameController(highScores, () -> new Game(withoutHeroes, new Random(1)));
    }

    // Jeu dont chaque partie est perdue dès la première image, avec le score voulu
    private GameController losingController(int[] targetScore) {
        return new GameController(highScores, () -> {
            Game game = new Game(Settings.defaults(), new Random(1));
            game.getEnemy().setScore(targetScore[0]);
            game.getEnemy().setHealth(0);
            return game;
        });
    }

    @Test
    void theGameStartsOnTheMenuAndDoesNotAdvanceUntilStarted() {
        GameController controller = quietController();
        assertEquals(GameState.MENU, controller.getState());
        controller.tick(0.1);
        assertEquals(0, controller.getGame().getElapsedTime());
    }

    @Test
    void startBeginsTheGame() {
        GameController controller = quietController();
        controller.start();
        assertEquals(GameState.RUNNING, controller.getState());
        controller.tick(0.02);
        assertTrue(controller.getGame().getElapsedTime() > 0);
    }

    @Test
    void jumpAndShootAreIgnoredWhenNotRunning() {
        GameController controller = quietController();
        controller.jump();
        controller.shoot();
        assertTrue(controller.pollEvents().isEmpty());
        assertTrue(controller.getGame().getBullets().isEmpty());
    }

    @Test
    void jumpAndShootWhileRunningProduceEvents() {
        GameController controller = quietController();
        controller.start();
        controller.jump();
        controller.shoot();
        assertEquals(List.of(GameEvent.JUMP, GameEvent.SHOOT), controller.pollEvents());
        assertEquals(1, controller.getGame().getBullets().size());
    }

    @Test
    void pauseSuspendsTheGameThenResumesIt() {
        GameController controller = quietController();
        controller.start();
        controller.tick(0.02);
        double time = controller.getGame().getElapsedTime();

        controller.togglePause();
        assertEquals(GameState.PAUSED, controller.getState());
        controller.tick(0.5);
        controller.jump();
        assertEquals(time, controller.getGame().getElapsedTime());
        assertTrue(controller.pollEvents().isEmpty());

        controller.togglePause();
        assertEquals(GameState.RUNNING, controller.getState());
        controller.tick(0.02);
        assertTrue(controller.getGame().getElapsedTime() > time);
    }

    @Test
    void pauseIsIgnoredOnTheMenu() {
        GameController controller = quietController();
        controller.togglePause();
        assertEquals(GameState.MENU, controller.getState());
    }

    @Test
    void aTooLongTimeStepIsCapped() {
        GameController controller = quietController();
        controller.start();
        controller.tick(10);
        assertEquals(GameController.MAX_STEP, controller.getGame().getElapsedTime(), 1e-9);
    }

    @Test
    void startIsIgnoredWhileTheGameIsRunning() {
        GameController controller = quietController();
        controller.start();
        Game game = controller.getGame();
        controller.start();
        assertSame(game, controller.getGame());
    }

    @Test
    void theEndOfAGameSavesTheScoreAndReportsTheRecord() {
        int[] targetScore = {42};
        GameController controller = losingController(targetScore);
        controller.start();
        controller.tick(0.02);

        assertEquals(GameState.GAME_OVER, controller.getState());
        assertTrue(controller.isNewRecord());
        assertTrue(controller.pollEvents().contains(GameEvent.GAME_OVER));
        assertEquals(controller.getGame().getScore(), highScores.getBest());
        assertTrue(highScores.getBest() >= 42);
    }

    @Test
    void aWorseScoreIsNotARecord() {
        int[] targetScore = {42};
        GameController controller = losingController(targetScore);
        controller.start();
        controller.tick(0.02);
        int record = highScores.getBest();

        targetScore[0] = 5;
        controller.start();          // rejouer
        controller.tick(0.02);

        assertEquals(GameState.GAME_OVER, controller.getState());
        assertFalse(controller.isNewRecord());
        assertEquals(record, highScores.getBest());
        assertEquals(2, highScores.getScores().size());
    }

    @Test
    void playingAgainCreatesANewGame() {
        int[] targetScore = {10};
        GameController controller = losingController(targetScore);
        controller.start();
        controller.tick(0.02);
        Game previous = controller.getGame();

        controller.start();

        assertEquals(GameState.RUNNING, controller.getState());
        assertNotSame(previous, controller.getGame());
        assertFalse(controller.isNewRecord());
    }

    @Test
    void afterGameOverActionsAreIgnored() {
        int[] targetScore = {10};
        GameController controller = losingController(targetScore);
        controller.start();
        controller.tick(0.02);
        controller.pollEvents();

        controller.jump();
        controller.shoot();
        controller.togglePause();

        assertEquals(GameState.GAME_OVER, controller.getState());
        assertTrue(controller.pollEvents().isEmpty());
    }
}
