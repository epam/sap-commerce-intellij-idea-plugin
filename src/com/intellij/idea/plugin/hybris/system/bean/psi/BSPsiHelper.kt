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

package com.intellij.idea.plugin.hybris.system.bean.psi

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.system.bean.meta.model.*
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement

object BSPsiHelper {

    fun delete(project: Project, owner: BSGlobalMetaBean, meta: BSMetaAnnotations) = delete(
        project, owner.shortName, meta,
        "hybris.bs.wizard.bean.modified.title",
        "hybris.bs.wizard.bean.annotations.delete.content"
    )

    fun delete(project: Project, owner: BSGlobalMetaBean, meta: BSMetaHint) = delete(
        project, owner.shortName, meta,
        "hybris.bs.wizard.bean.modified.title",
        "hybris.bs.wizard.bean.hint.delete.content"
    )

    fun delete(project: Project, owner: BSGlobalMetaBean, meta: BSMetaImport) = delete(
        project, owner.shortName, meta,
        "hybris.bs.wizard.bean.modified.title",
        "hybris.bs.wizard.bean.import.delete.content"
    )

    fun delete(project: Project, owner: BSGlobalMetaBean, meta: BSMetaProperty) = delete(
        project, owner.shortName, meta,
        "hybris.bs.wizard.bean.modified.title",
        "hybris.bs.wizard.bean.property.delete.content"
    )

    fun delete(project: Project, owner: BSGlobalMetaEnum, meta: BSMetaEnum.BSMetaEnumValue) = delete(
        project, owner.shortName, meta,
        "hybris.bs.wizard.enum.modified.title",
        "hybris.bs.wizard.enum.value.delete.content"
    )

    private fun delete(
        project: Project,
        ownerName: String?,
        meta: BSMetaClassifier<out DomElement>,
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
                        message(messageTitleKey),
                        message(messageContentKey, ownerName ?: "?", meta.name ?: "?")
                    )
                        .notify(project)
                }
            }
    }
}