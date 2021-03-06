package com.ss.editor.ui.component.creator.impl;

import static com.ss.editor.Messages.POST_FILTER_VIEW_FILE_CREATOR_FILE_DESCRIPTION;
import static com.ss.editor.Messages.POST_FILTER_VIEW_FILE_CREATOR_TITLE;

import com.ss.editor.FileExtensions;
import com.ss.editor.ui.component.creator.FileCreatorDescription;
import com.ss.editor.util.EditorUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Реализация создателя новых файлов для PostFilterView.
 *
 * @author Ronn
 */
public class PostFilterViewFileCreator extends AbstractFileCreator {

    public static final FileCreatorDescription DESCRIPTION = new FileCreatorDescription();

    static {
        DESCRIPTION.setFileDescription(POST_FILTER_VIEW_FILE_CREATOR_FILE_DESCRIPTION);
        DESCRIPTION.setConstructor(PostFilterViewFileCreator::new);
    }

    public PostFilterViewFileCreator() {
    }

    @NotNull
    @Override
    protected String getTitleText() {
        return POST_FILTER_VIEW_FILE_CREATOR_TITLE;
    }

    @NotNull
    @Override
    protected String getFileExtension() {
        return FileExtensions.POST_FILTER_VIEW;
    }

    @Override
    protected void processCreate() {
        super.processCreate();

        final String materialContent = "";
        final Path fileToCreate = getFileToCreate();

        try (final PrintWriter out = new PrintWriter(Files.newOutputStream(fileToCreate))) {
            out.print(materialContent);
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return;
        }

        notifyFileCreated(fileToCreate, true);
    }
}
