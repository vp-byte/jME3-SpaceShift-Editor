package com.ss.editor.ui.component.editing.terrain;

import static java.util.Objects.requireNonNull;
import static rlib.util.array.ArrayFactory.toArray;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.component.editing.impl.AbstractEditingComponent;
import com.ss.editor.ui.component.editing.terrain.control.*;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.css.CSSIds;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rlib.ui.control.input.FloatTextField;
import rlib.ui.util.FXUtils;
import rlib.util.StringUtils;
import rlib.util.array.Array;
import rlib.util.array.ArrayFactory;
import rlib.util.dictionary.DictionaryFactory;
import rlib.util.dictionary.ObjectDictionary;

/**
 * The implementation of a terrain editor.
 *
 * @author JavaSaBr
 */
public class TerrainEditingComponent extends AbstractEditingComponent<TerrainQuad> {

    public static final double LABEL_PERCENT = 0.6;
    public static final double FIELD_PERCENT = 0.4;

    /**
     * The list of all tool controls.
     */
    @NotNull
    private final Array<TerrainToolControl> toolControls;

    /**
     * The list of all toggle buttons.
     */
    @NotNull
    private final Array<ToggleButton> toggleButtons;

    /**
     * The map with mapping toggle button to terrain control.
     */
    @NotNull
    private final ObjectDictionary<ToggleButton, TerrainToolControl> buttonToControl;

    /**
     * The map with mapping toggle button to its settings.
     */
    @NotNull
    private final ObjectDictionary<ToggleButton, Pane> buttonToSettings;

    /**
     * The control to raise/lowe terrain.
     */
    @NotNull
    private final RaiseLowerTerrainToolControl raiseLowerToolControl;

    /**
     * The control to smooth terrain.
     */
    @NotNull
    private final SmoothTerrainToolControl smoothToolControl;

    /**
     * The control to make rough surface terrain.
     */
    @NotNull
    private final RoughTerrainToolControl roughToolControl;

    /**
     * The control to make some levels terrain.
     */
    @NotNull
    private final LevelTerrainToolControl levelToolControl;

    /**
     * The control to make slopes on terrain.
     */
    @NotNull
    private final SlopeTerrainToolControl slopeToolControl;

    /**
     * The control to paint on terrain.
     */
    @NotNull
    private final PaintTerrainToolControl paintToolControl;

    /**
     * The button to raise/lower terrain.
     */
    @Nullable
    private ToggleButton raiseLowerButton;

    /**
     * The button to smooth terrain.
     */
    @Nullable
    private ToggleButton smoothButton;

    /**
     * The button to make rough terrain.
     */
    @Nullable
    private ToggleButton roughButton;

    /**
     * The button to change height of terrain by level.
     */
    @Nullable
    private ToggleButton levelButton;

    /**
     * The button to make slopes on terrain.
     */
    @Nullable
    private ToggleButton slopeButton;

    /**
     * The button to paint on terrain.
     */
    @Nullable
    private ToggleButton paintButton;

    /**
     * The brush size field.
     */
    @Nullable
    private FloatTextField brushSizeField;

    /**
     * The brush power field.
     */
    @Nullable
    private FloatTextField brushPowerField;

    /**
     * The container of control settings.
     */
    @Nullable
    private VBox controlSettings;

    /**
     * The settings of level control.
     */
    @Nullable
    private GridPane levelControlSettings;

    /**
     * The setting of using smoothly changing of terrain height.
     */
    @Nullable
    private CheckBox levelControlSmoothly;

    /**
     * The setting of using marker to detect a level.
     */
    @Nullable
    private CheckBox levelControlUseMarker;

    /**
     * The setting of target level.
     */
    @Nullable
    private FloatTextField levelControlLevelField;

    /**
     * The settings of rough control.
     */
    @Nullable
    private GridPane roughControlSettings;

    /**
     * The settings of roughness.
     */
    @Nullable
    private FloatTextField roughControlRoughnessField;

    /**
     * The settings of frequency.
     */
    @Nullable
    private FloatTextField roughControlFrequencyField;

