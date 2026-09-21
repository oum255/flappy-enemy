package com.flappyenemy.view;

import com.flappyenemy.controller.GameController;
import com.flappyenemy.controller.GameState;
import com.flappyenemy.model.Enemy;
import com.flappyenemy.model.Game;
import com.flappyenemy.model.HeroType;
import com.flappyenemy.model.MeleeHero;
import com.flappyenemy.model.StealthHero;
import com.flappyenemy.model.TankHero;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

// La "vue" du modèle MVC : le canvas de jeu, la barre d'état (pause, vie, score, record) et les écrans
// superposés (menu, pause, game over). Elle affiche l'état fourni par le contrôleur et lui transmet les clics.
public class GameScreen extends BorderPane {
    public static final int BAR_HEIGHT = 40;
    private static final double GAUGE_WIDTH = 120;

    private final GameController controller;
    private final Renderer renderer;

    private final Button pauseButton = new Button("Pause");
    private final Region healthGauge = new Region();
    private final Label scoreValue = new Label("0");
    private final Label bestValue = new Label("0");

    private final VBox overlay = new VBox();
    private final Label title = new Label();
    private final Label subtitle = new Label();
    private final VBox content = new VBox(6);
    private final Button actionButton = new Button();

    private GameState displayedState;   // Dernier état pour lequel l'écran superposé a été construit

    public GameScreen(GameController controller) {
        this.controller = controller;

        Canvas canvas = new Canvas(Game.WIDTH, Game.HEIGHT);
        this.renderer = new Renderer(canvas.getGraphicsContext2D());

        buildOverlay();
        setCenter(new StackPane(canvas, overlay));
        setBottom(buildStatusBar());
        refresh();
    }

    // ------------------------------------------------------------------ construction

    private HBox buildStatusBar() {
        // Les boutons ne prennent pas le focus : sinon la barre d'espace (saut) les déclencherait
        pauseButton.setFocusTraversable(false);
        pauseButton.setMinWidth(90);
        pauseButton.getStyleClass().add("btn");
        pauseButton.setOnAction(e -> controller.togglePause());

        Region gaugeBackground = new Region();
        gaugeBackground.getStyleClass().add("gauge-background");
        gaugeBackground.setPrefSize(GAUGE_WIDTH, 12);
        gaugeBackground.setMaxSize(GAUGE_WIDTH, 12);
        healthGauge.getStyleClass().add("gauge-health");
        healthGauge.setMinWidth(0);
        healthGauge.setPrefHeight(12);
        healthGauge.setMaxHeight(12);
        StackPane gauge = new StackPane(gaugeBackground, healthGauge);
        gauge.setAlignment(Pos.CENTER_LEFT);
        gauge.setMaxWidth(GAUGE_WIDTH);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        scoreValue.getStyleClass().add("status-value");
        bestValue.getStyleClass().add("status-value");

        HBox bar = new HBox(10, pauseButton, new Label("Health"), gauge, spacer,
                new Label("Score"), scoreValue, new Label("Best"), bestValue);
        bar.getStyleClass().add("status-bar");
        bar.setPrefHeight(BAR_HEIGHT);
        bar.setMinHeight(BAR_HEIGHT);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private void buildOverlay() {
        title.getStyleClass().add("overlay-title");
        subtitle.getStyleClass().add("overlay-subtitle");
        content.setAlignment(Pos.CENTER);
        actionButton.setFocusTraversable(false);
        actionButton.getStyleClass().addAll("btn", "btn-action");
        actionButton.setOnAction(e -> {
            if (controller.getState() == GameState.PAUSED) {
                controller.togglePause();
            } else {
                controller.start();
            }
        });

        overlay.getStyleClass().add("overlay");
        overlay.setAlignment(Pos.CENTER);
        overlay.getChildren().addAll(title, subtitle, content, actionButton);
    }

    // ------------------------------------------------------------------ mise à jour à chaque image

    public void refresh() {
        renderer.draw(controller.getGame());

        Game game = controller.getGame();
        double healthRatio = game.getHealth() / (double) Enemy.MAX_HEALTH;
        healthGauge.setPrefWidth(GAUGE_WIDTH * healthRatio);
        healthGauge.setMaxWidth(GAUGE_WIDTH * healthRatio);
        scoreValue.setText(Integer.toString(game.getScore()));
        bestValue.setText(Integer.toString(controller.getHighScores().getBest()));

        GameState state = controller.getState();
        pauseButton.setText(state == GameState.PAUSED ? "Resume" : "Pause");
        pauseButton.setDisable(state == GameState.MENU || state == GameState.GAME_OVER);

        if (state != displayedState) {
            displayedState = state;
            buildOverlayContent(state);
        }
    }

    // Reconstruit l'écran superposé lorsque l'état du jeu change
    private void buildOverlayContent(GameState state) {
        content.getChildren().clear();
        overlay.setVisible(state != GameState.RUNNING);

        switch (state) {
            case MENU -> {
                title.setText("FLAPPY ENEMY");
                subtitle.setText("You are the ghost: dodge the heroes, collect coins and shoot them down!");
                content.getChildren().add(text("Space: fly     E: shoot     P: pause     M: sound"));
                content.getChildren().add(heroRow(HeroType.MELEE,
                        "Warrior: deadly on contact  ·  shot: +" + MeleeHero.SHOT_POINTS));
                content.getChildren().add(heroRow(HeroType.STEALTH,
                        "Ninja: contact -" + StealthHero.SCORE_PENALTY + " points  ·  shot: +" + StealthHero.SHOT_POINTS));
                content.getChildren().add(heroRow(HeroType.TANK,
                        "Knight: contact -" + TankHero.HEALTH_DAMAGE + " health  ·  shot: +" + TankHero.SHOT_POINTS));
                actionButton.setText("Play  (Enter)");
            }
            case PAUSED -> {
                title.setText("PAUSED");
                subtitle.setText("");
                actionButton.setText("Resume  (P)");
            }
            case GAME_OVER -> {
                Game game = controller.getGame();
                title.setText("GAME OVER");
                subtitle.setText("Score: " + game.getScore());
                if (controller.isNewRecord()) {
                    Label record = new Label("New record!");
                    record.getStyleClass().add("record-text");
                    content.getChildren().add(record);
                }
                content.getChildren().add(text("Coins: " + game.getCollectedCoins()
                        + "   ·   Heroes eliminated: " + game.getEliminatedHeroes()
                        + "   ·   Time: " + formatDuration(game.getElapsedTime())));
                content.getChildren().add(text("Best scores"));
                List<Integer> scores = controller.getHighScores().getScores();
                for (int i = 0; i < scores.size(); i++) {
                    content.getChildren().add(text((i + 1) + ".  " + scores.get(i)));
                }
                actionButton.setText("Play again  (Enter)");
            }
            case RUNNING -> {
            }
        }
    }

    private Label text(String contentText) {
        Label label = new Label(contentText);
        label.getStyleClass().add("info-text");
        return label;
    }

    // Une ligne de la légende : l'image du héros suivie de sa description
    private HBox heroRow(HeroType type, String description) {
        ImageView icon = new ImageView(renderer.heroImage(type));
        icon.setFitWidth(34);
        icon.setFitHeight(34);
        HBox row = new HBox(10, icon, text(description));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(420);
        return row;
    }

    private static String formatDuration(double seconds) {
        int total = (int) seconds;
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
