package com.flappyenemy.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighScoresTest {

    @TempDir
    Path directory;

    @Test
    void thereAreNoScoresAtFirst() {
        HighScores scores = new HighScores(directory.resolve("scores.txt"), 5);
        assertTrue(scores.getScores().isEmpty());
        assertEquals(0, scores.getBest());
    }

    @Test
    void scoresAreSortedFromBestToWorst() {
        HighScores scores = new HighScores(directory.resolve("scores.txt"), 5);
        scores.add(10);
        scores.add(50);
        scores.add(30);
        assertEquals(List.of(50, 30, 10), scores.getScores());
        assertEquals(50, scores.getBest());
    }

    @Test
    void onlyTheBestScoresAreKept() {
        HighScores scores = new HighScores(directory.resolve("scores.txt"), 3);
        for (int score : new int[]{10, 50, 30, 20, 40}) {
            scores.add(score);
        }
        assertEquals(List.of(50, 40, 30), scores.getScores());
    }

    @Test
    void addTellsWhetherTheRecordWasBeaten() {
        HighScores scores = new HighScores(directory.resolve("scores.txt"), 5);
        assertTrue(scores.add(10));    // premier score : c'est un record
        assertFalse(scores.add(5));
        assertTrue(scores.add(20));
        assertFalse(scores.add(20));   // une égalité n'est pas un record
    }

    @Test
    void aZeroOrNegativeScoreIsNotSaved() {
        Path file = directory.resolve("scores.txt");
        HighScores scores = new HighScores(file, 5);
        assertFalse(scores.add(0));
        assertFalse(scores.add(-4));
        assertTrue(scores.getScores().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void scoresSurviveARestart() {
        Path file = directory.resolve("scores.txt");
        HighScores first = new HighScores(file, 5);
        first.add(12);
        first.add(99);

        HighScores reloaded = new HighScores(file, 5);
        assertEquals(List.of(99, 12), reloaded.getScores());
    }

    @Test
    void unreadableLinesInTheFileAreIgnored() throws IOException {
        Path file = directory.resolve("scores.txt");
        Files.write(file, List.of("12", "abc", "", "-5", "40", "0"));

        HighScores scores = new HighScores(file, 5);
        assertEquals(List.of(40, 12), scores.getScores());
    }

    @Test
    void missingDirectoriesAreCreated() {
        Path file = directory.resolve("a").resolve("b").resolve("scores.txt");
        HighScores scores = new HighScores(file, 5);
        scores.add(7);
        assertTrue(Files.exists(file));
    }
}
