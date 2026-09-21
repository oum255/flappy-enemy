package com.flappyenemy.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeroesTest {
    private static final int WIDTH = Game.WIDTH;
    private static final int HEIGHT = Game.HEIGHT;

    // Générateur qui renvoie toujours la même valeur : rend les positions aléatoires prévisibles
    private static Random fixedRandom(int value) {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return value;
            }
        };
    }

    // ------------------------------------------------------------------ MeleeHero

    @Test
    void theMeleeHeroKillsOnContact() {
        Enemy enemy = new Enemy();
        new MeleeHero().applyContact(enemy);
        assertTrue(enemy.isDead());
    }

    @Test
    void theMeleeHeroSpawnsOnTheRightAtAValidHeight() {
        Random random = new Random(5);
        for (int i = 0; i < 200; i++) {
            MeleeHero hero = new MeleeHero(random);
            hero.spawn(WIDTH, HEIGHT);
            assertEquals(WIDTH, hero.getX());
            assertTrue(hero.getY() >= 0 && hero.getY() + hero.getSize() < HEIGHT);
        }
    }

    @Test
    void theMeleeHeroMovesLeft() {
        MeleeHero hero = new MeleeHero(fixedRandom(0));
        hero.spawn(WIDTH, HEIGHT);
        hero.move(15);
        assertEquals(WIDTH - 15, hero.getX());
        assertEquals(0, hero.getY());
    }

    @Test
    void aHeroLeavesTheGameOnceFullyOffScreenOnTheLeft() {
        MeleeHero hero = new MeleeHero();
        hero.moveTo(-Hero.SIZE + 0.1, 50);
        assertFalse(hero.isOut(HEIGHT));
        hero.moveTo(-Hero.SIZE, 50);
        assertTrue(hero.isOut(HEIGHT));
    }

    // ------------------------------------------------------------------ StealthHero

    @Test
    void theStealthHeroSpawnsAtTheTopAtTheDrawnAbscissa() {
        StealthHero hero = new StealthHero(fixedRandom(100));
        hero.spawn(WIDTH, HEIGHT);
        assertEquals(100, hero.getX());
        assertEquals(0, hero.getY());
    }

    @Test
    void theStealthHeroFallsWhileZigzagging() {
        StealthHero hero = new StealthHero(fixedRandom(100));
        hero.spawn(WIDTH, HEIGHT);
        hero.move(90);   // y = 90 : sin(90°) = 1, donc décalage maximal vers la droite
        assertEquals(90, hero.getY(), 1e-9);
        assertEquals(150, hero.getX(), 1e-9);
    }

    @Test
    void theStealthHeroZigzagStaysWithinItsAmplitude() {
        StealthHero hero = new StealthHero(fixedRandom(300));
        hero.spawn(WIDTH, HEIGHT);
        for (int i = 0; i < 300; i++) {
            hero.move(1);
            assertTrue(Math.abs(hero.getX() - 300) <= 50 + 1e-9);
        }
    }

    @Test
    void theStealthHeroCostsPointsWithoutHurtingTheEnemy() {
        Enemy enemy = new Enemy();
        enemy.setScore(30);
        new StealthHero().applyContact(enemy);
        assertEquals(30 - StealthHero.SCORE_PENALTY, enemy.getScore());
        assertEquals(Enemy.MAX_HEALTH, enemy.getHealth());
    }

    @Test
    void theStealthHeroPenaltyNeverMakesTheScoreNegative() {
        Enemy enemy = new Enemy();
        enemy.setScore(3);
        new StealthHero().applyContact(enemy);
        assertEquals(0, enemy.getScore());
    }

    @Test
    void theStealthHeroDisappearsWhenItHitsTheGround() {
        StealthHero hero = new StealthHero();
        hero.moveTo(100, HEIGHT - Hero.SIZE - 1);
        assertFalse(hero.isOut(HEIGHT));
        hero.moveTo(100, HEIGHT - Hero.SIZE);
        assertTrue(hero.isOut(HEIGHT));
    }

    // ------------------------------------------------------------------ TankHero

    @Test
    void theTankHeroRemovesHealth() {
        Enemy enemy = new Enemy();
        new TankHero().applyContact(enemy);
        assertEquals(Enemy.MAX_HEALTH - TankHero.HEALTH_DAMAGE, enemy.getHealth());
        assertFalse(enemy.isDead());
    }

    @Test
    void twoContactsWithTanksKillTheEnemy() {
        Enemy enemy = new Enemy();
        new TankHero().applyContact(enemy);
        new TankHero().applyContact(enemy);
        assertTrue(enemy.isDead());
    }

    @Test
    void theTankTeleportationStaysInsideTheWorldAndItsAmplitude() {
        TankHero tank = new TankHero(new Random(11));
        tank.spawn(WIDTH, HEIGHT);
        for (int i = 0; i < 500; i++) {
            double xBefore = tank.getX();
            tank.teleport(HEIGHT);
            assertTrue(Math.abs(tank.getX() - xBefore) <= 30);
            assertTrue(tank.getY() >= 0 && tank.getY() <= HEIGHT - tank.getSize());
        }
    }

    @Test
    void theTankNeverLeavesThroughTheBottomWhenTeleporting() {
        TankHero tank = new TankHero(fixedRandom(59));   // décalage vertical de +29
        tank.moveTo(300, HEIGHT - Hero.SIZE - 5);
        tank.teleport(HEIGHT);
        assertEquals(HEIGHT - Hero.SIZE, tank.getY());
    }

    @Test
    void theTankTeleportsEveryHalfSecond() {
        TankHero tank = new TankHero(fixedRandom(50));   // décalage de +20 sur chaque axe
        tank.spawn(WIDTH, HEIGHT);                       // y = 50 (valeur fixe)
        double x = tank.getX();
        double y = tank.getY();

        tank.update(0.4, HEIGHT);
        assertEquals(x, tank.getX());
        assertEquals(y, tank.getY());

        tank.update(0.2, HEIGHT);                        // 0,6 s écoulées : une téléportation
        assertEquals(x + 20, tank.getX());
        assertEquals(y + 20, tank.getY());
    }

    // ------------------------------------------------------------------ fabrique et points

    @Test
    void eachTypeCreatesAHeroOfTheRightType() {
        for (HeroType type : HeroType.values()) {
            assertEquals(type, type.create(new Random(1)).getType());
        }
    }

    @Test
    void theRandomDrawProducesAllTypes() {
        Random random = new Random(3);
        Set<HeroType> seen = EnumSet.noneOf(HeroType.class);
        for (int i = 0; i < 100; i++) {
            seen.add(HeroType.createRandom(random).getType());
        }
        assertEquals(EnumSet.allOf(HeroType.class), seen);
    }

    @Test
    void shootingAHeroGivesItsPoints() {
        assertEquals(MeleeHero.SHOT_POINTS, new MeleeHero().shotPoints());
        assertEquals(StealthHero.SHOT_POINTS, new StealthHero().shotPoints());
        assertEquals(TankHero.SHOT_POINTS, new TankHero().shotPoints());
    }
}
