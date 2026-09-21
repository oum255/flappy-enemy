package com.flappyenemy.model;

import java.util.Random;
import java.util.function.Function;

// Les différents types de héros. L'énumération joue aussi le rôle de fabrique (Factory) :
// ajouter un nouveau héros revient à écrire sa classe et à ajouter une ligne ici.
public enum HeroType {
    MELEE(MeleeHero::new),
    STEALTH(StealthHero::new),
    TANK(TankHero::new);

    private final Function<Random, Hero> factory;

    HeroType(Function<Random, Hero> factory) {
        this.factory = factory;
    }

    public Hero create(Random random) {
        return factory.apply(random);
    }

    // Crée un héros d'un type tiré au hasard
    public static Hero createRandom(Random random) {
        HeroType[] types = values();
        return types[random.nextInt(types.length)].create(random);
    }
}
