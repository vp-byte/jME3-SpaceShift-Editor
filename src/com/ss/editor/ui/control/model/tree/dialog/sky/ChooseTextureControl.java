package com.ss.editor.ui.control.model.tree.dialog.sky;

import static com.ss.editor.util.EditorUtil.getAssetFile;
import static java.util.Objects.requireNonNull;
import com.ss.editor.FileExtensions;
import com.ss.editor.JFXApplication;
import com.ss.editor.Messages;
import com.ss.editor.manager.JavaFXImageManager;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.component.asset.tree.context.menu.action.DeleteFileAction;
import com.ss.editor.ui.component.asset.tree.context.menu.action.NewFileAction;
import com.ss.editor.ui.component.asset.tree.context.menu.action.RenameFileAction;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.css.CSSIds;
import com.ss.editor.ui.dialog.asset.AssetEditorDialog;
import com.ss.editor.ui.dialog.asset.FileAssetEditorDialog;
import com.ss.editor.ui.scene.EditorFXScene;
import com.ss.editor.ui.tooltip.ImageChannelPreview;
import com.ss.editor.ui.util.UIUtils;
import com.ss.editor.util.EditorUtil;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rlib.ui.util.FXUtils;
import rlib.util.array.Array;
import rlib.util.array.ArrayFactory;

/**
 * The control for choosing textures.
 *
 * @author JavaSaBr.
 */
public class ChooseTextureControl extends HBox {

    @NotNull
    private static final Insets ELEMENT_OFFSET = new Insets(0, 0, 0, 3);

    @NotNull
    private static final Predicate<Class<?>> ACTION_TESTER = type -> type == NewFileAction.class ||
            type == DeleteFileAction.class ||
            type == RenameFileAction.class;

    @NotNull
    private static final Array<String> TEXTURE_EXTENSIONS = ArrayFactory.newArray(String.class);

    static {
        TEXTURE_EXTENSIONS.add(FileExtensions.IMAGE_PNG);
        TEXTURE_EXTENSIONS.add(FileExtensions.IMAGE_JPG);
        TEXTURE_EXTENSIONS.add(FileExtensions.IMAGE_JPEG);
        TEXTURE_EXTENSIONS.add(FileExtensions.IMAGE_TGA);
        TEXTURE_EXTENSIONS.add(FileExtensions.IMAGE_DDS);
        TEXTURE_EXTENSIONS.add(FileExtensions.IMAGE_HDR);
    }

    @NotNull
    private static final JavaFXImageManager IMAGE_MANAGER = JavaFXImageManager.getInstance();

    /**
     * The image channels preview.
     */
    @Nullable
    private ImageChannelPreview textureTooltip;

    /**
     * The image preview.
     */
    @Nullable
    private ImageView texturePreview;

    /**
     * The label for the path of texture.
     */
    @Nullable
    private Label textureLabel;

    /**
     * The selected file.
     */
    @Nullable
    private Path textureFile;

    /**
     * The handler.
     */
    @Nullable
    private Runnable changeHandler;

    ChooseTextureControl() {
        setAlignment(Pos.CENTER_LEFT);
        createComponents();
    }

    /**
     * @param changeHandler the handler.
     */
    public void setChangeHandler(@Nullable final Runnable changeHandler) {
        this.changeHandler = changeHandler;
    }

    /**
     * tThe handler.
     */
    @Nullable
    private Runnable getChangeHandler() {
        return changeHandler;
    }

    protected void createComponents() {

        textureLabel = new Label();
        textureLabel.setId(CSSIds.CHOOSE_TEXTURE_CONTROL_TEXTURE_LABEL);

        textureTooltip = new ImageChannelPreview();

        final VBox previewContainer = new VBox();
        previewContainer.setId(CSSIds.CHOOSE_TEXTURE_CONTROL_PREVIEW);

        texturePreview = new ImageView();
        texturePreview.fitHeightProperty().bind(previewContainer.heightProperty().subtract(2));
        texturePreview.fitWidthProperty().bind(previewContainer.widthProperty().subtract(2));

        Tooltip.install(texturePreview, textureTooltip);

        final Button addButton = new Button();
        addButton.setId(CSSIds.CREATE_SKY_DIALOG_BUTTON);
        addButton.setGraphic(new ImageView(Icons.ADD_18));
        addButton.setOnAction(event -> processAdd());

        final Button removeButton = new Button();
        removeButton.setId(CSSIds.CREATE_SKY_DIALOG_BUTTON);
        removeButton.setGraphic(new ImageView(Icons.REMOVE_18));
        removeButton.setOnAction(event -> processRemove());

        FXUtils.addToPane(textureLabel, this);
        FXUtils.addToPane(texturePreview, previewContainer);
        FXUtils.addToPane(previewContainer, this);
        FXUtils.addToPane(addButton, this);
        FXUtils.addToPane(removeButton, this);

        FXUtils.addClassTo(textureLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(addButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(removeButton, CSSClasses.TOOLBAR_BUTTON);

        HBox.setMargin(previewContainer, ELEMENT_OFFSET);
        HBox.setMargin(addButton, ELEMENT_OFFSET);
        HBox.setMargin(removeButton, ELEMENT_OFFSET);

        removeButton.disableProperty().bind(texturePreview.imageProperty().isNull());
    }

    /**
     * @return the label for the path of texture.
     */
    @NotNull
    private Label getTextureLabel() {
        return requireNonNull(textureLabel);
    }

    /**
     * @return The image channels preview.
     */
    @NotNull
    private ImageChannelPreview getTextureTooltip() {
        return requireNonNull(textureTooltip);
    }

    /**
     * Add new texture.
     */
    private void processAdd() {
        UIUtils.openAssetDialog(this::setTextureFile, TEXTURE_EXTENSIONS, ACTION_TESTER);
    }

    /**
     * @return the selected file.
     */
    @Nullable
    Path getTextureFile() {
        return textureFile;
    }

    /**
     * @param textureFile the selected file.
     */
    private void setTextureFile(@Nullable final Path textureFile) {
        this.textureFile = textureFile;

        reload();

        final Runnable changeHandler = getChangeHandler();
        if (changeHandler != null) changeHandler.run();
    }

    /**
     * Remove the texture.
     */
    private void processRemove() {
        setTextureFile(null);
    }

    /**
     * @return the image preview.
     */
    @NotNull
    private ImageView getTexturePreview() {
        return requireNonNull(texturePreview);
    }

    protected void reload() {

        final ImageChannelPreview textureTooltip = getTextureTooltip();
        final Label textureLabel = getTextureLabel();
        final ImageView preview = getTexturePreview();

        final Path textureFile = getTextureFile();

        if (textureFile == null) {
            textureLabel.setText(Messages.MATERIAL_MODEL_PROPERTY_CONTROL_NO_TEXTURE);
            preview.setImage(null);
            textureTooltip.showImage(null);
            return;
        }

        final Path assetFile = requireNonNull(getAssetFile(textureFile));

        textureLabel.setText(assetFile.toString());
        preview.setImage(IMAGE_MANAGER.getTexturePreview(textureFile, 28, 28));
        textureTooltip.showImage(textureFile);
    }
}
