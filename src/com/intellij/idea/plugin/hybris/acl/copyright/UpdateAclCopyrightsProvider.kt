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
package com.intellij.idea.plugin.hybris.acl.copyright

import com.intellij.idea.plugin.hybris.acl.psi.AclFile
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiComment
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.TreeTraversal
import com.maddyhome.idea.copyright.CopyrightProfile
import com.maddyhome.idea.copyright.psi.UpdateCopyright
import com.maddyhome.idea.copyright.psi.UpdateCopyrightsProvider
import com.maddyhome.idea.copyright.psi.UpdatePsiFileCopyright

class UpdateAclCopyrightsProvider : UpdateCopyrightsProvider() {

    override fun createInstance(
        project: Project?,
        module: Module?,
        vf: VirtualFile?,
        ft: FileType?,
        options: CopyrightProfile?
    ): UpdateCopyright? = object : UpdatePsiFileCopyright(project, module, vf, options) {
        override fun accept() = file is AclFile

        override fun scanFile() {
            val comments = SyntaxTraverser.psiTraverser(file)
                .withTraversal(TreeTraversal.LEAVES_DFS)
                .traverse()
                .takeWhile { it is PsiComment || it is LeafPsiElement }
                .filterIsInstance<PsiComment>()

            checkComments(ContainerUtil.getLastItem(comments), true, comments)
        }

        override fun addAction(action: CommentAction) {
            if (action.type == CommentAction.ACTION_INSERT) {
                super.addAction(CommentAction(0, action.prefix, action.suffix))
            } else {
                super.addAction(action)
            }
        }
    }

}
