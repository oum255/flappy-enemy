package com.flappyenemy.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Une partie de jeu : l'état du monde (ennemi, héros, pièces, balles) et toutes les règles.
// Cette classe ne dépend pas de JavaFX : elle est pilotée par update(dt) et se teste sans fenêtre.
public class Game {
    public static final int WIDTH = 600;              // Largeur du monde
    public static final int HEIGHT = 400;             // Hauteur du monde
    private static final int INITIAL_COINS = 4;       // Pièces déjà présentes au démarrage
    private static final int BULLET_OFFSET_X = 90;    // Position de départ des balles par rapport à l'ennemi
    private static final int BULLET_OFFSET_Y = 25;

    private final Settings settings;
    private final Random random;

    private final Enemy enemy = new Enemy();
    private final List<Hero> heroes = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<GameEvent> events = new ArrayList<>();

    private double verticalSpeed;                     // Vitesse verticale de l'ennemi
    private double gravity;
    private double scrollSpeed;
    private double backgroundScroll;                  // Distance totale parcourue par le décor (pour le fond)
    private double elapsedTime;
    private double timeSinceShot;
    private double heroTimer;
    private double coinTimer;
    private int collectedCoins;
    private int eliminatedHeroes;
    private boolean finished;

    public Game(Settings settings, Random random) {
        this.settings = settings;
        this.random = random;
        this.gravity = settings.gravity();
        this.scrollSpeed = settings.scrollSpeed();
        this.timeSinceShot = settings.shootDelay();   // Le premier tir est possible immédiatement
        enemy.moveTo(WIDTH / 2.0, HEIGHT / 2.0);
        for (int i = 0; i < INITIAL_COINS; i++) {
            coins.add(new Coin(WIDTH * 0.6 + random.nextInt(WIDTH), random.nextInt(HEIGHT - Coin.SIZE)));
        }
    }

    public Game(Settings settings) {
        this(settings, new Random());
    }

    // ------------------------------------------------------------------ actions du joueur

    // Fait sauter l'ennemi : sa vitesse verticale devient celle du saut
    public void jump() {
        if (finished) return;
        verticalSpeed = settings.jumpSpeed();
        events.add(GameEvent.JUMP);
    }

    // Tire une balle si le délai entre deux tirs est écoulé. Retourne vrai si la balle est partie.
    public boolean shoot() {
        if (finished || timeSinceShot < settings.shootDelay()) return false;
        timeSinceShot = 0;
        bullets.add(new Bullet(enemy.getX() + BULLET_OFFSET_X, enemy.getY() + BULLET_OFFSET_Y));
        events.add(GameEvent.SHOOT);
        return true;
    }

    // ------------------------------------------------------------------ boucle de jeu

    // Fait avancer la partie de "dt" secondes
    public void update(double dt) {
        if (finished || dt <= 0) return;

        elapsedTime += dt;
        timeSinceShot += dt;

        // Difficulté progressive : le décor défile de plus en plus vite
        scrollSpeed = Math.min(settings.maxScrollSpeed(),
                settings.scrollSpeed() + settings.scrollAcceleration() * elapsedTime);
        double distance = scrollSpeed * dt;
        backgroundScroll += distance;

        moveEnemy(dt);
        moveBullets(dt);
        moveCoins(distance);
        moveHeroes(dt, distance);
        spawnItems(dt);

        if (enemy.isDead()) {
            finished = true;
            events.add(GameEvent.GAME_OVER);
        }
    }

    // Gravité, chute limitée, et maintien de l'ennemi entre le plafond et le sol
    private void moveEnemy(double dt) {
        verticalSpeed = Math.min(settings.maxFallSpeed(), verticalSpeed + gravity * dt);
        enemy.move(verticalSpeed * dt);

        double maxY = HEIGHT - enemy.getSize();
        if (enemy.getY() > maxY) {
            enemy.moveTo(enemy.getX(), maxY);
            verticalSpeed = 0;
        }
        if (enemy.getY() < 0) {
            enemy.moveTo(enemy.getX(), 0);
            verticalSpeed = 0;
        }
    }

