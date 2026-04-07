package com.example.oop_20252.util;

import javafx.scene.Scene;

/**
 * Optional helper to attach {@code app.css} to a {@link Scene} from code (e.g. tests).
 * <p>
 * Normal UI screens load {@code app.css} via {@code stylesheets="@app.css"} on the FXML root.
 */
public final class FxStyles {

    private static final String APP_CSS = "/com/example/oop_20252/app.css";

    private FxStyles() {
    }

    /**
     * Applies the shared stylesheet to a scene (idempotent if already added).
     */
    public static void applyAppStylesheet(Scene scene) {
        if (scene == null) {
            return;
        }
        var url = FxStyles.class.getResource(APP_CSS);
        if (url == null) {
            return;
        }
        String external = url.toExternalForm();
        if (!scene.getStylesheets().contains(external)) {
            scene.getStylesheets().add(external);
        }
    }
}
