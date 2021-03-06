package com.ss.editor.ui.control.model.tree.action.emitter.shape;

import com.jme3.scene.Mesh;
import com.ss.editor.Messages;
import com.ss.editor.ui.control.tree.AbstractNodeTree;
import com.ss.editor.ui.control.tree.node.ModelNode;

import org.jetbrains.annotations.NotNull;

import tonegod.emitter.ParticleEmitterNode;
import tonegod.emitter.shapes.TriangleEmitterShape;

/**
 * The action to switch an emitter shape of the {@link ParticleEmitterNode} to a {@link TriangleEmitterShape}.
 *
 * @author JavaSaBr
 */
public class CreateTriangleShapeEmitterAction extends AbstractCreateShapeEmitterAction {

    public CreateTriangleShapeEmitterAction(@NotNull final AbstractNodeTree<?> nodeTree, @NotNull final ModelNode<?> node) {
        super(nodeTree, node);
    }

    @NotNull
    @Override
    protected String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_EMITTER_CHANGE_TRIANGLE_SHAPE;
    }

    @NotNull
    @Override
    protected Mesh createMesh() {
        return new TriangleEmitterShape(1);
    }
}
