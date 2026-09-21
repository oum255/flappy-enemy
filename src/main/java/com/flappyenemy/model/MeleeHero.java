package com.flappyenemy.model;

import java.util.Random;

// Héros de type corps-à-corps : il avance en ligne droite vers l'ennemi et le tue au contact.
public class MeleeHero extends Hero {
    public static final int SHOT_POINTS = 5;

    public MeleeHero(Random random) {
        super(random);
    }

    public MeleeHero() {
        this(new Random());
    }

    @Override
    public HeroType getType() {
        return HeroType.MELEE;
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

    // Contact mortel
    @Override
    public void applyContact(Enemy enemy) {
        enemy.setHealth(0);
    }

    @Override
    public int shotPoints() {
        return SHOT_POINTS;
    }
}
