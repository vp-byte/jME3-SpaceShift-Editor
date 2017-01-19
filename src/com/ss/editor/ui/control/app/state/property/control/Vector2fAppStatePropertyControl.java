package com.ss.editor.ui.control.app.state.property.control;

import static com.ss.editor.ui.control.app.state.property.control.AppStatePropertyControl.newChangeHandler;

import com.jme3.math.Vector2f;
import com.ss.editor.model.undo.editor.SceneChangeConsumer;
import com.ss.editor.ui.control.property.AbstractPropertyControl;
import com.ss.editor.ui.control.property.impl.AbstractVector2fPropertyControl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of the {@link AbstractPropertyControl} to edit vector2f values.
 *
 * @author JavaSaBr
 */
public class Vector2fAppStatePropertyControl<T> extends AbstractVector2fPropertyControl<SceneChangeConsumer, T> {

    public Vector2fAppStatePropertyControl(@Nullable final Vector2f propertyValue, @NotNull final String propertyName,
                                           @NotNull final SceneChangeConsumer changeConsumer) {
        super(propertyValue, propertyName, changeConsumer, newChangeHandler());
    }
}
