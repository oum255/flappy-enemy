module flappyenemy {
    requires transitive javafx.controls;
    requires java.desktop;   // javax.sound.sampled : effets sonores

    exports com.flappyenemy.model;
    exports com.flappyenemy.controller;
    exports com.flappyenemy.view;
}
