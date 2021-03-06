package com.ss.editor.ui.control.model.tree.action.emitter.influerencer;

import com.ss.editor.Messages;
import com.ss.editor.model.undo.editor.ModelChangeConsumer;
import com.ss.editor.ui.control.tree.AbstractNodeTree;
import com.ss.editor.ui.control.tree.node.ModelNode;

import org.jetbrains.annotations.NotNull;

import tonegod.emitter.ParticleEmitterNode;
import tonegod.emitter.influencers.ParticleInfluencer;
import tonegod.emitter.influencers.impl.DestinationInfluencer;

/**
 * The action to create a {@link DestinationInfluencer} for a {@link ParticleEmitterNode}.
 *
 * @author JavaSaBr
 */
public class CreateDestinationParticleInfluencerAction extends AbstractCreateParticleInfluencerAction {

    public CreateDestinationParticleInfluencerAction(@NotNull final AbstractNodeTree<ModelChangeConsumer> nodeTree, @NotNull final ModelNode<?> node) {
        super(nodeTree, node);
    }

    @NotNull
    @Override
    protected String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_EMITTER_CREATE_INFLUENCER_DESTINATION;
    }

    @NotNull
    @Override
    protected ParticleInfluencer createInfluencer() {
        return new DestinationInfluencer();
    }
}
