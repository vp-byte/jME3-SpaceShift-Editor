package com.ss.editor.ui.control.model.tree.dialog.geometry.lod;

import com.jme3.scene.Mesh;
import com.ss.editor.ui.control.model.tree.dialog.geometry.lod.GenerateLodLevelsDialog.ReductionMethod;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.css.CSSIds;

import com.ss.editor.ui.util.UIUtils;
import org.jetbrains.annotations.NotNull;

import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import rlib.ui.util.FXUtils;

/**
 * The implementation of the list cell to edit LoD levels.
 *
 * @author JavaSaBr
 */
public class LodValueCell extends TextFieldListCell<Number> {

    private final StringConverter<Number> converter = new StringConverter<Number>() {

        @Override
        public String toString(final Number value) {
            return String.valueOf(value);
        }

        @Override
        public Number fromString(final String string) {

            final ReductionMethod method = dialog.getMethod();
            final Mesh mesh = dialog.getMesh();

            if (method == ReductionMethod.CONSTANT) {

                final int value = Integer.parseInt(string);
                if (value < 1) return 1;
                if (value > mesh.getTriangleCount()) return mesh.getTriangleCount();

                return value;

            } else {

                final float value = Float.parseFloat(string);
                if (value < 0.001F) return 0.001F;
                if (value >= 1.0F) return 1F;

                return value;
            }
        }
    };

    /**
     * The generator dialog.
     */
    @NotNull
    private final GenerateLodLevelsDialog dialog;

    LodValueCell(final @NotNull GenerateLodLevelsDialog dialog) {
        setId(CSSIds.GENERATE_LOD_DIALOG_LIST_VIEW_CELL);
        setConverter(converter);

        this.dialog = dialog;

        FXUtils.addClassTo(this, CSSClasses.TRANSPARENT_LIST_CELL);
        FXUtils.addClassTo(this, CSSClasses.SPECIAL_FONT_15);
    }

    @Override
    public void startEdit() {
        if (!isEditable()) return;
        super.startEdit();
        UIUtils.updateEditedCell(this);
    }
}
