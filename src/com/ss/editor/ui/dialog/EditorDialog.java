package com.ss.editor.ui.dialog;

import static javafx.geometry.Pos.BOTTOM_LEFT;
import static javafx.scene.text.TextAlignment.LEFT;
import com.ss.editor.Editor;
import com.ss.editor.analytics.google.GAEvent;
import com.ss.editor.analytics.google.GAnalytics;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.css.CSSIds;
import com.ss.editor.ui.event.FXEventManager;
import com.ss.editor.ui.event.impl.WindowChangeFocusEvent;
import com.ss.editor.ui.scene.EditorFXScene;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rlib.logging.Logger;
import rlib.logging.LoggerManager;
import rlib.ui.hanlder.WindowDragHandler;
import rlib.ui.util.FXUtils;
import rlib.ui.window.popup.dialog.AbstractPopupDialog;

import java.time.Duration;
import java.time.LocalTime;

/**
 * The base implementation of the {@link AbstractPopupDialog} for using dialogs in the {@link Editor}.
 *
 * @author JavaSaBr
 */
public class EditorDialog extends AbstractPopupDialog {

    @NotNull
    protected static final Logger LOGGER = LoggerManager.getLogger(EditorDialog.class);

    @NotNull
    protected static final FXEventManager FX_EVENT_MANAGER = FXEventManager.getInstance();

    /**
     * The handler for handling changing a focus of the window.
     */
    @NotNull
    private final EventHandler<? super Event> hideEventHandler = event -> {
        final WindowChangeFocusEvent focusEvent = (WindowChangeFocusEvent) event;
        if (!focusEvent.isFocused()) {
            super.hide();
        } else {
            show();
        }
    };

    /**
     * The handler for handling changing a focus of the window from JavaFX.
     */
    @NotNull
    private final ChangeListener<Boolean> hideListener = (observable, oldValue, newValue) -> {
        if (newValue == Boolean.FALSE) {
            super.hide();
        } else {
            show();
        }
    };

    /**
     * The time when this DIALOG was showed.
     */
    @NotNull
    private volatile LocalTime showedTime;

    /**
     * The last focus owner.
     */
    @Nullable
    private Node focusOwner;

    public EditorDialog() {
        this.showedTime = LocalTime.now();
    }

    @Override
    protected void createControls(@NotNull final VBox root) {
        root.setId(CSSIds.EDITOR_DIALOG_BACKGROUND);

        super.createControls(root);

        createHeader(root);

        if (isGridStructure()) {
            final GridPane gridPane = new GridPane();
            createContent(gridPane);
            FXUtils.addToPane(gridPane, root);
        } else {
            createContent(root);
        }

        createActions(root);

        addEventHandler(KeyEvent.KEY_RELEASED, this::processKey);
    }

    protected void processKey(@NotNull final KeyEvent event) {
        event.consume();
        if (event.getCode() == KeyCode.ESCAPE) {
            hide();
        }
    }

    @Override
    public void show(@NotNull final Window owner) {

        final EditorFXScene scene = (EditorFXScene) owner.getScene();
        final StackPane container = scene.getContainer();
        container.setFocusTraversable(false);

        focusOwner = scene.getFocusOwner();

        super.show(owner);

        if (isHideOnLostFocus()) {
            owner.focusedProperty().addListener(hideListener);
            FX_EVENT_MANAGER.addEventHandler(WindowChangeFocusEvent.EVENT_TYPE, hideEventHandler);
        }

        GAnalytics.sendPageView(null, null, "/dialog/" + getDialogId());
        GAnalytics.sendEvent(GAEvent.Category.DIALOG, GAEvent.Action.DIALOG_OPENED, getDialogId());
    }

    @NotNull
    protected String getDialogId() {
        return getClass().getSimpleName();
    }

    @Override
    public void hide() {

        final Duration duration = Duration.between(showedTime, LocalTime.now());
        final int seconds = (int) duration.getSeconds();

        final Window window = getOwnerWindow();
        final EditorFXScene scene = (EditorFXScene) window.getScene();
        final StackPane container = scene.getContainer();
        container.setFocusTraversable(true);

        if (focusOwner != null) {
            focusOwner.requestFocus();
        }

        super.hide();

        if (isHideOnLostFocus()) {
            window.focusedProperty().removeListener(hideListener);
            FX_EVENT_MANAGER.removeEventHandler(WindowChangeFocusEvent.EVENT_TYPE, hideEventHandler);
        }

        GAnalytics.sendEvent(GAEvent.Category.DIALOG, GAEvent.Action.DIALOG_CLOSED, getDialogId());
        GAnalytics.sendTiming(GAEvent.Category.DIALOG, GAEvent.Label.SHOWING_A_DIALOG, seconds, getDialogId());
    }

    /**
     * @return true if dialog will hide after losing a focus.
     */
    protected boolean isHideOnLostFocus() {
        return true;
    }

    /**
     * Create the header of this dialog.
     */
    protected void createHeader(@NotNull final VBox root) {

        final StackPane header = new StackPane();
        header.setId(CSSIds.EDITOR_DIALOG_HEADER);

        final HBox titleContainer = new HBox();
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        titleContainer.setPickOnBounds(false);

        final Label titleLabel = new Label(getTitleText());
        titleLabel.setTextAlignment(LEFT);
        titleLabel.setAlignment(BOTTOM_LEFT);

        final Button closeButton = new Button();
        closeButton.setId(CSSIds.EDITOR_DIALOG_HEADER_BUTTON_CLOSE);
        closeButton.setOnAction(event -> hide());

        FXUtils.addClassTo(titleLabel, CSSClasses.SPECIAL_FONT_16);
        FXUtils.addClassTo(closeButton, CSSClasses.EDITOR_BAR_BUTTON);

        FXUtils.addToPane(titleLabel, titleContainer);
        FXUtils.addToPane(closeButton, header);
        FXUtils.addToPane(titleContainer, header);

        WindowDragHandler.install(header);

        FXUtils.addToPane(header, root);
    }

    /**
     * Create the content of this dialog.
     */
    protected void createContent(@NotNull final VBox root) {
    }

    /**
     * Create the content of this dialog.
     */
    protected void createContent(@NotNull final GridPane root) {
    }

    /**
     * @return true if this dialog has grid structure.
     */
    protected boolean isGridStructure() {
        return false;
    }

    /**
     * Create the actions of this dialog.
     */
    protected void createActions(@NotNull final VBox root) {
    }

    /**
     * @return the title of this dialog.
     */
    @NotNull
    protected String getTitleText() {
        return "Title";
    }
}
