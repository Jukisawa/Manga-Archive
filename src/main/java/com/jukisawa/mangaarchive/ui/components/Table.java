package com.jukisawa.mangaarchive.ui.components;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Eine generische Tabelle mit Filter und Nested Tables.
 */
public class Table<T> extends VBox {

    private final VBox tableBody = new VBox();
    private final HBox header;
    private final List<Column<T>> columns = new ArrayList<>();
    private ObservableList<T> items = FXCollections.observableArrayList();
    private Function<T, Node> nestedTableProvider;

    private boolean ascending = true;
    private Column<T> lastSortedColumn = null;

    private double totalWidth = 0;

    private final javafx.collections.ListChangeListener<T> listChangeListener = _ -> refresh();

    public Table() {
        setSpacing(5);
        setPadding(new Insets(10));
        HBox filterBox = new HBox(5);
        getChildren().add(filterBox);

        // Tabellenstruktur
        header = new HBox();
        getChildren().add(header);
        getChildren().add(tableBody);

        // Spalten-Listener
        ChangeListener<Number> widthListener = (_, _, newW) -> resizeColumns(newW.doubleValue());
        widthProperty().addListener(widthListener);

        // CSS Types
        tableBody.getStyleClass().add("table");
        header.getStyleClass().add("table-header");

    }

    public void addStringColumn(String title, int minWidth, Function<T, String> valueProvider, boolean centerContent,
                                boolean centerHeader) {
        addStringColumn(title, minWidth, valueProvider, t -> valueProvider.apply(t).toLowerCase(), centerContent,
                centerHeader);
    }

    public void addStringColumn(String title, int width, Function<T, String> valueProvider,
                                Function<T, String> sortKeyExtractor, boolean centerContent, boolean centerHeader) {
        addNodeColumn(title, width, t -> {
            Label label = new Label(valueProvider.apply(t));
            label.setPrefWidth(width);
            if (centerContent) {
                label.setAlignment(Pos.CENTER);
                label.setMaxWidth(Double.MAX_VALUE);
            }
            return label;
        }, sortKeyExtractor, centerContent, centerHeader);
    }

    public void addActionColumn(Runnable onAdd, Consumer<T> onEdit) {
        columns.add(new Column<>("Aktion", 80, t -> {
            Button editBtn = new Button();
            editBtn.setGraphic(createEditIcon());
            editBtn.setOnAction(_ -> onEdit.accept(t));
            editBtn.setStyle("-fx-background-color: transparent;");
            return editBtn;
        }, onAdd, null, true));
        updateHeader();
        refresh();
    }

    public void addNodeColumn(String title, int width, Function<T, Node> nodeProvider, boolean centerContent,
                              boolean centerHeader) {
        addNodeColumn(title, width, nodeProvider, null, centerContent, centerHeader);
    }

    public void addNodeColumn(String title, int width, Function<T, Node> nodeProvider, Function<T, String> sortKey,
                              boolean centerContent, boolean centerHeader) {
        columns.add(new Column<>(title, width, nodeProvider, null, sortKey, centerHeader));
        updateHeader();
        refresh();
    }

    public void setItems(ObservableList<T> items) {
        // remove old listener if any
        if (this.items != null) {
            this.items.removeListener(listChangeListener);
        }

        this.items = items;

        // listen to changes in the list
        this.items.addListener(listChangeListener);

        refresh();
    }

    public void setNestedTableProvider(Function<T, Node> provider) {
        this.nestedTableProvider = provider;
    }

    private void resizeColumns(double totalWidth) {
        this.totalWidth = totalWidth;
        if (columns.isEmpty())
            return;

        double minWidthSum = columns.stream().mapToDouble(c -> c.minWidth).sum();
        double extraSpace = Math.max(0, totalWidth - minWidthSum - 40);
        double perColumnExtra = extraSpace / columns.size();

        for (Column<T> col : columns) {
            col.currentWidth = col.minWidth + perColumnExtra;
        }

        applyColumnWidths();
        updateHeaderWidths();
    }

    private void updateHeaderWidths() {
        if (getChildren().isEmpty())
            return;

        for (int i = 0; i < columns.size() && i < header.getChildren().size(); i++) {
            Node node = header.getChildren().get(i);
            Column<T> col = columns.get(i);

            if (node instanceof Region region) {
                region.setPrefWidth(col.currentWidth);
                region.setMinWidth(col.minWidth);
            }
        }
    }

