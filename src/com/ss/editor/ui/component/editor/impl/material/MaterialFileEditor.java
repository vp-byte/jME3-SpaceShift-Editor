package com.ss.editor.ui.component.editor.impl.material;

import static com.ss.editor.Messages.MATERIAL_EDITOR_NAME;
import static com.ss.editor.util.MaterialUtils.updateMaterialIdNeed;
import static java.util.Objects.requireNonNull;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.MaterialKey;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.ss.editor.FileExtensions;
import com.ss.editor.Messages;
import com.ss.editor.manager.ResourceManager;
import com.ss.editor.manager.WorkspaceManager;
import com.ss.editor.model.undo.EditorOperation;
import com.ss.editor.model.undo.EditorOperationControl;
import com.ss.editor.model.undo.UndoableEditor;
import com.ss.editor.model.undo.editor.MaterialChangeConsumer;
import com.ss.editor.model.workspace.Workspace;
import com.ss.editor.serializer.MaterialSerializer;
import com.ss.editor.state.editor.impl.material.MaterialEditorAppState;
import com.ss.editor.state.editor.impl.material.MaterialEditorAppState.ModelType;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.component.editor.EditorDescription;
import com.ss.editor.ui.component.editor.impl.AbstractFileEditor;
import com.ss.editor.ui.component.editor.state.impl.MaterialFileEditorState;
import com.ss.editor.ui.component.split.pane.EditorToolSplitPane;
import com.ss.editor.ui.component.tab.ScrollableEditorToolComponent;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.css.CSSIds;
import com.ss.editor.ui.event.impl.FileChangedEvent;
import com.ss.editor.util.EditorUtil;
import com.ss.editor.util.MaterialUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rlib.ui.util.FXUtils;
import rlib.util.array.Array;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * The implementation of the Editor for editing materials.
 *
 * @author JavaSaBr.
 */
public class MaterialFileEditor extends AbstractFileEditor<StackPane> implements UndoableEditor, MaterialChangeConsumer {

    @NotNull
    public static final EditorDescription DESCRIPTION = new EditorDescription();

    static {
        DESCRIPTION.setConstructor(MaterialFileEditor::new);
        DESCRIPTION.setEditorName(MATERIAL_EDITOR_NAME);
        DESCRIPTION.setEditorId(MaterialFileEditor.class.getSimpleName());
        DESCRIPTION.addExtension(FileExtensions.JME_MATERIAL);
    }

    @NotNull
    private static final ResourceManager RESOURCE_MANAGER = ResourceManager.getInstance();

    @NotNull
    private static final RenderQueue.Bucket[] BUCKETS = RenderQueue.Bucket.values();

    @NotNull
    private static final Insets SMALL_OFFSET = new Insets(0, 0, 0, 3);

    @NotNull
    private static final Insets BIG_OFFSET = new Insets(0, 0, 0, 6);

    /**
     * The operation control.
     */
    @NotNull
    private final EditorOperationControl operationControl;

    /**
     * The changes counter.
     */
    @NotNull
    private final AtomicInteger changeCounter;

    /**
     * 3D part of this editor.
     */
    @NotNull
    private final MaterialEditorAppState editorAppState;

    /**
     * The state of this editor.
     */
    @Nullable
    private MaterialFileEditorState editorState;

    /**
     * The textures editor.
     */
    @Nullable
    private MaterialTexturesComponent materialTexturesComponent;

    /**
     * The colors editor.
     */
    @Nullable
    private MaterialColorsComponent materialColorsComponent;

    /**
     * The other parameters editor.
     */
    @Nullable
    private MaterialOtherParamsComponent materialOtherParamsComponent;

    /**
     * The render settings editor.
     */
    @Nullable
    private MaterialRenderParamsComponent materialRenderParamsComponent;

    /**
     * The main split container.
     */
    @Nullable
    private EditorToolSplitPane mainSplitContainer;

    /**
     * Editor tool component.
     */
    @Nullable
    private ScrollableEditorToolComponent editorToolComponent;

