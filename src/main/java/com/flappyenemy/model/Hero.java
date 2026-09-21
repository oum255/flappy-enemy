package com.flappyenemy.model;

import java.util.Random;

// Classe abstraite dédiée aux héros. Elle sert de classe mère aux différents types de héros.
// Chaque héros décrit lui-même son comportement (apparition, déplacement) et ses effets
// (ce qui arrive à l'ennemi à son contact, points gagnés quand on le tire) : le reste du jeu
// n'a donc pas besoin de connaître les types concrets (pas de "instanceof").
public abstract class Hero extends Entity {
    public static final int SIZE = 80;
    protected static final int BOTTOM_MARGIN = 20;   // Marge basse pour l'apparition verticale

    protected final Random random;                   // Source d'aléatoire (injectable pour les tests)

    protected Hero(Random random) {
        super(SIZE);
        this.random = random;
    }

    // Type du héros (sert notamment à la vue pour choisir l'image)
    public abstract HeroType getType();

    // Place le héros à sa position de départ dans un monde de la taille indiquée
    public abstract void spawn(int worldWidth, int worldHeight);

    // Déplace le héros pendant que le décor défile de "distance" pixels
    public abstract void move(double distance);

    // Applique à l'ennemi les conséquences d'un contact avec ce héros
    public abstract void applyContact(Enemy enemy);

    // Points gagnés par l'ennemi lorsqu'il élimine ce héros d'un tir
    public abstract int shotPoints();

    // Comportement propre à chaque héros à chaque image (par défaut : rien)
    public void update(double dt, int worldHeight) {
    }

    // Vrai quand le héros est sorti du jeu et peut être supprimé
    public boolean isOut(int worldHeight) {
        return x <= -size;
    }
}
