/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.system.type.psi

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement

object TSPsiHelper {

    fun resolveTypeCode(element: PsiElement) = element.parentsOfType<XmlTag>()
        .firstOrNull { it.localName == "itemtype" && it.getAttribute("code") != null }
        ?.getAttributeValue("code")

    fun delete(project: Project, owner: TSGlobalMetaEnum, meta: TSMetaEnum.TSMetaEnumValue) = delete(
        project, owner.name, meta,
        "hybris.ts.wizard.enum.modified.title",
        "hybris.ts.wizard.enum.value.delete.content"
    )

    fun delete(project: Project, owner: TSGlobalMetaItem, meta: TSMetaItem.TSMetaItemAttribute) = delete(
        project, owner.name, meta,
        "hybris.ts.wizard.item.modified.title",
        "hybris.ts.wizard.item.attribute.delete.content"
    )

    fun delete(project: Project, owner: TSGlobalMetaItem, meta: TSMetaItem.TSMetaItemIndex) = delete(
        project, owner.name, meta,
        "hybris.ts.wizard.item.modified.title",
        "hybris.ts.wizard.item.index.delete.content"
    )

    fun delete(project: Project, owner: TSGlobalMetaItem, meta: TSMetaCustomProperty) = delete(
        project, owner.name, meta,
        "hybris.ts.wizard.item.modified.title",
        "hybris.ts.wizard.item.customProperty.delete.content"
    )

    private fun delete(
        project: Project,
        ownerName: String?,
        meta: TSMetaClassifier<out DomElement>,
        messageTitleKey: String,
        messageContentKey: String,
    ) {
        meta.retrieveDom()
            ?.xmlTag
            ?.let { xmlTag ->
                WriteCommandAction.runWriteCommandAction(project) {
                    xmlTag.delete()

                    Notifications.create(
                        NotificationType.INFORMATION,
                        HybrisI18NBundleUtils.message(messageTitleKey),
                        HybrisI18NBundleUtils.message(messageContentKey, ownerName ?: "?", meta.name ?: "?")
                    )
                        .notify(project)
                }
            }
    }
}