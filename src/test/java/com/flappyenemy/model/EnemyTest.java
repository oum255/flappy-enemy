package com.flappyenemy.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnemyTest {

    @Test
    void aNewEnemyIsInFullHealthWithAZeroScore() {
        Enemy enemy = new Enemy();
        assertEquals(0, enemy.getScore());
        assertEquals(Enemy.MAX_HEALTH, enemy.getHealth());
        assertFalse(enemy.isDead());
    }

    @Test
    void scoreCannotBeNegative() {
        Enemy enemy = new Enemy();
        enemy.setScore(5);
        enemy.setScore(enemy.getScore() - 10);
        assertEquals(0, enemy.getScore());
    }

    @Test
    void incrementScoreAddsOnePoint() {
        Enemy enemy = new Enemy();
        enemy.incrementScore();
        enemy.incrementScore();
        assertEquals(2, enemy.getScore());
    }

    @Test
    void healthStaysBetweenZeroAndTheMaximum() {
        Enemy enemy = new Enemy();
        enemy.setHealth(-30);
        assertEquals(0, enemy.getHealth());
        enemy.setHealth(500);
        assertEquals(Enemy.MAX_HEALTH, enemy.getHealth());
    }

    @Test
    void theEnemyIsDeadWhenItsHealthIsZero() {
        Enemy enemy = new Enemy();
        enemy.setHealth(0);
        assertTrue(enemy.isDead());
    }

    @Test
    void moveOnlyChangesTheVerticalPosition() {
        Enemy enemy = new Enemy();
        enemy.moveTo(300, 200);
        enemy.move(2.5);
        assertEquals(300, enemy.getX());
        assertEquals(202.5, enemy.getY());
    }
}
