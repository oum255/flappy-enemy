package com.flappyenemy.model;

// Un projectile tiré par l'ennemi. Sa position est celle de son centre à la création.
public class Bullet extends Entity {
    public static final int SIZE = 12;   // Diamètre de la balle

    public Bullet(double centerX, double centerY) {
        super(SIZE);
        moveTo(centerX - SIZE / 2.0, centerY - SIZE / 2.0);
    }

    // Fait avancer la balle vers la droite
    public void advance(double dx) {
        translate(dx, 0);
    }
}
