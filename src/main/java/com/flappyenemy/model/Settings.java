package com.flappyenemy.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Paramètres d'équilibrage du jeu. Les valeurs par défaut peuvent être modifiées dans le fichier
// game.properties (voir les ressources) sans recompiler le code.
// Vitesses en pixels par seconde, durées en secondes.
public record Settings(
        double scrollSpeed,              // Vitesse de défilement du décor au départ
        double scrollAcceleration,       // Vitesse gagnée par seconde de jeu (difficulté progressive)
        double maxScrollSpeed,           // Plafond de la vitesse de défilement
        double gravity,                  // Gravité au départ
        double maxGravity,               // Plafond de la gravité
        double gravityBonusPerCoin,      // Gravité gagnée à chaque pièce ramassée
        double jumpSpeed,                // Vitesse verticale donnée par un saut (négative = vers le haut)
        double maxFallSpeed,             // Vitesse de chute maximale
        double bulletSpeed,              // Vitesse des balles
        double shootDelay,               // Délai minimal entre deux tirs
        double heroDelay,                // Délai entre deux apparitions de héros au départ
        double minHeroDelay,             // Plancher de ce délai
        double heroDelayReduction,       // Réduction du délai par seconde de jeu
        double coinDelay) {              // Délai entre deux apparitions de pièces

    // Vérifie la cohérence des valeurs : un jeu avec une gravité négative n'aurait aucun sens
    public Settings {
        requirePositive(scrollSpeed, "scrollSpeed");
        requireNonNegative(scrollAcceleration, "scrollAcceleration");
        requirePositive(maxScrollSpeed, "maxScrollSpeed");
        requirePositive(gravity, "gravity");
        requirePositive(maxGravity, "maxGravity");
        requireNonNegative(gravityBonusPerCoin, "gravityBonusPerCoin");
        requirePositive(maxFallSpeed, "maxFallSpeed");
        requirePositive(bulletSpeed, "bulletSpeed");
        requireNonNegative(shootDelay, "shootDelay");
        requirePositive(heroDelay, "heroDelay");
        requirePositive(minHeroDelay, "minHeroDelay");
        requireNonNegative(heroDelayReduction, "heroDelayReduction");
        requirePositive(coinDelay, "coinDelay");
        if (!(jumpSpeed < 0)) {
            throw new IllegalArgumentException("jumpSpeed must be negative (upwards): " + jumpSpeed);
        }
    }

    private static void requirePositive(double value, String name) {
        if (!(value > 0) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be strictly positive: " + value);
        }
    }

    private static void requireNonNegative(double value, String name) {
        if (!(value >= 0) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be positive or zero: " + value);
        }
    }

    public static Settings defaults() {
        return new Settings(
                120, 1.5, 240,
                500, 900, 10,
                -300, 300,
                300, 0.25,
                3.0, 1.2, 0.03,
                0.8);
    }

    // Charge les paramètres depuis game.properties (dans les ressources). En cas de problème
    // (fichier absent, valeur invalide), on retombe sur les valeurs par défaut.
    public static Settings load() {
        Properties properties = new Properties();
        try (InputStream stream = Settings.class.getResourceAsStream("/game.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException e) {
            System.err.println("Unable to read game.properties, using default values: " + e.getMessage());
        }
        return from(properties);
    }

    // Construit les paramètres à partir de propriétés ; toute clé absente ou invalide prend sa valeur par défaut
    public static Settings from(Properties p) {
        Settings d = defaults();
        try {
            return new Settings(
                    read(p, "scroll.speed", d.scrollSpeed),
                    read(p, "scroll.acceleration", d.scrollAcceleration),
                    read(p, "scroll.maxSpeed", d.maxScrollSpeed),
                    read(p, "gravity.value", d.gravity),
                    read(p, "gravity.max", d.maxGravity),
                    read(p, "gravity.bonusPerCoin", d.gravityBonusPerCoin),
                    read(p, "jump.speed", d.jumpSpeed),
                    read(p, "fall.maxSpeed", d.maxFallSpeed),
                    read(p, "bullet.speed", d.bulletSpeed),
                    read(p, "shoot.delay", d.shootDelay),
                    read(p, "heroes.delay", d.heroDelay),
                    read(p, "heroes.minDelay", d.minHeroDelay),
                    read(p, "heroes.delayReduction", d.heroDelayReduction),
                    read(p, "coins.delay", d.coinDelay));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid settings, using default values: " + e.getMessage());
            return d;
        }
    }

    private static double read(Properties p, String key, double defaultValue) {
        String text = p.getProperty(key);
        if (text == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            System.err.println("Unreadable value for " + key + ": " + text + " (using default value)");
            return defaultValue;
        }
    }
}
