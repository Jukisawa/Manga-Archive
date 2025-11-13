package com.jukisawa.mangaarchive.ui.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;


import java.util.function.Function;

public class CustomDropdown<T> extends HBox {

    private final Label displayLabel = new Label("Select...");
    private final Popup popup = new Popup();
    private final VBox listBox = new VBox();
    private final ScrollPane scrollPane = new ScrollPane();
    private ObservableList<T> items;
    private final ObjectProperty<T> selectedItem = new SimpleObjectProperty<>();
    private Function<T, String> displayFunction;

    public CustomDropdown() {
        setupUI();
    }

    private void setupUI() {
        setPadding(new Insets(5));
        displayLabel.setPadding(new Insets(5, 10, 5, 10));
        displayLabel.setStyle("""
            -fx-border-color: #888;
            -fx-border-radius: 6;
        """);
        getChildren().add(displayLabel);

        listBox.setSpacing(3);
        listBox.setPadding(new Insets(5));

        scrollPane.setContent(listBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(180);
        scrollPane.setMaxHeight(500);
        scrollPane.setStyle("-fx-border-color: #888;");

        popup.getContent().add(scrollPane);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, _ -> togglePopup());

        selectedItem.addListener((_, _, newVal) -> displayLabel.setText(newVal != null ? newVal.toString() : "Select..."));
    }

    public void setItems(ObservableList<T> items) {
        this.items = items;
        refreshList();
    }

    public void refreshList() {
        listBox.getChildren().clear();
        if (items == null) return;

        for (T item : items) {
            String displayText;
            if (displayFunction != null) {
                displayText = displayFunction.apply(item);
            } else {
                displayText = item != null ? item.toString() : "";
            }

            Label lbl = new Label(displayText);
            lbl.setPrefWidth(150);
            lbl.setPadding(new Insets(5));
//            lbl.setOnMouseEntered(e -> lbl.setStyle("-fx-background-color: #efefef;"));
//            lbl.setOnMouseExited(e -> lbl.setStyle("-fx-background-color: transparent;"));
            lbl.setOnMouseClicked(_ -> selectItem(item));
            listBox.getChildren().add(lbl);
        }
    }
    public void refreshDisplay() {
        T sel = selectedItem.get();
        displayLabel.setText(sel != null ? sel.toString() : "Select...");
    }

    private void togglePopup() {
        if (popup.isShowing()) {
            popup.hide();
        } else {
            Bounds bounds = localToScreen(getBoundsInLocal());
            popup.show(this, bounds.getMinX(), bounds.getMaxY());
        }
    }

    private void selectItem(T item) {
        selectedItem.set(item);
        popup.hide();
    }

    public T getSelectedItem() {
        return selectedItem.get();
    }

    public void setSelectedItem(T item) {
        selectedItem.set(item);
    }

    public ObjectProperty<T> selectedItemProperty() {
        return selectedItem;
    }

    public void setDisplayFunction(Function<T, String> function) {
        this.displayFunction = function;
        refreshList();
    }
}