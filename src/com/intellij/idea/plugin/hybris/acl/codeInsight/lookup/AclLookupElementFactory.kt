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

package com.intellij.idea.plugin.hybris.acl.codeInsight.lookup

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.codeInsight.completion.AutoPopupInsertHandler
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import javax.swing.Icon

object AclLookupElementFactory {

    private const val OFFSET = $$"$END_USERRIGHTS ".length

    fun buildUserRightsPasswordAware() = LookupElementBuilder.create(
        """
            ${'$'}START_USERRIGHTS
            Type;UID;MemberOfGroups;Password;Target;read;change;create;remove;change_perm
            
            ${'$'}END_USERRIGHTS
        """.trimIndent()
    )
        .withPresentableText("Password Aware User Rights")
        .withIcon(HybrisIcons.Acl.USER_RIGHTS_PASSWORD_AWARE)
        .withInsertHandler(object : AutoPopupInsertHandler() {
            override fun handle(context: InsertionContext, item: LookupElement) {
                val cursorOffset = context.editor.caretModel.offset
                context.editor.caretModel.moveToOffset(cursorOffset - OFFSET)
            }
        })

    fun buildUserRightsPasswordUnaware() = LookupElementBuilder.create(
        """
            ${'$'}START_USERRIGHTS
            Type;UID;MemberOfGroups;Target;read;change;create;remove;change_perm
            
            ${'$'}END_USERRIGHTS
        """.trimIndent()
    )
        .withPresentableText("Password Unaware User Rights")
        .withIcon(HybrisIcons.Acl.USER_RIGHTS_PASSWORD_UNAWARE)
        .withInsertHandler(object : AutoPopupInsertHandler() {
            override fun handle(context: InsertionContext, item: LookupElement) {
                val cursorOffset = context.editor.caretModel.offset
                context.editor.caretModel.moveToOffset(cursorOffset - OFFSET)
            }
        })

    fun buildPermissions() = listOf(
        buildPermission("+", "Granted", HybrisIcons.Acl.PERMISSION_GRANTED),
        buildPermission("-", "Denied", HybrisIcons.Acl.PERMISSION_DENIED),
        buildPermission(".", "Inherited", HybrisIcons.Acl.PERMISSION_INHERITED),
    )

    private fun buildPermission(permission: String, presentableText: String, icon: Icon) = LookupElementBuilder.create(permission)
        .withPresentableText(presentableText)
        .withTypeText(permission)
        .withTypeIconRightAligned(true)
        .withIcon(icon)
}