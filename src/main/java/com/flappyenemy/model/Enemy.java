package com.flappyenemy.model;

// Le personnage contrôlé par le joueur (le fantôme). Cette classe gère son score, sa santé et sa position.
public class Enemy extends Entity {
    public static final int SIZE = 80;         // Taille standard de l'ennemi
    public static final int MAX_HEALTH = 100;  // Santé initiale

    private int score;
    private int health;

    public Enemy() {
        super(SIZE);
        this.score = 0;
        this.health = MAX_HEALTH;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, score);   // Le score ne peut pas être négatif
    }

    public void incrementScore() {
        score++;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(MAX_HEALTH, health));   // Santé comprise entre 0 et MAX_HEALTH
    }

    public boolean isDead() {
        return health == 0;
    }

    // Déplace l'ennemi verticalement
    public void move(double dy) {
        translate(0, dy);
    }
}
