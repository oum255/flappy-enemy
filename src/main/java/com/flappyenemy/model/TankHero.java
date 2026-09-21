package com.flappyenemy.model;

import java.util.Random;

// Héros de type tank : il avance vers l'ennemi en se téléportant régulièrement de quelques pixels.
// Son contact retire de la santé à l'ennemi.
public class TankHero extends Hero {
    public static final int SHOT_POINTS = 7;
    public static final int HEALTH_DAMAGE = 50;
    private static final double TELEPORT_DELAY = 0.5;      // Secondes entre deux téléportations
    private static final int TELEPORT_AMPLITUDE = 30;      // Décalage maximal (en pixels) sur chaque axe

    private double timer;                                  // Temps écoulé depuis la dernière téléportation

    public TankHero(Random random) {
        super(random);
    }

    public TankHero() {
        this(new Random());
    }

    @Override
    public HeroType getType() {
        return HeroType.TANK;
    }

    // Apparaît à droite de l'écran, à une hauteur aléatoire
    @Override
    public void spawn(int worldWidth, int worldHeight) {
        moveTo(worldWidth, random.nextInt(worldHeight - size - BOTTOM_MARGIN));
    }

    @Override
    public void move(double distance) {
        translate(-distance, 0);
    }

    @Override
    public void update(double dt, int worldHeight) {
        timer += dt;
        while (timer >= TELEPORT_DELAY) {
            timer -= TELEPORT_DELAY;
            teleport(worldHeight);
        }
    }

    // Décale le héros d'une valeur aléatoire sur chaque axe, sans jamais sortir du monde par le haut ou le bas
    public void teleport(int worldHeight) {
        double dx = random.nextInt(2 * TELEPORT_AMPLITUDE) - TELEPORT_AMPLITUDE;
        double dy = random.nextInt(2 * TELEPORT_AMPLITUDE) - TELEPORT_AMPLITUDE;
        double newY = Math.max(0, Math.min(worldHeight - size, y + dy));
        moveTo(x + dx, newY);
    }

    @Override
    public void applyContact(Enemy enemy) {
        enemy.setHealth(enemy.getHealth() - HEALTH_DAMAGE);
    }

    @Override
    public int shotPoints() {
        return SHOT_POINTS;
    }
}