    /**
     * The settings of lacunarity.
     */
    @Nullable
    private FloatTextField roughControlLacunarityField;

    /**
     * The settings of octaves.
     */
    @Nullable
    private FloatTextField roughControlOctavesField;

    /**
     * The settings of scale.
     */
    @Nullable
    private FloatTextField roughControlScaleField;

    /**
     * The settings of slope control.
     */
    @Nullable
    private GridPane slopeControlSettings;

    /**
     * The setting of using smoothly changing of terrain height.
     */
    @Nullable
    private CheckBox slopeControlSmoothly;

    /**
     * The setting of using limited between markers.
     */
    @Nullable
    private CheckBox slopeControlLimited;

    @Nullable
    private VBox paintControlSettings;

    /**
     * The current tool control.
     */
    @Nullable
    private TerrainToolControl toolControl;

    public TerrainEditingComponent() {
        this.buttonToControl = DictionaryFactory.newObjectDictionary();
        this.buttonToSettings = DictionaryFactory.newObjectDictionary();
        this.raiseLowerToolControl = new RaiseLowerTerrainToolControl(this);
        this.smoothToolControl = new SmoothTerrainToolControl(this);
        this.roughToolControl = new RoughTerrainToolControl(this);
        this.levelToolControl = new LevelTerrainToolControl(this);
        this.slopeToolControl = new SlopeTerrainToolControl(this);
        this.paintToolControl = new PaintTerrainToolControl(this);
        this.toolControls = ArrayFactory.newArray(TerrainToolControl.class);
        this.toggleButtons = ArrayFactory.newArray(ToggleButton.class);
        this.toolControls.addAll(toArray(raiseLowerToolControl, smoothToolControl, roughToolControl, levelToolControl, slopeToolControl, paintToolControl));
        this.toggleButtons.addAll(toArray(raiseLowerButton, smoothButton, roughButton, levelButton, slopeButton, paintButton));

        final ToggleButton raiseLowerButton = getRaiseLowerButton();

        buttonToControl.put(raiseLowerButton, raiseLowerToolControl);
        buttonToControl.put(getSmoothButton(), smoothToolControl);
        buttonToControl.put(getRoughButton(), roughToolControl);
        buttonToControl.put(getLevelButton(), levelToolControl);
        buttonToControl.put(getSlopeButton(), slopeToolControl);
        buttonToControl.put(getPaintButton(), paintToolControl);
        buttonToSettings.put(getSlopeButton(), slopeControlSettings);
        buttonToSettings.put(getLevelButton(), levelControlSettings);
        buttonToSettings.put(getRoughButton(), roughControlSettings);
        buttonToSettings.put(getPaintButton(), paintControlSettings);

        getLevelControlLevelField().setValue(1);
        getLevelControlUseMarker().setSelected(false);
        getLevelControlSmoothly().setSelected(true);
        getSlopeControlLimited().setSelected(true);
        getSlopeControlSmoothly().setSelected(true);
        getRoughControlFrequencyField().setValue(0.2f);
        getRoughControlLacunarityField().setValue(2.12f);
        getRoughControlOctavesField().setValue(8);
        getRoughControlRoughnessField().setValue(1.2f);
        getRoughControlScaleField().setValue(1.0f);

        raiseLowerButton.setSelected(true);

        getBrushSizeField().setValue(1);
        getBrushPowerField().setValue(1);

        setToolControl(raiseLowerToolControl);
    }

    /**
     * @return the list of all tool controls.
     */
    @NotNull
    private Array<TerrainToolControl> getToolControls() {
        return toolControls;
    }

    /**
     * @return the list of all toggle buttons.
     */
    @NotNull
    private Array<ToggleButton> getToggleButtons() {
        return toggleButtons;
    }

    /**
     * @return the map with mapping toggle button to terrain control.
     */
    @NotNull
    private ObjectDictionary<ToggleButton, TerrainToolControl> getButtonToControl() {
        return buttonToControl;
    }

    /**
     * @return the map with mapping toggle button to its settings.
     */
    @NotNull
    private ObjectDictionary<ToggleButton, Pane> getButtonToSettings() {
        return buttonToSettings;
    }

