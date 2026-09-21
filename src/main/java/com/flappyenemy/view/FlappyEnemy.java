package com.flappyenemy.view;

import com.flappyenemy.controller.GameController;
import com.flappyenemy.controller.GameState;
import com.flappyenemy.model.Game;
import com.flappyenemy.model.HighScores;
import com.flappyenemy.model.Settings;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

// Point d'entrée du jeu. Cette classe crée la fenêtre, relie le clavier au contrôleur (GameController)
// et cadence la boucle de jeu ; l'affichage est confié à GameScreen.
public class FlappyEnemy extends Application {
    private GameController controller;
    private Sounds sounds;

    @Override
    public void start(Stage stage) {
        controller = new GameController(Settings.load(), HighScores.atDefaultLocation());
        sounds = new Sounds();
        GameScreen screen = new GameScreen(controller);

        Scene scene = new Scene(screen, Game.WIDTH, Game.HEIGHT + GameScreen.BAR_HEIGHT);
        scene.getStylesheets().add(FlappyEnemy.class.getResource("/style.css").toExternalForm());
        scene.setOnKeyPressed(this::handleKey);

        stage.setTitle("Flappy Enemy");
        stage.getIcons().add(Renderer.load("icon.png"));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        // La boucle tourne en permanence : le temps écoulé entre deux images est transmis au contrôleur,
        // qui l'ignore tant que la partie n'est pas en cours (donc pas de saut de temps après une pause).
        new AnimationTimer() {
            private long last = 0;

            @Override
            public void handle(long now) {
                double dt = (last == 0) ? 0 : (now - last) * 1e-9;
                last = now;

                controller.tick(dt);
                controller.pollEvents().forEach(sounds::play);
                screen.refresh();
            }
        }.start();
    }

    // Traduit les touches du clavier en actions du joueur
    private void handleKey(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE -> {
                if (controller.getState() == GameState.MENU) {
                    controller.start();
                } else {
                    controller.jump();
                }
            }
            case ENTER -> controller.start();
            case E -> controller.shoot();
            case P, ESCAPE -> controller.togglePause();
            case M -> sounds.toggleMute();
            default -> {
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