    // Les parcours se font à l'envers pour pouvoir supprimer un élément sans en sauter un autre
    private void moveBullets(double dt) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.advance(settings.bulletSpeed() * dt);
            if (bullet.getX() > WIDTH || eliminateHitHero(bullet)) {
                bullets.remove(i);
            }
        }
    }

    // Si la balle touche un héros, celui-ci est éliminé et l'ennemi gagne des points
    private boolean eliminateHitHero(Bullet bullet) {
        for (int i = heroes.size() - 1; i >= 0; i--) {
            Hero hero = heroes.get(i);
            if (bullet.intersects(hero)) {
                enemy.setScore(enemy.getScore() + hero.shotPoints());
                heroes.remove(i);
                eliminatedHeroes++;
                events.add(GameEvent.HERO_ELIMINATED);
                return true;
            }
        }
        return false;
    }

    private void moveCoins(double distance) {
        for (int i = coins.size() - 1; i >= 0; i--) {
            Coin coin = coins.get(i);
            coin.translate(-distance, 0);
            if (coin.getX() + coin.getSize() < 0) {
                coins.remove(i);
            } else if (coin.intersects(enemy)) {
                coins.remove(i);
                enemy.incrementScore();
                collectedCoins++;
                // Plus on ramasse de pièces, plus l'ennemi devient lourd
                gravity = Math.min(settings.maxGravity(), gravity + settings.gravityBonusPerCoin());
                events.add(GameEvent.COIN_COLLECTED);
            }
        }
    }

    private void moveHeroes(double dt, double distance) {
        for (int i = heroes.size() - 1; i >= 0; i--) {
            Hero hero = heroes.get(i);
            hero.move(distance);
            hero.update(dt, HEIGHT);
            if (hero.isOut(HEIGHT)) {
                heroes.remove(i);
            } else if (hero.intersects(enemy)) {
                hero.applyContact(enemy);
                heroes.remove(i);
                events.add(GameEvent.DAMAGE);
            }
        }
    }

    // Apparition périodique des héros (de plus en plus fréquente) et des pièces
    private void spawnItems(double dt) {
        heroTimer += dt;
        double heroDelay = Math.max(settings.minHeroDelay(),
                settings.heroDelay() - settings.heroDelayReduction() * elapsedTime);
        if (heroTimer >= heroDelay) {
            heroTimer = 0;
            Hero hero = HeroType.createRandom(random);
            hero.spawn(WIDTH, HEIGHT);
            heroes.add(hero);
        }

        coinTimer += dt;
        if (coinTimer >= settings.coinDelay()) {
            coinTimer = 0;
            coins.add(new Coin(WIDTH, random.nextInt(HEIGHT - Coin.SIZE)));
        }
    }

    // ------------------------------------------------------------------ événements

    // Retourne les événements survenus depuis le dernier appel, puis les oublie
    public List<GameEvent> pollEvents() {
        List<GameEvent> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }

    // ------------------------------------------------------------------ accès en lecture

    public Enemy getEnemy() {
        return enemy;
    }

    public List<Hero> getHeroes() {
        return Collections.unmodifiableList(heroes);
    }

    public List<Coin> getCoins() {
        return Collections.unmodifiableList(coins);
    }

    public List<Bullet> getBullets() {
        return Collections.unmodifiableList(bullets);
    }

    public boolean isFinished() {
        return finished;
    }

    public int getScore() {
        return enemy.getScore();
    }

    public int getHealth() {
        return enemy.getHealth();
    }

    public int getCollectedCoins() {
        return collectedCoins;
    }

    public int getEliminatedHeroes() {
        return eliminatedHeroes;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public double getBackgroundScroll() {
        return backgroundScroll;
    }

    public double getScrollSpeed() {
        return scrollSpeed;
    }

    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    public double getGravity() {
        return gravity;
    }

    // Accès réservés aux tests du même paquet : placer précisément des éléments dans le monde
    void addHero(Hero hero) {
        heroes.add(hero);
    }

    void addCoin(Coin coin) {
        coins.add(coin);
    }

    void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    void clearWorld() {
        heroes.clear();
        coins.clear();
        bullets.clear();
    }
}