    /**
     * @return the current tool control.
     */
    @Nullable
    private TerrainToolControl getToolControl() {
        return toolControl;
    }

    /**
     * @param toolControl the current tool control.
     */
    private void setToolControl(@Nullable final TerrainToolControl toolControl) {
        this.toolControl = toolControl;
    }

    /**
     * @return the container of control settings.
     */
    @NotNull
    private VBox getControlSettings() {
        return requireNonNull(controlSettings);
    }

    /**
     * @return the button to change height of terrain by level.
     */
    @NotNull
    private ToggleButton getLevelButton() {
        return requireNonNull(levelButton);
    }

    /**
     * @return the button to paint on terrain.
     */
    @NotNull
    private ToggleButton getPaintButton() {
        return requireNonNull(paintButton);
    }

    /**
     * @return the button to make slopes on terrain.
     */
    @NotNull
    private ToggleButton getSlopeButton() {
        return requireNonNull(slopeButton);
    }

    /**
     * @return the button to make rough terrain.
     */
    @NotNull
    private ToggleButton getRoughButton() {
        return requireNonNull(roughButton);
    }

    /**
     * @return the button to smooth terrain.
     */
    @NotNull
    private ToggleButton getSmoothButton() {
        return requireNonNull(smoothButton);
    }

    /**
     * @return The button to raise/lower terrain.
     */
    @NotNull
    private ToggleButton getRaiseLowerButton() {
        return requireNonNull(raiseLowerButton);
    }

    /**
     * @return the brush power field.
     */
    @NotNull
    private FloatTextField getBrushPowerField() {
        return requireNonNull(brushPowerField);
    }

    /**
     * @return the brush size field.
     */
    @NotNull
    private FloatTextField getBrushSizeField() {
        return requireNonNull(brushSizeField);
    }