    private void sortByColumn(Column<T> col) {
        if (col.sortKeyExtractor == null)
            return;

        if (col == lastSortedColumn) {
            ascending = !ascending;
        } else {
            ascending = true;
            lastSortedColumn = col;
        }

        Comparator<T> comparator = Comparator.comparing(
                col.sortKeyExtractor,
                Comparator.nullsLast((String s1, String s2) -> {
                    if (s1 == null && s2 == null)
                        return 0;
                    if (s1 == null)
                        return 1;
                    if (s2 == null)
                        return -1;

                    // Check if both strings are integers
                    if (s1.matches("\\d+") && s2.matches("\\d+")) {
                        return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
                    }

                    // Fallback: case-insensitive string compare
                    return s1.toLowerCase().compareTo(s2.toLowerCase());
                }));
        if (!ascending) {
            comparator = comparator.reversed();
        }

        FXCollections.sort(items, comparator);
        refresh();
    }

    private void updateHeader() {
        header.getChildren().clear();
        for (Column<T> col : columns) {
            if (col.headerAction != null) {
                Button addBtn = new Button();
                addBtn.setGraphic(createPlusIcon());
                addBtn.setOnAction(_ -> col.headerAction.run());
                addBtn.setPrefWidth(col.currentWidth);
                addBtn.setMinWidth(col.minWidth);
                addBtn.setStyle("-fx-background-color: transparent;");
                header.getChildren().add(addBtn);
                if (col.centerHeader) {
                    addBtn.setAlignment(Pos.CENTER);
                    addBtn.setMaxWidth(Double.MAX_VALUE);
                }
            } else {
                Label label = new Label(col.title);
                label.setPrefWidth(col.currentWidth);
                label.setMinWidth(col.minWidth);
                label.setStyle("-fx-font-weight: bold;");
                if (col.centerHeader) {
                    label.setAlignment(Pos.CENTER);
                    label.setMaxWidth(Double.MAX_VALUE);
                }
                label.setOnMouseClicked(_ -> {
                    if (col.sortKeyExtractor != null) {
                        sortByColumn(col);
                    }
                });
                header.getChildren().add(label);
            }
        }
    }

    private void applyColumnWidths() {
        // Header aktualisieren
        for (int i = 0; i < columns.size() && i < header.getChildren().size(); i++) {
            Node node = header.getChildren().get(i);
            Column<T> col = columns.get(i);
            if (node instanceof Region region) {
                region.setPrefWidth(col.currentWidth);
            }
        }

        // Zeilen aktualisieren
        for (Node node : tableBody.getChildren()) {
            if (node instanceof HBox row) {
                for (int i = 0; i < columns.size() && i < row.getChildren().size(); i++) {
                    Node cell = row.getChildren().get(i);
                    Column<T> col = columns.get(i);
                    if (cell instanceof Region region) {
                        region.setPrefWidth(col.currentWidth);
                    }
                }
            }
        }
    }

    private void refresh() {
        tableBody.getChildren().clear();

        for (T t : items) {
            HBox row = new HBox();
            row.getStyleClass().add("table-row");

            for (Column<T> col : columns) {
                Node cell = col.nodeProvider.apply(t);
                cell.getStyleClass().add("table-cell");
                cell.prefWidth(col.currentWidth);
                row.getChildren().add(cell);
            }

            // Row über Nested Table
            tableBody.getChildren().add(row);
            // Nested Table
            Node nestedNode = nestedTableProvider != null ? nestedTableProvider.apply(t) : null;
            if (nestedNode != null) {
                nestedNode.setVisible(false);
                nestedNode.setManaged(false);
                tableBody.getChildren().add(nestedNode);

                // Row Click
                row.setOnMouseClicked(_ -> {
                    boolean show = !nestedNode.isVisible();
                    nestedNode.setVisible(show);
                    nestedNode.setManaged(show);
                });
            }
            resizeColumns(totalWidth);
        }
    }

    private Node createPlusIcon() {
        Label icon = new Label("+");
        icon.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        return icon;
    }

    private Node createEditIcon() {
        Label icon = new Label("✎");
        icon.setStyle("-fx-font-size: 16;");
        return icon;
    }

    // Column-Hilfsklasse
    private static class Column<T> {
        String title;
        double minWidth;
        double currentWidth;
        Function<T, Node> nodeProvider;
        Function<T, String> sortKeyExtractor;
        Runnable headerAction;
        boolean centerHeader;

        Column(String title, double minWidth, Function<T, Node> nodeProvider, Runnable headerAction,
               Function<T, String> sortKeyExtractor, boolean centerHeader) {
            this.title = title;
            this.minWidth = minWidth;
            this.nodeProvider = nodeProvider;
            this.sortKeyExtractor = sortKeyExtractor;
            this.headerAction = headerAction;
            this.centerHeader = centerHeader;
        }
    }
}