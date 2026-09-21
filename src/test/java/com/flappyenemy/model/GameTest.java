package com.flappyenemy.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    // Paramètres par défaut mais sans aucun héros : permet de simuler longtemps sans que l'ennemi meure
    private static Settings withoutHeroes() {
        return new Settings(120, 1.5, 240, 500, 900, 10, -300, 300, 300, 0.25, 1e9, 1e9, 0, 0.8);
    }

    // Une partie dont le monde a été vidé (pas de pièces initiales) : on y place exactement ce qu'on teste
    private static Game emptyGame(Settings settings) {
        Game game = new Game(settings, new Random(1));
        game.clearWorld();
        return game;
    }

    private static Game emptyGame() {
        return emptyGame(Settings.defaults());
    }

    // Générateur qui renvoie toujours la même valeur : rend les positions aléatoires prévisibles
    private static Random fixedRandom(int value) {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return value;
            }
        };
    }

    private static MeleeHero meleeHeroAt(double x, double y) {
        MeleeHero hero = new MeleeHero(new Random(1));
        hero.moveTo(x, y);
        return hero;
    }

    // ------------------------------------------------------------------ début de partie

    @Test
    void theGameStartsWithTheEnemyInTheCenterAndSomeCoins() {
        Game game = new Game(Settings.defaults(), new Random(1));
        assertEquals(Game.WIDTH / 2.0, game.getEnemy().getX());
        assertEquals(Game.HEIGHT / 2.0, game.getEnemy().getY());
        assertEquals(4, game.getCoins().size());
        assertTrue(game.getHeroes().isEmpty());
        assertFalse(game.isFinished());
        for (Coin coin : game.getCoins()) {
            assertTrue(coin.getY() >= 0 && coin.getY() + coin.getSize() <= Game.HEIGHT);
        }
    }

    // ------------------------------------------------------------------ physique de l'ennemi

    @Test
    void theEnemyFallsUnderGravity() {
        Game game = emptyGame();
        double y = game.getEnemy().getY();
        game.update(0.1);
        assertTrue(game.getEnemy().getY() > y);
        assertTrue(game.getVerticalSpeed() > 0);
    }

    @Test
    void jumpingMakesTheEnemyRise() {
        Game game = emptyGame();
        double y = game.getEnemy().getY();
        game.jump();
        game.update(0.05);
        assertTrue(game.getEnemy().getY() < y);
    }

    @Test
    void theEnemyStaysBetweenTheCeilingAndTheGround() {
        Game game = emptyGame(withoutHeroes());
        for (int i = 0; i < 30; i++) {          // saute sans arrêt : il monte jusqu'au plafond
            game.jump();
            game.update(0.05);
            assertTrue(game.getEnemy().getY() >= 0);
        }
        assertEquals(0, game.getEnemy().getY());

        for (int i = 0; i < 60; i++) {          // puis tombe jusqu'au sol
            game.update(0.05);
            assertTrue(game.getEnemy().getY() <= Game.HEIGHT - game.getEnemy().getSize());
        }
        assertEquals(Game.HEIGHT - game.getEnemy().getSize(), game.getEnemy().getY());
    }

    @Test
    void theFallSpeedIsLimited() {
        Game game = emptyGame(withoutHeroes());
        game.jump();
        for (int i = 0; i < 20; i++) {
            game.update(0.05);
            assertTrue(game.getVerticalSpeed() <= 300);
        }
    }

    // ------------------------------------------------------------------ pièces

    @Test
    void collectingACoinGivesAPointAndMakesTheEnemyHeavier() {
        Game game = emptyGame();
        Enemy enemy = game.getEnemy();
        double initialGravity = game.getGravity();
        game.addCoin(new Coin(enemy.getX() + 10, enemy.getY() + 10));

        game.update(0.01);

        assertEquals(1, game.getScore());
        assertEquals(1, game.getCollectedCoins());
        assertTrue(game.getCoins().isEmpty());
        assertEquals(initialGravity + Settings.defaults().gravityBonusPerCoin(), game.getGravity());
        assertTrue(game.pollEvents().contains(GameEvent.COIN_COLLECTED));
    }

    @Test
    void gravityIsCapped() {
        Game game = emptyGame();
        Enemy enemy = game.getEnemy();
        for (int i = 0; i < 200; i++) {
            game.addCoin(new Coin(enemy.getX() + 10, enemy.getY() + 10));
            game.update(0.001);
        }
        assertEquals(200, game.getScore());
        assertEquals(Settings.defaults().maxGravity(), game.getGravity());
    }

    @Test
    void aCoinLeftBehindOffScreenIsRemoved() {
        Game game = emptyGame();
        game.addCoin(new Coin(-39, 0));
        game.update(0.05);   // le décor recule d'environ 6 pixels
        assertTrue(game.getCoins().isEmpty());
        assertEquals(0, game.getScore());
    }

    @Test
    void coinsKeepAppearingOnTheRight() {
        Game game = emptyGame(withoutHeroes());
        double time = 0;
        while (game.getCoins().isEmpty() && time < 3) {
            game.update(0.05);
            time += 0.05;
        }
        assertFalse(game.getCoins().isEmpty());
        assertTrue(time > 0.7 && time < 1.0, "first coin around 0.8 s, got it at " + time);
        Coin coin = game.getCoins().get(0);
        assertTrue(coin.getX() <= Game.WIDTH);
        assertTrue(coin.getY() >= 0 && coin.getY() + coin.getSize() <= Game.HEIGHT);
    }

    // ------------------------------------------------------------------ tirs

    @Test
    void shootingCreatesABulletInFrontOfTheEnemy() {
        Game game = emptyGame();
        Enemy enemy = game.getEnemy();

        assertTrue(game.shoot());

        assertEquals(1, game.getBullets().size());
        Bullet bullet = game.getBullets().get(0);
        assertEquals(enemy.getX() + 90, bullet.getX() + Bullet.SIZE / 2.0, 1e-9);
        assertEquals(enemy.getY() + 25, bullet.getY() + Bullet.SIZE / 2.0, 1e-9);
        assertTrue(game.pollEvents().contains(GameEvent.SHOOT));
    }

    @Test
    void youMustWaitBetweenTwoShots() {
        Game game = emptyGame();
        assertTrue(game.shoot());
        assertFalse(game.shoot());          // trop tôt
        game.update(0.3);                   // le délai (0,25 s) est écoulé
        assertTrue(game.shoot());
    }

    @Test
    void aBulletLeavingTheScreenIsRemoved() {
        Game game = emptyGame();
        game.addBullet(new Bullet(Game.WIDTH - 1, 100));
        game.update(0.05);
        assertTrue(game.getBullets().isEmpty());
    }

    @Test
    void aBulletEliminatesAHeroThenDisappearsWithIt() {
        Game game = emptyGame();
        game.addHero(meleeHeroAt(450, 190));
        game.addBullet(new Bullet(440, 230));

        game.update(0.05);

        assertTrue(game.getHeroes().isEmpty());
        assertTrue(game.getBullets().isEmpty(), "the bullet must not keep flying, invisible");
        assertEquals(MeleeHero.SHOT_POINTS, game.getScore());
        assertEquals(1, game.getEliminatedHeroes());
        assertTrue(game.pollEvents().contains(GameEvent.HERO_ELIMINATED));
    }

    @Test
    void aBulletDoesNotEliminateTwoHeroesAtOnce() {
        Game game = emptyGame();
        game.addHero(meleeHeroAt(450, 190));
        game.addHero(meleeHeroAt(450, 190));
        game.addBullet(new Bullet(440, 230));

        game.update(0.05);

        assertEquals(1, game.getHeroes().size());
        assertEquals(MeleeHero.SHOT_POINTS, game.getScore());
    }

    @Test
    void severalBulletsAndHeroesInTheSameFrameAreAllHandled() {
        Game game = emptyGame();
        game.addHero(meleeHeroAt(450, 50));
        game.addHero(meleeHeroAt(450, 300));
        game.addBullet(new Bullet(440, 90));
        game.addBullet(new Bullet(440, 340));

        game.update(0.05);

        assertTrue(game.getHeroes().isEmpty());
        assertTrue(game.getBullets().isEmpty());
        assertEquals(2 * MeleeHero.SHOT_POINTS, game.getScore());
    }

    // ------------------------------------------------------------------ contacts avec les héros

    @Test
    void touchingAMeleeHeroEndsTheGame() {
        Game game = emptyGame();
        Enemy enemy = game.getEnemy();
        game.addHero(meleeHeroAt(enemy.getX() + 5, enemy.getY() + 5));

        game.update(0.01);

        assertTrue(game.isFinished());
        assertEquals(0, game.getHealth());
        assertTrue(game.getHeroes().isEmpty());
        List<GameEvent> events = game.pollEvents();
        assertTrue(events.contains(GameEvent.DAMAGE));
        assertTrue(events.contains(GameEvent.GAME_OVER));
    }

    @Test
    void touchingATankRemovesHealthWithoutEndingTheGame() {
        Game game = emptyGame();
        Enemy enemy = game.getEnemy();
        TankHero tank = new TankHero(new Random(1));
        tank.moveTo(enemy.getX() + 5, enemy.getY() + 5);
        game.addHero(tank);

        game.update(0.01);

        assertEquals(Enemy.MAX_HEALTH - TankHero.HEALTH_DAMAGE, game.getHealth());
        assertFalse(game.isFinished());
    }

    @Test
    void twoHeroesTouchedInTheSameFrameAreBothTakenIntoAccount() {
        Game game = emptyGame();
        Enemy enemy = game.getEnemy();
        for (int i = 0; i < 2; i++) {
            TankHero tank = new TankHero(new Random(1));
            tank.moveTo(enemy.getX() + 5, enemy.getY() + 5);
            game.addHero(tank);
        }

        game.update(0.01);

        assertTrue(game.getHeroes().isEmpty());
        assertEquals(0, game.getHealth());
        assertTrue(game.isFinished());
    }

    @Test
    void touchingAStealthHeroOnlyCostsPoints() {
        Game game = emptyGame();
        Enemy enemy = game.getEnemy();
        enemy.setScore(15);
        // Le furtif zigzague autour de son abscisse de départ : on le fait apparaître à l'aplomb de l'ennemi,
        // puis descendre jusqu'à sa hauteur
        StealthHero stealth = new StealthHero(fixedRandom((int) enemy.getX() + 5));
        stealth.spawn(Game.WIDTH, Game.HEIGHT);
        stealth.move(enemy.getY() - 20);
        game.addHero(stealth);

        game.update(0.01);

        assertEquals(15 - StealthHero.SCORE_PENALTY, game.getScore());
        assertEquals(Enemy.MAX_HEALTH, game.getHealth());
        assertFalse(game.isFinished());
    }

    @Test
    void aHeroThatLeftTheScreenIsRemoved() {
        Game game = emptyGame();
        game.addHero(meleeHeroAt(-79, 0));
        game.update(0.05);
        assertTrue(game.getHeroes().isEmpty());
        assertFalse(game.isFinished());
    }

    // ------------------------------------------------------------------ apparitions et difficulté

    @Test
    void theFirstHeroAppearsAroundThreeSeconds() {
        Game game = emptyGame();
        double time = 0;
        while (game.getHeroes().isEmpty() && time < 5) {
            game.update(0.05);
            time += 0.05;
        }
        assertFalse(game.getHeroes().isEmpty());
        assertTrue(time > 2.7 && time < 3.1, "first hero around 3 s, got it at " + time);
    }

    @Test
    void theBackgroundScrollsFasterAndFasterThenLevelsOff() {
        Game game = emptyGame(withoutHeroes());
        for (int i = 0; i < 800; i++) {           // 40 s de jeu
            game.update(0.05);
        }
        assertEquals(120 + 1.5 * 40, game.getScrollSpeed(), 0.01);

        for (int i = 0; i < 4000; i++) {          // 200 s de plus
            game.update(0.05);
        }
        assertEquals(240, game.getScrollSpeed());
    }

    @Test
    void theBackgroundAdvancesByTheDistanceTravelledByTheScenery() {
        Game game = emptyGame();
        game.update(0.1);
        assertEquals(game.getScrollSpeed() * 0.1, game.getBackgroundScroll(), 1e-9);
    }

    // ------------------------------------------------------------------ fin de partie et événements

    @Test
    void onceFinishedTheGameStopsMoving() {
        Game game = emptyGame();
        game.getEnemy().setHealth(0);
        game.update(0.01);
        assertTrue(game.isFinished());
        game.pollEvents();

        double time = game.getElapsedTime();
        game.jump();
        assertFalse(game.shoot());
        game.update(1);

        assertEquals(time, game.getElapsedTime());
        assertTrue(game.pollEvents().isEmpty());
    }

    @Test
    void aZeroOrNegativeTimeStepIsIgnored() {
        Game game = emptyGame();
        double y = game.getEnemy().getY();
        game.update(0);
        game.update(-1);
        assertEquals(0, game.getElapsedTime());
        assertEquals(y, game.getEnemy().getY());
    }

    @Test
    void eventsAreClearedOnceRetrieved() {
        Game game = emptyGame();
        game.jump();
        assertEquals(List.of(GameEvent.JUMP), game.pollEvents());
        assertTrue(game.pollEvents().isEmpty());
    }
}