    @Override
    protected void createComponents() {
        super.createComponents();

        raiseLowerButton = new ToggleButton(StringUtils.EMPTY, new ImageView(Icons.TERRAIN_UP_32));
        raiseLowerButton.setOnAction(this::switchMode);

        smoothButton = new ToggleButton(StringUtils.EMPTY, new ImageView(Icons.TERRAIN_SMOOTH_32));
        smoothButton.setOnAction(this::switchMode);

        roughButton = new ToggleButton(StringUtils.EMPTY, new ImageView(Icons.TERRAIN_ROUGH_32));
        roughButton.setOnAction(this::switchMode);

        levelButton = new ToggleButton(StringUtils.EMPTY, new ImageView(Icons.TERRAIN_LEVEL_32));
        levelButton.setOnAction(this::switchMode);

        slopeButton = new ToggleButton(StringUtils.EMPTY, new ImageView(Icons.TERRAIN_SLOPE_32));
        slopeButton.setOnAction(this::switchMode);

        paintButton = new ToggleButton(StringUtils.EMPTY, new ImageView(Icons.TERRAIN_PAINT_32));
        paintButton.setOnAction(this::switchMode);

        final GridPane buttonsContainer = new GridPane();
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setPadding(new Insets(2, 4, 2, 4));
        buttonsContainer.add(raiseLowerButton, 0, 0);
        buttonsContainer.add(smoothButton, 1, 0);
        buttonsContainer.add(roughButton, 2, 0);
        buttonsContainer.add(levelButton, 3, 0);
        buttonsContainer.add(slopeButton, 4, 0);
        buttonsContainer.add(paintButton, 5, 0);
        buttonsContainer.prefWidthProperty().bind(widthProperty());

        final Label brushSizeLabel = new Label("Brush size:");
        brushSizeLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        brushSizeLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        brushSizeField = new FloatTextField();
        brushSizeField.setId(CSSIds.ABSTRACT_PARAM_CONTROL_COMBO_BOX);
        brushSizeField.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        brushSizeField.setMinMax(0.0001F, Integer.MAX_VALUE);
        brushSizeField.addChangeListener((observable, oldValue, newValue) -> changeBrushSize(newValue));

        final Label brushPowerLabel = new Label("Brush power:");
        brushPowerLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        brushPowerLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        brushPowerField = new FloatTextField();
        brushPowerField.setId(CSSIds.ABSTRACT_PARAM_CONTROL_COMBO_BOX);
        brushPowerField.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        brushPowerField.setMinMax(0.0001F, Integer.MAX_VALUE);
        brushPowerField.addChangeListener((observable, oldValue, newValue) -> changeBrushPower(newValue));

        final GridPane brushSettingsContainer = new GridPane();
        brushSettingsContainer.add(brushSizeLabel, 0, 0);
        brushSettingsContainer.add(brushSizeField, 1, 0);
        brushSettingsContainer.add(brushPowerLabel, 0, 1);
        brushSettingsContainer.add(brushPowerField, 1, 1);

        controlSettings = new VBox();
        controlSettings.prefWidthProperty().bind(widthProperty());

        FXUtils.addToPane(buttonsContainer, this);
        FXUtils.addToPane(brushSettingsContainer, this);
        FXUtils.addToPane(controlSettings, this);

        createLevelControlSettings();
        createSlopeControlSettings();
        createRoughControlSettings();

        paintControlSettings = new VBox();

        FXUtils.addClassTo(raiseLowerButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(raiseLowerButton, CSSClasses.EDITING_TOGGLE_BUTTON_BIG);
        FXUtils.addClassTo(smoothButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(smoothButton, CSSClasses.EDITING_TOGGLE_BUTTON_BIG);
        FXUtils.addClassTo(roughButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(roughButton, CSSClasses.EDITING_TOGGLE_BUTTON_BIG);
        FXUtils.addClassTo(levelButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(levelButton, CSSClasses.EDITING_TOGGLE_BUTTON_BIG);
        FXUtils.addClassTo(slopeButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(slopeButton, CSSClasses.EDITING_TOGGLE_BUTTON_BIG);
        FXUtils.addClassTo(paintButton, CSSClasses.TOOLBAR_BUTTON);
        FXUtils.addClassTo(paintButton, CSSClasses.EDITING_TOGGLE_BUTTON_BIG);

        FXUtils.addClassTo(brushSizeLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(brushSizeField, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(brushPowerLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(brushPowerField, CSSClasses.SPECIAL_FONT_13);
    }

    /**
     * Create settings of slope control.
     */
    private void createSlopeControlSettings() {

        final Label smoothlyLabel = new Label("Smoothly:");
        smoothlyLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        smoothlyLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        slopeControlSmoothly = new CheckBox();
        slopeControlSmoothly.setId(CSSIds.ABSTRACT_PARAM_CONTROL_CHECK_BOX);
        slopeControlSmoothly.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        slopeControlSmoothly.selectedProperty().addListener((observable, oldValue, newValue) ->
                changeSlopeControlSmoothly(newValue));

        final Label limitedLabel = new Label("Limited:");
        limitedLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        limitedLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        slopeControlLimited = new CheckBox();
        slopeControlLimited.setId(CSSIds.ABSTRACT_PARAM_CONTROL_CHECK_BOX);
        slopeControlLimited.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        slopeControlLimited.selectedProperty().addListener((observable, oldValue, newValue) ->
                changeSlopeControlLimited(newValue));

        slopeControlSettings = new GridPane();
        slopeControlSettings.add(smoothlyLabel, 0, 0);
        slopeControlSettings.add(slopeControlSmoothly, 1, 0);
        slopeControlSettings.add(limitedLabel, 0, 1);
        slopeControlSettings.add(slopeControlLimited, 1, 1);

        FXUtils.addClassTo(smoothlyLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(slopeControlSmoothly, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(limitedLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(slopeControlLimited, CSSClasses.SPECIAL_FONT_13);
    }

    /**
     * Create settings of level control.
     */
    private void createLevelControlSettings() {

        final Label smoothlyLabel = new Label("Smoothly:");
        smoothlyLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        smoothlyLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        levelControlSmoothly = new CheckBox();
        levelControlSmoothly.setId(CSSIds.ABSTRACT_PARAM_CONTROL_CHECK_BOX);
        levelControlSmoothly.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        levelControlSmoothly.selectedProperty().addListener((observable, oldValue, newValue) ->
                changeLevelControlSmoothly(newValue));

        final Label useMarkerLabel = new Label("Use marker:");
        useMarkerLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        useMarkerLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        levelControlUseMarker = new CheckBox();
        levelControlUseMarker.setId(CSSIds.ABSTRACT_PARAM_CONTROL_CHECK_BOX);
        levelControlUseMarker.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        levelControlUseMarker.selectedProperty().addListener((observable, oldValue, newValue) ->
                changeLevelControlUseMarker(newValue));

        final Label levelLabel = new Label("Level:");
        levelLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        levelLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        levelControlLevelField = new FloatTextField();
        levelControlLevelField.setId(CSSIds.ABSTRACT_PARAM_CONTROL_COMBO_BOX);
        levelControlLevelField.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        levelControlLevelField.setMinMax(0F, Integer.MAX_VALUE);
        levelControlLevelField.addChangeListener((observable, oldValue, newValue) -> changeLevelControlLevel(newValue));
        levelControlLevelField.disableProperty().bind(levelControlUseMarker.selectedProperty());

        levelControlSettings = new GridPane();
        levelControlSettings.add(smoothlyLabel, 0, 0);
        levelControlSettings.add(levelControlSmoothly, 1, 0);
        levelControlSettings.add(useMarkerLabel, 0, 1);
        levelControlSettings.add(levelControlUseMarker, 1, 1);
        levelControlSettings.add(levelLabel, 0, 2);
        levelControlSettings.add(levelControlLevelField, 1, 2);

        FXUtils.addClassTo(smoothlyLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(levelControlSmoothly, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(useMarkerLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(levelControlUseMarker, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(levelLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(levelControlLevelField, CSSClasses.SPECIAL_FONT_13);
    }

    /**
     * Create settings of rough control.
     */
    private void createRoughControlSettings() {

        final Label roughnessLabel = new Label("Roughness:");
        roughnessLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        roughnessLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        roughControlRoughnessField = new FloatTextField();
        roughControlRoughnessField.setId(CSSIds.ABSTRACT_PARAM_CONTROL_COMBO_BOX);
        roughControlRoughnessField.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        roughControlRoughnessField.setMinMax(0F, Integer.MAX_VALUE);
        roughControlRoughnessField.addChangeListener((observable, oldValue, newValue) -> changeRoughControlRoughness(newValue));

        final Label frequencyLabel = new Label("Frequency:");
        frequencyLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        frequencyLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        roughControlFrequencyField = new FloatTextField();
        roughControlFrequencyField.setId(CSSIds.ABSTRACT_PARAM_CONTROL_COMBO_BOX);
        roughControlFrequencyField.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        roughControlFrequencyField.setMinMax(0.1F, Integer.MAX_VALUE);
        roughControlFrequencyField.addChangeListener((observable, oldValue, newValue) -> changeRoughControlFrequency(newValue));

        final Label lacunarityLabel = new Label("Lacunarity:");
        lacunarityLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        lacunarityLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        roughControlLacunarityField = new FloatTextField();
        roughControlLacunarityField.setId(CSSIds.ABSTRACT_PARAM_CONTROL_COMBO_BOX);
        roughControlLacunarityField.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        roughControlLacunarityField.setMinMax(1.1F, Integer.MAX_VALUE);
        roughControlLacunarityField.addChangeListener((observable, oldValue, newValue) -> changeRoughControlLacunarity(newValue));

        final Label octavesLabel = new Label("Octaves:");
        octavesLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        octavesLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        roughControlOctavesField = new FloatTextField();
        roughControlOctavesField.setId(CSSIds.ABSTRACT_PARAM_CONTROL_COMBO_BOX);
        roughControlOctavesField.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        roughControlOctavesField.setMinMax(0F, Integer.MAX_VALUE);
        roughControlOctavesField.addChangeListener((observable, oldValue, newValue) -> changeRoughControlOctaves(newValue));

        final Label scaleLabel = new Label("Scale:");
        scaleLabel.setId(CSSIds.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        scaleLabel.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        roughControlScaleField = new FloatTextField();
        roughControlScaleField.setId(CSSIds.ABSTRACT_PARAM_CONTROL_COMBO_BOX);
        roughControlScaleField.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        roughControlScaleField.setMinMax(0F, Integer.MAX_VALUE);
        roughControlScaleField.addChangeListener((observable, oldValue, newValue) -> changeRoughControlScale(newValue));

        roughControlSettings = new GridPane();
        roughControlSettings.add(roughnessLabel, 0, 0);
        roughControlSettings.add(roughControlRoughnessField, 1, 0);
        roughControlSettings.add(frequencyLabel, 0, 1);
        roughControlSettings.add(roughControlFrequencyField, 1, 1);
        roughControlSettings.add(lacunarityLabel, 0, 2);
        roughControlSettings.add(roughControlLacunarityField, 1, 2);
        roughControlSettings.add(octavesLabel, 0, 3);
        roughControlSettings.add(roughControlOctavesField, 1, 3);
        roughControlSettings.add(scaleLabel, 0, 4);
        roughControlSettings.add(roughControlScaleField, 1, 4);

        FXUtils.addClassTo(roughnessLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(roughControlRoughnessField, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(frequencyLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(roughControlFrequencyField, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(lacunarityLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(roughControlLacunarityField, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(octavesLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(roughControlOctavesField, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(scaleLabel, CSSClasses.SPECIAL_FONT_13);
        FXUtils.addClassTo(roughControlScaleField, CSSClasses.SPECIAL_FONT_13);
    }

    /**
     * @return the control to make some levels terrain.
     */
    @NotNull
    private LevelTerrainToolControl getLevelToolControl() {
        return levelToolControl;
    }

    /**
     * @return the control to make slopes on terrain.
     */
    @NotNull
    private SlopeTerrainToolControl getSlopeToolControl() {
        return slopeToolControl;
    }

    /**
     * @return the control to make rough surface terrain.
     */
    @NotNull
    private RoughTerrainToolControl getRoughToolControl() {
        return roughToolControl;
    }

    /**
     * Change using smoothly editing.
     */
    private void changeLevelControlSmoothly(final Boolean newValue) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getLevelToolControl().setPrecision(!newValue));
    }

    /**
     * Change using marker for level control.
     */
    private void changeLevelControlUseMarker(final Boolean newValue) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getLevelToolControl().setUseMarker(newValue));
    }

    /**
     * Change a level of a level control.
     */
    private void changeLevelControlLevel(final Float newLevel) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getLevelToolControl().setLevel(newLevel));
    }

    /**
     * Change using smoothly editing.
     */
    private void changeSlopeControlSmoothly(final Boolean newValue) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getSlopeToolControl().setPrecision(!newValue));
    }

    /**
     * Change using limited editing.
     */
    private void changeSlopeControlLimited(final Boolean newValue) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getSlopeToolControl().setLock(newValue));
    }

    /**
     * Change scale of a rough control.
     */
    private void changeRoughControlScale(final Float newScale) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getRoughToolControl().setScale(newScale));
    }

    /**
     * Change frequency of a rough control.
     */
    private void changeRoughControlFrequency(final Float newFrequency) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getRoughToolControl().setFrequency(newFrequency));
    }

    /**
     * Change lacunarity of a rough control.
     */
    private void changeRoughControlLacunarity(final Float newLacunarity) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getRoughToolControl().setLacunarity(newLacunarity));
    }

    /**
     * Change octaves of a rough control.
     */
    private void changeRoughControlOctaves(final Float newOctaves) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getRoughToolControl().setOctaves(newOctaves));
    }

    /**
     * Change roughness of a rough control.
     */
    private void changeRoughControlRoughness(final Float newRoughness) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getRoughToolControl().setRoughness(newRoughness));
    }

    /**
     * Change brush sizes.
     */
    private void changeBrushSize(@NotNull final Float size) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> {
            final Array<TerrainToolControl> toolControls = getToolControls();
            toolControls.forEach(size, TerrainToolControl::setBrushSize);
        });
    }

    /**
     * Change brush powers.
     */
    private void changeBrushPower(@NotNull final Float power) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> {
            final Array<TerrainToolControl> toolControls = getToolControls();
            toolControls.forEach(power, TerrainToolControl::setBrushPower);
        });
    }

    /**
     * @return the setting of using smoothly changing of terrain height.
     */
    @NotNull
    private CheckBox getLevelControlSmoothly() {
        return requireNonNull(levelControlSmoothly);
    }

    /**
     * @return the setting of using marker to detect a level.
     */
    @NotNull
    private CheckBox getLevelControlUseMarker() {
        return requireNonNull(levelControlUseMarker);
    }

    /**
     * @return the setting of target level.
     */
    @NotNull
    private FloatTextField getLevelControlLevelField() {
        return requireNonNull(levelControlLevelField);
    }

    /**
     * @return the setting of using limited between markers.
     */
    @NotNull
    private CheckBox getSlopeControlLimited() {
        return requireNonNull(slopeControlLimited);
    }

    /**
     * @return the setting of using smoothly changing of terrain height.
     */
    @NotNull
    private CheckBox getSlopeControlSmoothly() {
        return requireNonNull(slopeControlSmoothly);
    }

    /**
     * @return the settings of frequency.
     */
    @NotNull
    private FloatTextField getRoughControlFrequencyField() {
        return requireNonNull(roughControlFrequencyField);
    }

    /**
     * @return the settings of lacunarity.
     */
    @NotNull
    private FloatTextField getRoughControlLacunarityField() {
        return requireNonNull(roughControlLacunarityField);
    }

    /**
     * @return the settings of octaves.
     */
    @NotNull
    private FloatTextField getRoughControlOctavesField() {
        return requireNonNull(roughControlOctavesField);
    }

    /**
     * @return the settings of roughness.
     */
    @NotNull
    private FloatTextField getRoughControlRoughnessField() {
        return requireNonNull(roughControlRoughnessField);
    }

    /**
     * @return the settings of scale.
     */
    @NotNull
    private FloatTextField getRoughControlScaleField() {
        return requireNonNull(roughControlScaleField);
    }

    /**
     * Switch editing mode.
     */
    private void switchMode(@NotNull final ActionEvent event) {

        final ToggleButton source = (ToggleButton) event.getSource();

        if (!source.isSelected()) {
            source.setSelected(true);
            return;
        }

        getToggleButtons().forEach(source, (button, arg) -> button !=
                arg, (toggleButton, arg) -> toggleButton.setSelected(false));

        final ObjectDictionary<ToggleButton, Pane> buttonToSettings = getButtonToSettings();
        final Pane settings = buttonToSettings.get(source);

        final VBox controlSettings = getControlSettings();
        final ObservableList<javafx.scene.Node> children = controlSettings.getChildren();
        children.clear();

        if (settings != null) {
            children.add(settings);
        }

        final ObjectDictionary<ToggleButton, TerrainToolControl> buttonToControl = getButtonToControl();
        final TerrainToolControl toolControl = buttonToControl.get(source);

        setToolControl(toolControl);

        if (!isShowed()) return;

        final Node cursorNode = getCursorNode();
        cursorNode.removeControl(TerrainToolControl.class);
        cursorNode.addControl(toolControl);
    }

    @Override
    public void stopEditing() {
        super.stopEditing();
    }

    @Override
    public void startEditing(@NotNull final Object object) {
        super.startEditing(object);
    }

    @Override
    public boolean isSupport(@NotNull final Object object) {
        return object instanceof TerrainQuad;
    }

    @Override
    public void notifyShowed() {
        super.notifyShowed();
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getCursorNode().addControl(getToolControl()));
    }

    @Override
    public void notifyHided() {
        super.notifyHided();
        EXECUTOR_MANAGER.addEditorThreadTask(() -> getCursorNode().removeControl(TerrainToolControl.class));
    }

    @NotNull
    @Override
    public String getName() {
        return "Terrain editor";
    }
}