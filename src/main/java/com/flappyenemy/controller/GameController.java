package com.flappyenemy.controller;

import com.flappyenemy.model.Game;
import com.flappyenemy.model.GameEvent;
import com.flappyenemy.model.HighScores;
import com.flappyenemy.model.Settings;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

// Cette classe joue le role du controleur dans le modèle MVC. Elle gère les états du jeu
// (menu, partie en cours, pause, game over), transmet les actions du joueur à la partie et enregistre
// les meilleurs scores. Elle ne dépend pas de JavaFX : la vue lui envoie les actions du joueur
// et le temps écoulé, puis lit l'état pour l'afficher.
public class GameController {
    // Un pas de temps trop long (fenêtre déplacée, machine ralentie...) ferait "téléporter" les objets :
    // on le plafonne.
    static final double MAX_STEP = 0.05;

    private final HighScores highScores;
    private final Supplier<Game> gameFactory;

    private Game game;
    private GameState state = GameState.MENU;
    private boolean newRecord;

    public GameController(Settings settings, HighScores highScores) {
        this(highScores, () -> new Game(settings, new Random()));
    }

    // La fabrique de parties permet aux tests de fournir une partie déterministe
    public GameController(HighScores highScores, Supplier<Game> gameFactory) {
        this.highScores = highScores;
        this.gameFactory = gameFactory;
        this.game = gameFactory.get();   // Une partie "vitrine" est affichée derrière le menu
    }

    // ------------------------------------------------------------------ actions du joueur

    // Lance une nouvelle partie depuis le menu ou après un game over (ignoré dans les autres états)
    public void start() {
        if (state == GameState.MENU || state == GameState.GAME_OVER) {
            game = gameFactory.get();
            newRecord = false;
            state = GameState.RUNNING;
        }
    }

    public void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
        }
    }

    public void jump() {
        if (state == GameState.RUNNING) {
            game.jump();
        }
    }

    public void shoot() {
        if (state == GameState.RUNNING) {
            game.shoot();
        }
    }

    // ------------------------------------------------------------------ boucle de jeu

    // Fait avancer le jeu de "dt" secondes (sans effet hors partie en cours)
    public void tick(double dt) {
        if (state != GameState.RUNNING) return;

        game.update(Math.min(dt, MAX_STEP));

        if (game.isFinished()) {
            state = GameState.GAME_OVER;
            newRecord = highScores.add(game.getScore());
        }
    }

    // Événements survenus depuis le dernier appel (utilisés par la vue pour les sons)
    public List<GameEvent> pollEvents() {
        return game.pollEvents();
    }

    // ------------------------------------------------------------------ accès en lecture

    public GameState getState() {
        return state;
    }

    public Game getGame() {
        return game;
    }

    public HighScores getHighScores() {
        return highScores;
    }

    // Vrai si la dernière partie terminée a battu le record
    public boolean isNewRecord() {
        return newRecord;
    }
}
