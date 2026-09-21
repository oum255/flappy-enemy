package com.flappyenemy.model;

// Classe de base de tout ce qui existe dans le monde du jeu : une position et une taille (carrée).
// Le modèle ne dépend pas de JavaFX : toute la logique peut être testée sans interface graphique.
public abstract class Entity {
    protected double x;         // Coordonnée X du coin haut-gauche
    protected double y;         // Coordonnée Y du coin haut-gauche
    protected final int size;   // Côté du carré occupé par l'entité (collisions et affichage)

    protected Entity(int size) {
        this.size = size;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    // Place l'entité à une position absolue
    public void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Déplace l'entité d'un décalage relatif
    public void translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    // Collision entre deux carrés : ils se chevauchent si leurs projections sur X et sur Y se chevauchent.
    // Deux carrés qui ne font que se toucher par un bord ne sont pas en collision.
    public boolean intersects(Entity other) {
        return x < other.x + other.size && x + size > other.x
                && y < other.y + other.size && y + size > other.y;
    }
}
