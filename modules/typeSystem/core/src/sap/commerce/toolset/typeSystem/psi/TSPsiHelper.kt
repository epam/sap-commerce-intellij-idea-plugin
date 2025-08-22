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

package sap.commerce.toolset.typeSystem.psi

import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.meta.TSMetaHelper
import sap.commerce.toolset.typeSystem.meta.model.*
import sap.commerce.toolset.typeSystem.model.Attribute
import sap.commerce.toolset.typeSystem.model.ItemType
import sap.commerce.toolset.typeSystem.model.ItemTypes
import sap.commerce.toolset.typeSystem.model.Persistence

object TSPsiHelper {

    fun resolveTypeCode(element: PsiElement) = resolveItemType(element)
        ?.getAttributeValue(ItemType.CODE)

    fun resolveItemType(element: PsiElement): XmlTag? = PsiTreeUtil.findFirstParent(element, true)
    { e -> return@findFirstParent e is XmlTag && e.name == ItemTypes.ITEMTYPE } as? XmlTag?

    fun resolveAttributeHandlerId(persistenceXmlTag: XmlTag): String? {
        val explicitAttributeHandler = persistenceXmlTag.getAttributeValue(Persistence.ATTRIBUTE_HANDLER)

        if (explicitAttributeHandler != null) return explicitAttributeHandler

        val typecode = resolveTypeCode(persistenceXmlTag) ?: return null
        val attributeQualifier = persistenceXmlTag.parentTag?.getAttributeValue(Attribute.QUALIFIER) ?: return null

        return TSMetaHelper.getAttributeHandlerId(typecode, attributeQualifier)
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
                i18n(messageTitleKey),
                i18n(messageContentKey, ownerName ?: "?", meta.name ?: "?")
            )
                .notify(project)
        }, xmlTag.containingFile)
    }
}