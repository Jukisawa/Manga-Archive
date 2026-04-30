package com.jukisawa.mangaarchive.ui.components;

import javafx.beans.value.ChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Eine generische Tabelle mit Filter und Nested Tables.
 */
public class Table<T> extends VBox {

    private final VBox tableBody = new VBox();
    private final HBox header;
    private final List<Column<T>> columns = new ArrayList<>();
    private SortedList<T> items;
    private Function<T, Node> nestedTableProvider;
    private HoverPopup<T> hoverPopup;
    private static final double SCROLLBAR_WIDTH = 12.0;

    private boolean ascending = true;
    private Column<T> lastSortedColumn = null;

    private double totalWidth = 0;

    private final javafx.collections.ListChangeListener<T> listChangeListener = _ -> refresh();

    private Function<T, String> rowStyleClassProvider;

    public void setRowStyleClassProvider(Function<T, String> provider) {
        this.rowStyleClassProvider = provider;
    }

    public Table() {
        setSpacing(5);
        setPadding(new Insets(10));
        HBox filterBox = new HBox(5);
        getChildren().add(filterBox);

        // Tabellenstruktur
        header = new HBox();
        Region scrollbarSpacer = new Region();
        scrollbarSpacer.setMinWidth(SCROLLBAR_WIDTH);
        scrollbarSpacer.setPrefWidth(SCROLLBAR_WIDTH);
        header.getChildren().add(scrollbarSpacer);
        getChildren().add(header);
        //getChildren().add(tableBody);

        ScrollPane tableBodyScrollPane = new ScrollPane();
        tableBodyScrollPane.setContent(tableBody);
        tableBodyScrollPane.setFitToWidth(true); // Wichtig, damit die Spaltenbreiten passen
        tableBodyScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Keine horizontale Scrollbar
        // Optional: Damit die Tabelle den verfügbaren Platz im FXML ausfüllt
        VBox.setVgrow(tableBodyScrollPane, Priority.ALWAYS);

        getChildren().add(tableBodyScrollPane);

        // Spalten-Listener
        ChangeListener<Number> widthListener = (_, _, newW) -> resizeColumns(newW.doubleValue());
        widthProperty().addListener(widthListener);

        // CSS Types
        tableBody.getStyleClass().add("table");
        header.getStyleClass().add("table-header");

    }

    public void addStringColumn(String title, int minWidth, Function<T, String> valueProvider, boolean centerContent,
                                boolean centerHeader) {
        addStringColumn(title, minWidth, valueProvider, centerContent, centerHeader, null);
    }

    public void addStringColumn(String title, int minWidth, Function<T, String> valueProvider, boolean centerContent,
                                boolean centerHeader, Consumer<T> onClick) {
        addStringColumn(
                title,
                minWidth,
                valueProvider,
                t -> {
                    if (t == null) return "";
                    String val = valueProvider.apply(t);
                    return val != null ? val.toLowerCase() : "";
                },
                centerContent,
                centerHeader, onClick
        );
    }

    public void addStringColumn(String title, int width, Function<T, String> valueProvider,
                                Function<T, String> sortKeyExtractor, boolean centerContent, boolean centerHeader, Consumer<T> onClick) {
        addNodeColumn(title, width, t -> {
            Label label = new Label(valueProvider.apply(t));
            label.setPrefWidth(width);
            if (centerContent) {
                label.setAlignment(Pos.CENTER);
                label.setMaxWidth(Double.MAX_VALUE);
            }
            return label;
        }, sortKeyExtractor, centerContent, centerHeader, onClick);
    }

    public void addActionColumn(Runnable onAdd, Consumer<T> onEdit, Consumer<T> onDelete, Runnable onShiftAdd) {
        columns.add(new Column<>("Aktion", 180, t -> {
            HBox actions = new HBox(5);
            actions.setAlignment(Pos.CENTER);

            actions.setMaxHeight(20);
            actions.setMinHeight(20);
            actions.setPadding(new Insets(0, 5, 0, 5));

            Label expandIcon = new Label("▶");
            expandIcon.setStyle("-fx-font-size: 11;");
            Button expandBtn = createIconButton(expandIcon, null);
            expandBtn.setUserData("expand-button");

            Button editBtn = createIconButton(createEditIcon(), _ -> onEdit.accept(t));

            Button deleteBtn = createIconButton(createDeleteIcon(), _ -> onDelete.accept(t));

            actions.getChildren().addAll(expandBtn, editBtn, deleteBtn);
            return actions;
        }, onAdd, null, true, onShiftAdd, null));

        updateHeader();
        refresh();
    }

    public void addNodeColumn(String title, int width, Function<T, Node> nodeProvider, Function<T, String> sortKey,
                              boolean centerContent, boolean centerHeader, Consumer<T> onClick) {
        columns.add(new Column<>(title, width, nodeProvider, null, sortKey, centerHeader, null, onClick));
        updateHeader();
        refresh();
    }

