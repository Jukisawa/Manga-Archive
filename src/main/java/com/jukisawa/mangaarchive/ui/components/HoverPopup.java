package com.jukisawa.mangaarchive.ui.components;

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
        if (content == null) return;

        popup.getContent().setAll(content);

        Window window = owner.getScene().getWindow();
        Point2D screenPos = owner.localToScreen(0, owner.getBoundsInParent().getHeight());
        popup.show(window, screenPos.getX() + 15, screenPos.getY());
    }

    public void show(Node content, Node owner) {
        if (content == null) return;
        popup.getContent().setAll(content);
        Window window = owner.getScene().getWindow();
        Point2D screenPos = owner.localToScreen(0, owner.getBoundsInParent().getHeight());
        popup.show(window, screenPos.getX() + 15, screenPos.getY());
    }

    public void hide() {
        popup.hide();
    }
}
