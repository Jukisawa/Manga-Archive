package com.jukisawa.mangaarchive.ui.components;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.stage.Popup;
import javafx.stage.Window;
import java.util.function.Function;

public class HoverPopup<T> {

    private final Popup popup = new Popup();
    private final Function<T, Node> nodeProvider;

    public HoverPopup(Function<T, Node> nodeProvider) {
        this.nodeProvider = nodeProvider;
        popup.setAutoHide(true);
        popup.setAutoFix(true);
    }

    public void showFor(T item, Node owner) {
        if (item == null) return;

        Node content = nodeProvider.apply(item);
        show(content, owner);
    }

    public void show(Node content, Node owner) {
        if (content == null) return;

        popup.getContent().setAll(content);

        Window window = owner.getScene().getWindow();

        popup.setOpacity(0);
        popup.show(window);

        Platform.runLater(() -> {
            Bounds ownerBounds = owner.localToScreen(owner.getBoundsInLocal());
            double popupHeight = content.getBoundsInLocal().getHeight();

            double viewportBottom = window.getY() + window.getHeight();

            double spaceBelow = viewportBottom - ownerBounds.getMaxY();
            double finalY;

            if (spaceBelow < popupHeight + 5) {
                finalY = ownerBounds.getMinY() - popupHeight - 10; // oben
            } else {
                finalY = ownerBounds.getMaxY(); // unten
            }

            double finalX = ownerBounds.getMinX() + 15;

            popup.setX(finalX);
            popup.setY(finalY);

            popup.setOpacity(1);
        });
    }

    public void hide() {
        popup.hide();
    }
}