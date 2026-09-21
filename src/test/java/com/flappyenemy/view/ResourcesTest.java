package com.flappyenemy.view;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Vérifie que toutes les ressources attendues par la vue sont bien présentes et lisibles.
// Un fichier renommé ou oublié ferait sinon planter le jeu au démarrage, sans qu'aucun autre test ne s'en aperçoive.
class ResourcesTest {
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G'};
    private static final byte[] WAV_SIGNATURE = {'R', 'I', 'F', 'F'};

    private static byte[] firstBytes(String resource) throws IOException {
        try (InputStream stream = ResourcesTest.class.getResourceAsStream("/" + resource)) {
            assertNotNull(stream, "missing resource: " + resource);
            return stream.readNBytes(4);
        }
    }

    @Test
    void allImagesArePresentAndArePngFiles() throws IOException {
        List<String> images = List.of("ghost.png", "icon.png", "melee.png", "stealth.png",
                "tank.png", "coin.png", "background.png");
        for (String image : images) {
            assertArrayEquals(PNG_SIGNATURE, firstBytes(image), image + " is not a PNG file");
        }
    }

    @Test
    void allSoundsArePresentAndAreWavFiles() throws IOException {
        List<String> sounds = List.of("jump.wav", "shoot.wav", "coin.wav",
                "eliminated.wav", "damage.wav", "gameover.wav");
        for (String sound : sounds) {
            assertArrayEquals(WAV_SIGNATURE, firstBytes(sound), sound + " is not a WAV file");
        }
    }

    @Test
    void theStylesheetAndTheConfigurationArePresent() {
        assertNotNull(ResourcesTest.class.getResource("/style.css"));
        assertNotNull(ResourcesTest.class.getResource("/game.properties"));
    }
}
