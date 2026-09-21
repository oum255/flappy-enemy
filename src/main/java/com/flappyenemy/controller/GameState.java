package com.flappyenemy.controller;

// Les différents états dans lesquels se trouve le jeu.
public enum GameState {
    MENU,        // Écran d'accueil, avant la première partie
    RUNNING,     // La partie se joue
    PAUSED,      // La partie est suspendue
    GAME_OVER    // La partie est terminée
}
