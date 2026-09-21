package com.flappyenemy.view;

import com.flappyenemy.model.GameEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

// Joue les effets sonores associés aux événements du jeu.
// Le son est facultatif : si un fichier ou la sortie audio est indisponible, le jeu continue en silence.
public class Sounds {
    private final Map<GameEvent, Clip> clips = new EnumMap<>(GameEvent.class);
    private boolean enabled = true;

    public Sounds() {
        load(GameEvent.JUMP, "/jump.wav");
        load(GameEvent.SHOOT, "/shoot.wav");
        load(GameEvent.COIN_COLLECTED, "/coin.wav");
        load(GameEvent.HERO_ELIMINATED, "/eliminated.wav");
        load(GameEvent.DAMAGE, "/damage.wav");
        load(GameEvent.GAME_OVER, "/gameover.wav");
    }

    private void load(GameEvent event, String resource) {
        try (InputStream stream = Sounds.class.getResourceAsStream(resource)) {
            if (stream == null) return;
            try (AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(stream))) {
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                clips.put(event, clip);
            }
        } catch (Exception e) {
            // Pas de carte son, format non pris en charge...: on joue sans ce son
            System.err.println("Sound unavailable (" + resource + "): " + e.getMessage());
        }
    }

    public void play(GameEvent event) {
        Clip clip = clips.get(event);
        if (!enabled || clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void toggleMute() {
        enabled = !enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
