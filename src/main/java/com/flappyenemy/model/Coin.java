package com.flappyenemy.model;

// Une pièce de monnaie à ramasser.
public class Coin extends Entity {
    public static final int SIZE = 40;

    public Coin(double x, double y) {
        super(SIZE);
        moveTo(x, y);
    }
}
