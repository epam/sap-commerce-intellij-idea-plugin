/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.Attribute
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.idea.plugin.hybris.system.type.model.ItemTypes
import com.intellij.idea.plugin.hybris.system.type.model.Persistence
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement

object TSPsiHelper {

    fun resolveTypeCode(element: PsiElement) = resolveItemType(element)
        ?.getAttributeValue(ItemType.CODE)

    fun resolveItemType(element: PsiElement): XmlTag? = PsiTreeUtil.findFirstParent(element, true)
    { e -> return@findFirstParent e is XmlTag && e.name == ItemTypes.ITEMTYPE } as? XmlTag?

    fun resolveAttributeHandlerId(persistanceXmlTag: XmlTag): String? {
        val explicitAttributeHandler = persistanceXmlTag.getAttributeValue(Persistence.ATTRIBUTE_HANDLER)

        if (explicitAttributeHandler != null) return explicitAttributeHandler

        val typecode = resolveTypeCode(persistanceXmlTag) ?: return null
        val attributeQualifier = persistanceXmlTag.parentTag?.getAttributeValue(Attribute.QUALIFIER) ?: return null

        // Magic starts here, see official documentation: https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/8bb46096866910149208fae7c4ec7596.html?locale=en-US
        return typecode + "_" + attributeQualifier + "AttributeHandler"
    }

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
        val xmlTag = meta.retrieveDom()
            ?.xmlTag
            ?: return

        WriteCommandAction.runWriteCommandAction(project, null, null, {
            xmlTag.delete()

            Notifications.create(
                NotificationType.INFORMATION,
                message(messageTitleKey),
                message(messageContentKey, ownerName ?: "?", meta.name ?: "?")
            )
                .notify(project)
        }, xmlTag.containingFile)
    }
}