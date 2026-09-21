package com.flappyenemy.view;

import com.flappyenemy.model.Bullet;
import com.flappyenemy.model.Enemy;
import com.flappyenemy.model.Entity;
import com.flappyenemy.model.Game;
import com.flappyenemy.model.Hero;
import com.flappyenemy.model.HeroType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

// Dessine l'état d'une partie sur un canvas. Cette classe ne modifie jamais le modèle : elle ne fait que le lire.
public class Renderer {
    private static final double MAX_TILT = 20;          // Inclinaison maximale du fantôme, en degrés
    private static final double REFERENCE_SPEED = 300;  // Vitesse verticale (px/s) à laquelle l'inclinaison est maximale

    private final GraphicsContext gc;
    private final Image background = load("background.png");
    private final Image ghost = load("ghost.png");
    private final Image coin = load("coin.png");
    private final Map<HeroType, Image> heroImages = new EnumMap<>(HeroType.class);

    public Renderer(GraphicsContext gc) {
        this.gc = gc;
        heroImages.put(HeroType.MELEE, load("melee.png"));
        heroImages.put(HeroType.STEALTH, load("stealth.png"));
        heroImages.put(HeroType.TANK, load("tank.png"));
    }

    // Charge une image depuis les ressources, avec un message clair si elle est absente
    public static Image load(String name) {
        InputStream stream = Renderer.class.getResourceAsStream("/" + name);
        return new Image(Objects.requireNonNull(stream, "Resource not found: " + name));
    }

    public Image heroImage(HeroType type) {
        return heroImages.get(type);
    }

    public void draw(Game game) {
        drawBackground(game);
        for (Entity c : game.getCoins()) {
            gc.drawImage(coin, c.getX(), c.getY(), c.getSize(), c.getSize());
        }
        for (Hero h : game.getHeroes()) {
            gc.drawImage(heroImages.get(h.getType()), h.getX(), h.getY(), h.getSize(), h.getSize());
        }
        for (Bullet b : game.getBullets()) {
            drawBullet(b);
        }
        drawEnemy(game);
    }

    // Deux copies du fond se suivent : quand l'une sort à gauche, l'autre la remplace (défilement continu)
    private void drawBackground(Game game) {
        double offset = game.getBackgroundScroll() % Game.WIDTH;
        gc.drawImage(background, -offset, 0, Game.WIDTH, Game.HEIGHT);
        gc.drawImage(background, Game.WIDTH - offset, 0, Game.WIDTH, Game.HEIGHT);
    }

    private void drawBullet(Bullet b) {
        gc.setFill(Color.rgb(255, 120, 60, 0.35));
        gc.fillOval(b.getX() - 4, b.getY() - 4, b.getSize() + 8, b.getSize() + 8);
        gc.setFill(Color.rgb(255, 90, 40));
        gc.fillOval(b.getX(), b.getY(), b.getSize(), b.getSize());
        gc.setFill(Color.rgb(255, 235, 170));
        gc.fillOval(b.getX() + 3, b.getY() + 3, b.getSize() - 6, b.getSize() - 6);
    }

    // Le fantôme s'incline vers le haut quand il monte et vers le bas quand il tombe
    private void drawEnemy(Game game) {
        Enemy enemy = game.getEnemy();
        double angle = Math.max(-MAX_TILT, Math.min(MAX_TILT,
                game.getVerticalSpeed() / REFERENCE_SPEED * MAX_TILT));
        double centerX = enemy.getX() + enemy.getSize() / 2.0;
        double centerY = enemy.getY() + enemy.getSize() / 2.0;

        gc.save();
        if (game.isFinished()) {
            gc.setGlobalAlpha(0.5);
        }
        gc.translate(centerX, centerY);
        gc.rotate(angle);
        gc.drawImage(ghost, -enemy.getSize() / 2.0, -enemy.getSize() / 2.0, enemy.getSize(), enemy.getSize());
        gc.restore();
    }
}
