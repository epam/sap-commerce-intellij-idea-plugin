/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.copyright;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexFile;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.TreeTraversal;
import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.psi.UpdateCopyright;
import com.maddyhome.idea.copyright.psi.UpdateCopyrightsProvider;
import com.maddyhome.idea.copyright.psi.UpdatePsiFileCopyright;

import java.util.List;

public class UpdateImpexCopyrightsProvider extends UpdateCopyrightsProvider {

    @Override
    public UpdateCopyright createInstance(
        final Project project,
        final Module module,
        final VirtualFile file,
        final FileType base,
        final CopyrightProfile options
    ) {
        return new Provider(project, module, file, options);
    }

    private static class Provider extends UpdatePsiFileCopyright {

        public Provider(
            final Project project,
            final Module module,
            final VirtualFile root,
            final CopyrightProfile options
        ) {
            super(project, module, root, options);
        }

        @Override
        protected boolean accept() {
            return getFile() instanceof ImpexFile;
        }

        @Override
        protected void scanFile() {
            final List<PsiComment> comments = SyntaxTraverser.psiTraverser(getFile())
                                                             .withTraversal(TreeTraversal.LEAVES_DFS)
                                                             .traverse()
                                                             .takeWhile(Conditions.instanceOf(
                                                                 PsiComment.class,
                                                                 LeafPsiElement.class
                                                             ))
                                                             .filter(PsiComment.class)
                                                             .toList();
            checkComments(ContainerUtil.getLastItem(comments), true, comments);
        }

        @Override
        protected void addAction(final CommentAction action) {
            if (action.getType() == CommentAction.ACTION_INSERT) {
                super.addAction(new CommentAction(0, action.getPrefix(), action.getSuffix()));
            } else {
                super.addAction(action);
            }
        }
    }
}
