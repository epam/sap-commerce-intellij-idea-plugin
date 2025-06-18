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
package com.intellij.idea.plugin.hybris.acl.lang.folding

import com.intellij.idea.plugin.hybris.acl.psi.*
import com.intellij.idea.plugin.hybris.psi.FoldablePsiElement
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiElementFilter

class AclFoldingBuilder : FoldingBuilderEx(), DumbAware {

    private val filter: PsiElementFilter by lazy {
        PsiElementFilter { element -> element is AclComment || (element is FoldablePsiElement && !element.textRange.isEmpty) }
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val foldingSettings = DeveloperSettingsComponent.getInstance(root.project).state.aclSettings.folding
        if (!foldingSettings.enabled) return emptyArray()

        return CachedValuesManager.getCachedValue(root) {
            val results = SyntaxTraverser.psiTraverser(root)
                .filter { filter.isAccepted(it) }
                .mapNotNull {
                    val groupName = when (it) {
                        is AclUserRights -> "ACL - Root"
                        is AclUserRightsBody -> "ACL - Body"
                        is AclUserRightsValueLines -> "ACL - Lines"
                        else -> "Access Control Lists"
                    }
                    FoldingDescriptor(it.node, it.textRange, FoldingGroup.newGroup(groupName))
                }
                .toSet()
                .toTypedArray()

            CachedValueProvider.Result.create(
                results,
                root.containingFile,
                ProjectRootModificationTracker.getInstance(root.project),
                foldingSettings
            )
        }
    }

    override fun getPlaceholderText(node: ASTNode) = when (node.elementType) {
        AclTypes.COMMENT -> "/*...*/"

        AclTypes.USER_RIGHTS -> node
            .findChildByType(TokenSet.create(AclTypes.USER_RIGHTS_BODY))
            ?.getChildren(
                TokenSet.create(
                    AclTypes.USER_RIGHTS_VALUE_LINES_PASSWORD_AWARE,
                    AclTypes.USER_RIGHTS_VALUE_LINES_PASSWORD_UNAWARE,
                )
            )
            ?.size
            ?.let { "$it elements.." }

        AclTypes.USER_RIGHTS_BODY -> node
            .getChildren(
                TokenSet.create(
                    AclTypes.USER_RIGHTS_VALUE_LINES_PASSWORD_AWARE,
                    AclTypes.USER_RIGHTS_VALUE_LINES_PASSWORD_UNAWARE,
                )
            )
            .size
            .let { "$it elements.." }

        AclTypes.USER_RIGHTS_VALUE_LINES_PASSWORD_AWARE -> getPlaceholderText(
            node,
            AclTypes.USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_AWARE,
            AclTypes.USER_RIGHTS_VALUE_LINE_PASSWORD_AWARE
        )

        AclTypes.USER_RIGHTS_VALUE_LINES_PASSWORD_UNAWARE -> getPlaceholderText(
            node,
            AclTypes.USER_RIGHTS_VALUE_LINE_TYPE_PASSWORD_UNAWARE,
            AclTypes.USER_RIGHTS_VALUE_LINE_PASSWORD_UNAWARE
        )

        else -> null
    }

    private fun getPlaceholderText(node: ASTNode, typeElementType: IElementType, valueLineElementType: IElementType): String? = node.findChildByType(typeElementType)
        ?.findChildByType(AclTypes.USER_RIGHTS_VALUE_GROUP_TYPE)
        ?.text
        ?.let {
            val valueLines = node.getChildren(TokenSet.create(valueLineElementType)).size
            "$it - $valueLines permissions.."
        }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
