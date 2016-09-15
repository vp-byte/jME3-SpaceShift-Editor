package com.ss.editor.state.editor.impl.model;

import com.jme3.scene.Spatial;
import com.ss.editor.util.NodeUtils;

import org.jetbrains.annotations.NotNull;

import tonegod.emitter.ParticleEmitterNode;
import tonegod.emitter.geometry.ParticleEmitterGeometry;

/**
 * The class with utilities methods for {@link ModelEditorState}.
 *
 * @author JavaSaBr.
 */
public class ModelEditorUtils {

    @NotNull
    public static Object findToSelect(@NotNull final Object object) {

        if (object instanceof ParticleEmitterGeometry) {
            final Spatial parent = NodeUtils.findParent((Spatial) object, spatial -> spatial instanceof ParticleEmitterNode);
            if (parent != null) return parent;
        }

        return object;
    }
}
