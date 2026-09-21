package com.flappyenemy.model;

import java.util.Random;

// Héros de type furtif : il tombe du haut de l'écran en zigzaguant (mouvement sinusoïdal).
// Son contact ne blesse pas l'ennemi mais lui fait perdre des points.
public class StealthHero extends Hero {
    public static final int SHOT_POINTS = 8;
    public static final int SCORE_PENALTY = 10;
    private static final double AMPLITUDE = 50;   // Amplitude du zigzag, en pixels

    private double startX;                         // Position X autour de laquelle il zigzague

    public StealthHero(Random random) {
        super(random);
    }

    public StealthHero() {
        this(new Random());
    }

    @Override
    public HeroType getType() {
        return HeroType.STEALTH;
    }

    // Apparaît en haut de l'écran, à une abscisse aléatoire
    @Override
    public void spawn(int worldWidth, int worldHeight) {
        startX = random.nextInt(worldWidth);
        moveTo(startX, 0);
    }

    // Descend de "distance" pixels et oscille horizontalement selon sa hauteur
    @Override
    public void move(double distance) {
        double newY = y + distance;
        moveTo(startX + AMPLITUDE * Math.sin(Math.toRadians(newY)), newY);
    }

    @Override
    public void applyContact(Enemy enemy) {
        enemy.setScore(enemy.getScore() - SCORE_PENALTY);
    }

    @Override
    public int shotPoints() {
        return SHOT_POINTS;
    }

    // Le furtif disparaît aussi lorsqu'il atteint le sol
    @Override
    public boolean isOut(int worldHeight) {
        return super.isOut(worldHeight) || y >= worldHeight - size;
    }
}