    /**
     * The current editing material.
     */
    @Nullable
    private Material currentMaterial;

    /**
     * The button for using a cube.
     */
    @Nullable
    private ToggleButton cubeButton;

    /**
     * The button for using a sphere.
     */
    @Nullable
    private ToggleButton sphereButton;

    /**
     * The button for using a plane.
     */
    @Nullable
    private ToggleButton planeButton;

    /**
     * The button for using a light.
     */
    @Nullable
    private ToggleButton lightButton;

    /**
     * The list of RenderQueue.Bucket.
     */
    @Nullable
    private ComboBox<RenderQueue.Bucket> bucketComboBox;

    /**
     * The list of material definitions.
     */
    @Nullable
    private ComboBox<String> materialDefinitionBox;

    /**
     * The pane of editor area.
     */
    @Nullable
    private Pane editorAreaPane;

    /**
     * The change handler.
     */
    @Nullable
    private Consumer<EditorOperation> changeHandler;

    /**
     * The flag for ignoring listeners.
     */
    private boolean ignoreListeners;

    private MaterialFileEditor() {
        this.editorAppState = new MaterialEditorAppState(this);
        this.operationControl = new EditorOperationControl(this);
        this.changeCounter = new AtomicInteger();
        addEditorState(editorAppState);
    }

    @Override
    public void incrementChange() {
        final int result = changeCounter.incrementAndGet();
        setDirty(result != 0);
    }

    @Override
    public void decrementChange() {
        final int result = changeCounter.decrementAndGet();
        setDirty(result != 0);
    }

    protected void processChangedFile(@NotNull final FileChangedEvent event) {

        final Material currentMaterial = getCurrentMaterial();
        final Path file = event.getFile();

        EXECUTOR_MANAGER.addEditorThreadTask(() -> {

            final Material newMaterial = updateMaterialIdNeed(file, currentMaterial);

            if (newMaterial == null) {
                EXECUTOR_MANAGER.addFXTask(() -> reload(currentMaterial));
            } else {
                EXECUTOR_MANAGER.addFXTask(() -> reload(newMaterial));
            }
        });
    }

    /**
     * @param ignoreListeners the flag for ignoring listeners.
     */
    private void setIgnoreListeners(final boolean ignoreListeners) {
        this.ignoreListeners = ignoreListeners;
    }

    /**
     * @return the flag for ignoring listeners.
     */
    private boolean isIgnoreListeners() {
        return ignoreListeners;
    }

    /**
     * @return the operation control.
     */
    @NotNull
    private EditorOperationControl getOperationControl() {
        return operationControl;
    }

    /**
     * Execute the operation.
     */
    private void handleChanges(final EditorOperation operation) {
        final EditorOperationControl operationControl = getOperationControl();
        operationControl.execute(operation);
    }

    @Override
    public void doSave() {
        super.doSave();

        final Material currentMaterial = getCurrentMaterial();
        final String content = MaterialSerializer.serializeToString(currentMaterial);

        try (final PrintWriter out = new PrintWriter(Files.newOutputStream(getEditFile()))) {
            out.print(content);
        } catch (final IOException e) {
            LOGGER.warning(this, e);
        }

        setDirty(false);
    }

    @NotNull
    @Override
    protected StackPane createRoot() {
        return new StackPane();
    }

    @Override
    protected void processKeyReleased(@NotNull final KeyEvent event) {
        super.processKeyReleased(event);

        if (!event.isControlDown()) return;

        final KeyCode code = event.getCode();

        if (code == KeyCode.Z) {
            undo();
            event.consume();
        } else if (code == KeyCode.Y) {
            redo();
            event.consume();
        }
    }

    /**
     * Redo the last operation.
     */
    public void redo() {
        final EditorOperationControl operationControl = getOperationControl();
        operationControl.redo();
    }

    /**
     * Undo the last operation.
     */
    public void undo() {
        final EditorOperationControl operationControl = getOperationControl();
        operationControl.undo();
    }

