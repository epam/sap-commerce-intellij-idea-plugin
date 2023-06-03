/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.project.providers;

import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.WritingAccessProvider;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class HybrisWritingAccessProvider extends WritingAccessProvider {

    /**
     * VirtualFile's marked with tis key are ignored by this provider.
     * This is needed to allow controlled refactoring for the generated bean classes (which are normally read-only)
     */
    public static final Key<Boolean> KEY_TEMPORARY_WRITABLE = Key.create(
        HybrisWritingAccessProvider.class.getName() + "TemporaryWritable");

    private final Project myProject;

    @NotNull
    public static HybrisWritingAccessProvider getInstance(@NotNull final Project project) {
        return EP.getExtensions(project).stream()
                .map(o -> ObjectUtils.tryCast(o, HybrisWritingAccessProvider.class))
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    public HybrisWritingAccessProvider(@NotNull final Project project) {
        myProject = project;
    }

    @NotNull
    @Override
    public Collection<VirtualFile> requestWriting(final Collection<? extends VirtualFile> files) {
        final List<VirtualFile> writingDenied = new ArrayList<>();
        for (VirtualFile file : files) {
            if (isFileReadOnly(file)) {
                writingDenied.add(file);
            }
        }
        return writingDenied;
    }

    @Override
    public boolean isPotentiallyWritable(@NotNull final VirtualFile file) {
        return !isFileReadOnly(file);
    }

    /**
     * Requests to temporary ignore the read-only status provided by this provider.
     * Does nothing if read-only status is for some other reason
     */
    public void markTemporaryWritable(@NotNull VirtualFile vFile) {
        if (vFile.isValid() && !isPotentiallyWritable(vFile)) {
            vFile.putUserData(KEY_TEMPORARY_WRITABLE, true);
        }
    }

    /**
     * Clears any possible flag marked by {@link #markTemporaryWritable(VirtualFile)} call
     */
    public static void unmarkTemporaryWritable(@NotNull VirtualFile vFile) {
        if (vFile.isValid()) {
            vFile.putUserData(KEY_TEMPORARY_WRITABLE, null);
        }
    }

    protected boolean isFileReadOnly(@NotNull final VirtualFile file) {
        if (isTemporarilyWritable(file)) {
            return false;
        }
        return Optional.ofNullable(ModuleUtilCore.findModuleForFile(file, myProject))
                       .map(module -> HybrisProjectSettingsComponent.getInstance(myProject)
                                                                .getModuleSettings(module)
                                                                .isReadonly())

                       .orElse(false);
    }

    private boolean isTemporarilyWritable(@NotNull final VirtualFile vFile) {
        Boolean excluded = vFile.getUserData(KEY_TEMPORARY_WRITABLE);
        return excluded != null && excluded;
    }

}
