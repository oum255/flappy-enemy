package com.flappyenemy.model;

// Événements survenus pendant la partie. La vue s'en sert pour jouer les sons,
// sans que le modèle ait besoin de connaître le son.
public enum GameEvent {
    JUMP,
    SHOOT,
    COIN_COLLECTED,
    HERO_ELIMINATED,
    DAMAGE,
    GAME_OVER
}
