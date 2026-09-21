package com.flappyenemy.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Les meilleurs scores, sauvegardés dans un fichier texte (un score par ligne) pour survivre
// à la fermeture du jeu.
public class HighScores {
    public static final int DEFAULT_COUNT = 5;

    private final Path file;
    private final int maximum;
    private final List<Integer> scores = new ArrayList<>();   // Triés du meilleur au moins bon

    public HighScores(Path file, int maximum) {
        this.file = file;
        this.maximum = maximum;
        load();
    }

    // Emplacement habituel : ~/.flappy-enemy/scores.txt
    public static HighScores atDefaultLocation() {
        Path directory = Path.of(System.getProperty("user.home"), ".flappy-enemy");
        return new HighScores(directory.resolve("scores.txt"), DEFAULT_COUNT);
    }

    public List<Integer> getScores() {
        return Collections.unmodifiableList(scores);
    }

    public int getBest() {
        return scores.isEmpty() ? 0 : scores.get(0);
    }

    // Enregistre un score. Retourne vrai s'il bat le record. Un score nul n'est pas enregistré.
    public boolean add(int score) {
        if (score <= 0) return false;
        boolean record = score > getBest();
        scores.add(score);
        sort();
        save();
        return record;
    }

    private void sort() {
        scores.sort(Collections.reverseOrder());
        while (scores.size() > maximum) {
            scores.remove(scores.size() - 1);
        }
    }

    // Lit le fichier s'il existe ; les lignes illisibles sont ignorées
    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                try {
                    int score = Integer.parseInt(line.trim());
                    if (score > 0) scores.add(score);
                } catch (NumberFormatException e) {
                    // ligne ignorée
                }
            }
            sort();
        } catch (IOException e) {
            System.err.println("Unable to read high scores: " + e.getMessage());
        }
    }

    // Une erreur d'écriture ne doit pas interrompre le jeu : on la signale simplement
    private void save() {
        try {
            Path directory = file.getParent();
            if (directory != null) Files.createDirectories(directory);
            List<String> lines = new ArrayList<>();
            for (int score : scores) lines.add(Integer.toString(score));
            Files.write(file, lines);
        } catch (IOException e) {
            System.err.println("Unable to save high scores: " + e.getMessage());
        }
    }
}
