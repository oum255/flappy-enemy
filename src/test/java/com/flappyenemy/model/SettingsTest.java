package com.flappyenemy.model;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SettingsTest {

    @Test
    void emptyPropertiesGiveTheDefaultValues() {
        assertEquals(Settings.defaults(), Settings.from(new Properties()));
    }

    @Test
    void aPropertyReplacesTheCorrespondingDefaultValue() {
        Properties properties = new Properties();
        properties.setProperty("gravity.value", "700");

        Settings settings = Settings.from(properties);

        assertEquals(700, settings.gravity());
        assertEquals(Settings.defaults().bulletSpeed(), settings.bulletSpeed());
    }

    @Test
    void anUnreadableValueIsIgnored() {
        Properties properties = new Properties();
        properties.setProperty("gravity.value", "a lot");
        properties.setProperty("bullet.speed", "450");

        Settings settings = Settings.from(properties);

        assertEquals(Settings.defaults().gravity(), settings.gravity());
        assertEquals(450, settings.bulletSpeed());
    }

    @Test
    void inconsistentValuesFallBackToTheDefaults() {
        Properties properties = new Properties();
        properties.setProperty("jump.speed", "300");   // un saut doit aller vers le haut (valeur négative)
        assertEquals(Settings.defaults(), Settings.from(properties));
    }

    @Test
    void theConstructorRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new Settings(
                120, 1.5, 240, -500, 900, 10, -300, 300, 300, 0.25, 3, 1.2, 0.03, 0.8));
        assertThrows(IllegalArgumentException.class, () -> new Settings(
                120, 1.5, 240, 500, 900, 10, 0, 300, 300, 0.25, 3, 1.2, 0.03, 0.8));
        assertThrows(IllegalArgumentException.class, () -> new Settings(
                Double.NaN, 1.5, 240, 500, 900, 10, -300, 300, 300, 0.25, 3, 1.2, 0.03, 0.8));
    }

    // Garde-fou : le fichier game.properties livré avec le jeu doit rester cohérent avec les valeurs par défaut
    @Test
    void theShippedConfigurationFileMatchesTheDefaultValues() {
        assertEquals(Settings.defaults(), Settings.load());
    }
}