    public void setItems(SortedList<T> items) {
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

        double effectiveWidth = totalWidth - SCROLLBAR_WIDTH;
        double minWidthSum = columns.stream().mapToDouble(c -> c.minWidth).sum();
        double extraSpace = Math.max(0, effectiveWidth - minWidthSum - 40); // 40 ist das Padding
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

    public void setHoverPopup(HoverPopup<T> hoverPopup) {
        this.hoverPopup = hoverPopup;
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

        items.setComparator(comparator);
        refresh();
    }

    private void updateHeader() {
        Node scrollbarSpacer = header.getChildren().isEmpty() ? null : header.getChildren().getLast();
        header.getChildren().clear();
        for (Column<T> col : columns) {
            if (col.headerAction != null) {
                Button addBtn = new Button();
                addBtn.setGraphic(createPlusIcon());
                addBtn.setOnMouseClicked(event -> {
                    if (event.isShiftDown() && col.headerShiftAction != null) {
                        col.headerShiftAction.run();
                    } else if (col.headerAction != null) {
                        col.headerAction.run();
                    }
                });
                addBtn.setPrefWidth(col.currentWidth);
                addBtn.setMinWidth(col.minWidth);
                addBtn.setStyle("-fx-background-color: transparent;");
                header.getChildren().add(addBtn);
                if (col.centerHeader) {
                    addBtn.setAlignment(Pos.CENTER);
                    addBtn.setMaxWidth(Double.MAX_VALUE);
                }
            } else {
                Label label = getLabel(col);
                header.getChildren().add(label);
            }
        }
        if (scrollbarSpacer instanceof Region spacer) {
            header.getChildren().add(spacer);
        }
    }

    private Label getLabel(Column<T> col) {
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
        return label;
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

    public void refresh() {
        tableBody.getChildren().clear();
        if (items == null) return;

        for (T t : items) {
            HBox row = new HBox();
            row.getStyleClass().add("table-row");

            // Dynamische CSS-Klasse pro Row
            if (rowStyleClassProvider != null) {
                String extraClass = rowStyleClassProvider.apply(t);
                if (extraClass != null && !extraClass.isBlank()) {
                    row.getStyleClass().add(extraClass);
                }
            }

            // Nested Table vorbereiten
            Node nestedNode = nestedTableProvider != null ? nestedTableProvider.apply(t) : null;
            if (nestedNode != null) {
                nestedNode.setVisible(false);
                nestedNode.setManaged(false);
            }

            for (Column<T> col : columns) {
                Node cell = col.nodeProvider.apply(t);
                cell.getStyleClass().add("table-cell");

                // Logik: Expand-Button in der Action-HBox funktionsfähig machen
                if (col.title.equals("Aktion") && cell instanceof HBox actionBox) {
                    actionBox.getChildren().stream()
                            .filter(n -> "expand-button".equals(n.getUserData()))
                            .findFirst()
                            .ifPresent(n -> {
                                Button btn = (Button) n;

                                // WICHTIG: Wenn keine Nested Table da ist, Button verstecken
                                if (nestedNode == null) {
                                    btn.setVisible(false);
                                    btn.setManaged(false);
                                } else {
                                    // Ansonsten Logik zuweisen
                                    btn.setVisible(true);
                                    btn.setManaged(true);
                                    btn.setOnAction(_ -> {
                                        boolean isVisible = !nestedNode.isVisible();
                                        nestedNode.setVisible(isVisible);
                                        nestedNode.setManaged(isVisible);
                                        ((Label) btn.getGraphic()).setText(isVisible ? "▼" : "▶");
                                    });
                                }
                            });
                }

                row.getChildren().add(cell);

                if (col.onClick != null) {
                    cell.setOnMouseClicked(_ -> col.onClick.accept(t));
                }

                // Hover logic
                if (col.headerAction == null && hoverPopup != null) {
                    cell.setOnMouseEntered(_ -> hoverPopup.showFor(t, row));
                    cell.setOnMouseExited(_ -> hoverPopup.hide());
                }
            }

            row.setAlignment(Pos.CENTER_LEFT);
            tableBody.getChildren().add(row);
            if (nestedNode != null) {
                tableBody.getChildren().add(nestedNode);
            }
        }
        resizeColumns(totalWidth);
    }

    private Button createIconButton(Node icon, Consumer<ActionEvent> action) {
        Button btn = new Button();
        btn.setGraphic(icon);
        if (action != null) {
            btn.setOnAction(action::accept);
        }

        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2 8 2 8;");

        btn.setMinHeight(30);
        btn.setMaxHeight(30);
        return btn;
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

    private Node createDeleteIcon() {
        Label icon = new Label("🗑"); // Trash bin icon
        icon.setStyle("-fx-font-size: 16; -fx-text-fill: #d9534f;"); // Optional: Make it red
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
        Runnable headerShiftAction;
        boolean centerHeader;
        Consumer<T> onClick;

        Column(String title, double minWidth, Function<T, Node> nodeProvider, Runnable headerAction,
               Function<T, String> sortKeyExtractor, boolean centerHeader, Runnable headerShiftAction, Consumer<T> onClick) {
            this.title = title;
            this.minWidth = minWidth;
            this.nodeProvider = nodeProvider;
            this.sortKeyExtractor = sortKeyExtractor;
            this.headerAction = headerAction;
            this.centerHeader = centerHeader;
            this.headerShiftAction = headerShiftAction;
            this.onClick = onClick;
        }
    }
}