    @Override
    protected void createContent(@NotNull final StackPane root) {
        changeHandler = this::handleChanges;
        editorAreaPane = new Pane();

        materialTexturesComponent = new MaterialTexturesComponent(changeHandler);
        materialColorsComponent = new MaterialColorsComponent(changeHandler);
        materialRenderParamsComponent = new MaterialRenderParamsComponent(changeHandler);
        materialOtherParamsComponent = new MaterialOtherParamsComponent(changeHandler);

        mainSplitContainer = new EditorToolSplitPane(JFX_APPLICATION.getScene(), root);
        mainSplitContainer.setId(CSSIds.FILE_EDITOR_MAIN_SPLIT_PANE);

        editorToolComponent = new ScrollableEditorToolComponent(mainSplitContainer, 1);
        editorToolComponent.prefHeightProperty().bind(root.heightProperty());
        editorToolComponent.addComponent(materialTexturesComponent, Messages.MATERIAL_FILE_EDITOR_TEXTURES_COMPONENT_TITLE);
        editorToolComponent.addComponent(materialColorsComponent, Messages.MATERIAL_FILE_EDITOR_COLORS_COMPONENT_TITLE);
        editorToolComponent.addComponent(materialRenderParamsComponent, Messages.MATERIAL_FILE_EDITOR_RENDER_PARAMS_COMPONENT_TITLE);
        editorToolComponent.addComponent(materialOtherParamsComponent, Messages.MATERIAL_FILE_EDITOR_OTHER_COMPONENT_TITLE);
        editorToolComponent.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            final MaterialFileEditorState editorState = getEditorState();
            if (editorState != null) editorState.setOpenedTool(newValue.intValue());
        });

        mainSplitContainer.initFor(editorToolComponent, editorAreaPane);

        FXUtils.addToPane(mainSplitContainer, root);
    }

    /**
     * @return the pane of editor area.
     */
    @NotNull
    private Pane getEditorAreaPane() {
        return requireNonNull(editorAreaPane);
    }

    @Override
    public boolean isInside(final double sceneX, final double sceneY) {
        final Pane editorAreaPane = getEditorAreaPane();
        final Point2D point2D = editorAreaPane.sceneToLocal(sceneX, sceneY);
        return editorAreaPane.contains(point2D);
    }

    /**
     * @return the textures editor.
     */
    @NotNull
    private MaterialTexturesComponent getMaterialTexturesComponent() {
        return requireNonNull(materialTexturesComponent);
    }

    /**
     * @return the colors editor.
     */
    @NotNull
    private MaterialColorsComponent getMaterialColorsComponent() {
        return requireNonNull(materialColorsComponent);
    }

    /**
     * @return the other parameters editor.
     */
    @NotNull
    private MaterialOtherParamsComponent getMaterialOtherParamsComponent() {
        return requireNonNull(materialOtherParamsComponent);
    }

    /**
     * @return the render settings editor.
     */
    @NotNull
    private MaterialRenderParamsComponent getMaterialRenderParamsComponent() {
        return requireNonNull(materialRenderParamsComponent);
    }

    @Override
    public void openFile(@NotNull final Path file) {
        super.openFile(file);

        final Path assetFile = EditorUtil.getAssetFile(file);

        requireNonNull(assetFile, "Asset file can't be null.");

        final MaterialKey materialKey = new MaterialKey(EditorUtil.toAssetPath(assetFile));

        final AssetManager assetManager = EDITOR.getAssetManager();
        final Material material = assetManager.loadAsset(materialKey);

        final MaterialEditorAppState editorState = getEditorAppState();
        editorState.changeMode(ModelType.BOX);

        reload(material);

        EXECUTOR_MANAGER.addFXTask(this::loadState);
    }

    /**
     * @return the state of this editor.
     */
    @Nullable
    private MaterialFileEditorState getEditorState() {
        return editorState;
    }

    @Override
    public void notifyChangedCamera(@NotNull final Vector3f cameraLocation, final float hRotation,
                                    final float vRotation, final float targetDistance) {

        final MaterialFileEditorState editorState = getEditorState();
        if (editorState == null) return;

        editorState.setCameraHRotation(hRotation);
        editorState.setCameraVRotation(vRotation);
        editorState.setCameraTDistance(targetDistance);
        editorState.setCameraLocation(cameraLocation);
    }

    /**
     * Loading a state of this editor.
     */
    @SuppressWarnings("ConstantConditions")
    private void loadState() {

        final WorkspaceManager workspaceManager = WorkspaceManager.getInstance();
        final Workspace currentWorkspace = requireNonNull(workspaceManager.getCurrentWorkspace(),
                "Current workspace can't be null.");

        editorState = currentWorkspace.getEditorState(getEditFile(), MaterialFileEditorState::new);

        switch (ModelType.valueOf(editorState.getModelType())) {
            case BOX:
                cubeButton.setSelected(true);
                break;
            case SPHERE:
                sphereButton.setSelected(true);
                break;
            case QUAD:
                planeButton.setSelected(true);
                break;
        }

        editorToolComponent.getSelectionModel().select(editorState.getOpenedTool());
        bucketComboBox.getSelectionModel().select(editorState.getBucketType());
        mainSplitContainer.updateFor(editorState);
        lightButton.setSelected(editorState.isLightEnable());

        final MaterialEditorAppState editorAppState = getEditorAppState();
        final Vector3f cameraLocation = editorState.getCameraLocation();

        final float hRotation = editorState.getCameraHRotation();
        final float vRotation = editorState.getCameraVRotation();
        final float tDistance = editorState.getCameraTDistance();

        EXECUTOR_MANAGER.addEditorThreadTask(() -> editorAppState.updateCamera(cameraLocation, hRotation, vRotation, tDistance));
    }

    /**
     * Reload the material.
     */
    private void reload(final Material material) {
        setCurrentMaterial(material);

        setIgnoreListeners(true);
        try {

            final MaterialEditorAppState editorState = getEditorAppState();
            editorState.updateMaterial(material);

            final ToggleButton cubeButton = getCubeButton();
            cubeButton.setSelected(true);

            final MaterialTexturesComponent materialTexturesComponent = getMaterialTexturesComponent();
            materialTexturesComponent.buildFor(material);

            final MaterialColorsComponent materialColorsComponent = getMaterialColorsComponent();
            materialColorsComponent.buildFor(material);

            final MaterialOtherParamsComponent materialOtherParamsComponent = getMaterialOtherParamsComponent();
            materialOtherParamsComponent.buildFor(material);

            final MaterialRenderParamsComponent materialRenderParamsComponent = getMaterialRenderParamsComponent();
            materialRenderParamsComponent.buildFor(material);

            final ComboBox<String> materialDefinitionBox = getMaterialDefinitionBox();
            final ObservableList<String> items = materialDefinitionBox.getItems();
            items.clear();

            final Array<String> availableMaterialDefinitions = RESOURCE_MANAGER.getAvailableMaterialDefinitions();
            availableMaterialDefinitions.forEach(items::add);

            final MaterialDef materialDef = material.getMaterialDef();
            materialDefinitionBox.getSelectionModel().select(materialDef.getAssetName());

        } finally {
            setIgnoreListeners(false);
        }
    }

    /**
     * @return the list of material definitions.
     */
    @NotNull
    private ComboBox<String> getMaterialDefinitionBox() {
        return requireNonNull(materialDefinitionBox);
    }

    @Override
    protected boolean needToolbar() {
        return true;
    }

    @Override
    protected void createToolbar(@NotNull final HBox container) {

        cubeButton = new ToggleButton();
        cubeButton.setGraphic(new ImageView(Icons.CUBE_16));
        cubeButton.selectedProperty().addListener((observable, oldValue, newValue) -> changeModelType(ModelType.BOX, newValue));

        sphereButton = new ToggleButton();
        sphereButton.setGraphic(new ImageView(Icons.SPHERE_16));
        sphereButton.selectedProperty().addListener((observable, oldValue, newValue) -> changeModelType(ModelType.SPHERE, newValue));

        planeButton = new ToggleButton();
        planeButton.setGraphic(new ImageView(Icons.PLANE_16));
        planeButton.selectedProperty().addListener((observable, oldValue, newValue) -> changeModelType(ModelType.QUAD, newValue));

        lightButton = new ToggleButton();
        lightButton.setGraphic(new ImageView(Icons.LIGHT_16));
        lightButton.setSelected(true);
        lightButton.selectedProperty().addListener((observable, oldValue, newValue) -> changeLight(newValue));

        final Label materialDefinitionLabel = new Label(Messages.MATERIAL_EDITOR_MATERIAL_TYPE_LABEL + ":");
        materialDefinitionLabel.setId(CSSIds.MATERIAL_FILE_EDITOR_TOOLBAR_LABEL);

        materialDefinitionBox = new ComboBox<>();
        materialDefinitionBox.setId(CSSIds.MATERIAL_FILE_EDITOR_TOOLBAR_BOX);
        materialDefinitionBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> changeType(newValue));

        final Label bucketLabel = new Label(Messages.MATERIAL_FILE_EDITOR_BUCKET_TYPE_LABEL + ":");
        bucketLabel.setId(CSSIds.MATERIAL_FILE_EDITOR_TOOLBAR_LABEL);

        bucketComboBox = new ComboBox<>(FXCollections.observableArrayList(BUCKETS));
        bucketComboBox.setId(CSSIds.MATERIAL_FILE_EDITOR_TOOLBAR_SMALL_BOX);
        bucketComboBox.getSelectionModel().select(RenderQueue.Bucket.Inherit);
        bucketComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> changeBucketType(newValue));

        FXUtils.addToPane(createSaveAction(), container);
        FXUtils.addToPane(cubeButton, container);
        FXUtils.addToPane(sphereButton, container);
        FXUtils.addToPane(planeButton, container);
        FXUtils.addToPane(lightButton, container);
        FXUtils.addToPane(materialDefinitionLabel, container);
        FXUtils.addToPane(materialDefinitionBox, container);
        FXUtils.addToPane(bucketLabel, container);
        FXUtils.addToPane(bucketComboBox, container);

        FXUtils.addClassTo(cubeButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(cubeButton, CSSClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        FXUtils.addClassTo(sphereButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(sphereButton, CSSClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        FXUtils.addClassTo(planeButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(planeButton, CSSClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        FXUtils.addClassTo(lightButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(lightButton, CSSClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        FXUtils.addClassTo(materialDefinitionLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(materialDefinitionBox, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(bucketLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(bucketComboBox, CSSClasses.SPECIAL_FONT_13);

        HBox.setMargin(cubeButton, SMALL_OFFSET);
        HBox.setMargin(sphereButton, SMALL_OFFSET);
        HBox.setMargin(planeButton, SMALL_OFFSET);
        HBox.setMargin(lightButton, BIG_OFFSET);
        HBox.setMargin(materialDefinitionLabel, BIG_OFFSET);
        HBox.setMargin(bucketLabel, BIG_OFFSET);
    }

    /**
     * Handle changing the bucket type.
     */
    private void changeBucketType(final RenderQueue.Bucket newValue) {

        final MaterialEditorAppState editorAppState = getEditorAppState();
        editorAppState.changeBucketType(newValue);

        final MaterialFileEditorState editorState = getEditorState();
        if (editorState != null) editorState.setBucketType(newValue);
    }

    /**
     * Handle changing the type.
     */
    private void changeType(final String newType) {
        if (isIgnoreListeners()) return;
        processChangeTypeImpl(newType);
    }

    /**
     * Handle changing the type.
     */
    private void processChangeTypeImpl(final String newType) {

        final AssetKey<MaterialDef> materialDefKey = new AssetKey<>(newType);

        final AssetManager assetManager = EDITOR.getAssetManager();
        final Material newMaterial = new Material(assetManager, newType);

        MaterialUtils.migrateTo(newMaterial, getCurrentMaterial());

        final EditorOperationControl operationControl = getOperationControl();
        operationControl.clear();

        incrementChange();

        reload(newMaterial);
    }

    /**
     * Handle changing the light enabling.
     */
    private void changeLight(final Boolean newValue) {

        final MaterialEditorAppState editorAppState = getEditorAppState();
        editorAppState.updateLightEnabled(newValue);

        final MaterialFileEditorState editorState = getEditorState();
        if (editorState != null) editorState.setLightEnable(newValue);
    }

    /**
     * @return the button for using a cube.
     */
    @NotNull
    private ToggleButton getCubeButton() {
        return requireNonNull(cubeButton);
    }

    /**
     * @return the button for using a plane.
     */
    @NotNull
    private ToggleButton getPlaneButton() {
        return requireNonNull(planeButton);
    }

    /**
     * @return the button for using a sphere.
     */
    @NotNull
    private ToggleButton getSphereButton() {
        return requireNonNull(sphereButton);
    }

    /**
     * Handle changing model type.
     */
    private void changeModelType(@NotNull final ModelType modelType, final Boolean newValue) {
        if (newValue == Boolean.FALSE) return;

        final MaterialEditorAppState editorAppState = getEditorAppState();

        final ToggleButton cubeButton = getCubeButton();
        final ToggleButton sphereButton = getSphereButton();
        final ToggleButton planeButton = getPlaneButton();

        if (modelType == ModelType.BOX) {
            cubeButton.setMouseTransparent(true);
            sphereButton.setMouseTransparent(false);
            planeButton.setMouseTransparent(false);
            sphereButton.setSelected(false);
            planeButton.setSelected(false);
            editorAppState.changeMode(modelType);
        } else if (modelType == ModelType.SPHERE) {
            cubeButton.setMouseTransparent(false);
            sphereButton.setMouseTransparent(true);
            planeButton.setMouseTransparent(false);
            cubeButton.setSelected(false);
            planeButton.setSelected(false);
            editorAppState.changeMode(modelType);
        } else if (modelType == ModelType.QUAD) {
            cubeButton.setMouseTransparent(false);
            sphereButton.setMouseTransparent(false);
            planeButton.setMouseTransparent(true);
            sphereButton.setSelected(false);
            cubeButton.setSelected(false);
            editorAppState.changeMode(modelType);
        }

        final MaterialFileEditorState editorState = getEditorState();
        if (editorState != null) editorState.setModelType(modelType);
    }

    @NotNull
    @Override
    public Material getCurrentMaterial() {
        return requireNonNull(currentMaterial);
    }

    @Override
    public void notifyChangeParam(@NotNull final String paramName) {

        final MaterialOtherParamsComponent otherParamsComponent = getMaterialOtherParamsComponent();
        otherParamsComponent.updateParam(paramName);

        final MaterialColorsComponent colorsComponent = getMaterialColorsComponent();
        colorsComponent.updateParam(paramName);

        final MaterialTexturesComponent texturesComponent = getMaterialTexturesComponent();
        texturesComponent.updateParam(paramName);
    }

    @Override
    public void notifyChangedRenderState() {
        final MaterialRenderParamsComponent renderParamsComponent = getMaterialRenderParamsComponent();
        renderParamsComponent.buildFor(getCurrentMaterial());
    }

    /**
     * @param currentMaterial the current editing material.
     */
    private void setCurrentMaterial(@NotNull final Material currentMaterial) {
        this.currentMaterial = currentMaterial;
    }

    /**
     * @return 3D part of this editor.
     */
    @NotNull
    private MaterialEditorAppState getEditorAppState() {
        return editorAppState;
    }

    @NotNull
    @Override
    public EditorDescription getDescription() {
        return DESCRIPTION;
    }
}
