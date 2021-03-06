package com.ss.editor.ui.component.creator.impl;

import static java.util.Objects.requireNonNull;
import com.ss.editor.Messages;
import com.ss.editor.ui.component.creator.FileCreatorDescription;
import com.ss.editor.util.EditorUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import rlib.util.StringUtils;

/**
 * The creator to create an empty file.
 *
 * @author JavaSaBr
 */
public class EmptyFileCreator extends AbstractFileCreator {

    @NotNull
    public static final FileCreatorDescription DESCRIPTION = new FileCreatorDescription();

    static {
        DESCRIPTION.setFileDescription(Messages.EMPTY_FILE_CREATOR_DESCRIPTION);
        DESCRIPTION.setConstructor(EmptyFileCreator::new);
    }

    @NotNull
    @Override
    protected String getTitleText() {
        return Messages.EMPTY_FILE_CREATOR_TITLE;
    }

    @NotNull
    @Override
    protected String getFileExtension() {
        return StringUtils.EMPTY;
    }

    @Override
    protected void processCreate() {
        super.processCreate();

        final Path fileToCreate = requireNonNull(getFileToCreate());
        try {
            Files.createFile(fileToCreate);
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return;
        }

        notifyFileCreated(fileToCreate, true);
    }
}
