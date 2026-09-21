package com.flappyenemy.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityTest {

    @Test
    void overlappingSquaresCollide() {
        Coin a = new Coin(0, 0);
        Coin b = new Coin(20, 20);
        assertTrue(a.intersects(b));
        assertTrue(b.intersects(a));   // la collision est symétrique
    }

    @Test
    void squaresTouchingByAnEdgeDoNotCollide() {
        Coin a = new Coin(0, 0);
        Coin toTheRight = new Coin(Coin.SIZE, 0);
        Coin below = new Coin(0, Coin.SIZE);
        assertFalse(a.intersects(toTheRight));
        assertFalse(a.intersects(below));
    }

    @Test
    void distantSquaresDoNotCollide() {
        assertFalse(new Coin(0, 0).intersects(new Coin(300, 300)));
    }

    @Test
    void aSquareFullyInsideAnotherCollides() {
        MeleeHero big = new MeleeHero();
        big.moveTo(100, 100);
        Coin small = new Coin(120, 120);
        assertTrue(big.intersects(small));
        assertTrue(small.intersects(big));
    }

    // Régression : l'ancien test ne regardait que les coins. Ici aucun coin d'un carré n'est dans l'autre,
    // et pourtant les deux carrés se chevauchent (forme de croix).
    @Test
    void crossShapedCollisionIsDetected() {
        MeleeHero big = new MeleeHero();
        big.moveTo(0, 0);
        Coin small = new Coin(20, -30);
        assertTrue(big.intersects(small));
        assertTrue(small.intersects(big));
    }

    @Test
    void translateAndMoveToChangeThePosition() {
        Coin coin = new Coin(10, 20);
        coin.translate(5, -8);
        assertEquals(15, coin.getX());
        assertEquals(12, coin.getY());
        coin.moveTo(1, 2);
        assertEquals(1, coin.getX());
        assertEquals(2, coin.getY());
    }

    @Test
    void aBulletIsCenteredOnItsStartPoint() {
        Bullet bullet = new Bullet(100, 50);
        assertEquals(100, bullet.getX() + Bullet.SIZE / 2.0);
        assertEquals(50, bullet.getY() + Bullet.SIZE / 2.0);
        bullet.advance(15);
        assertEquals(115, bullet.getX() + Bullet.SIZE / 2.0);
    }
}
