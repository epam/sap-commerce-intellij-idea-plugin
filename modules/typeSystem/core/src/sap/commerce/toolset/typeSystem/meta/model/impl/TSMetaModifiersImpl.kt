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
package sap.commerce.toolset.typeSystem.meta.model.impl

import com.intellij.util.xml.DomAnchor
import com.intellij.util.xml.DomService
import sap.commerce.toolset.typeSystem.meta.model.TSMetaModifiers
import sap.commerce.toolset.typeSystem.model.Modifiers
import sap.commerce.toolset.xml.toBoolean

internal class TSMetaModifiersImpl(
    dom: Modifiers,
    override val moduleName: String,
    override val extensionName: String,
    override var isCustom: Boolean
) : TSMetaModifiers {

    override val domAnchor: DomAnchor<Modifiers> = DomService.getInstance().createAnchor(dom)
    override val isRead = dom.read.toBoolean()
    override val isWrite = dom.write.toBoolean()
    override val isSearch = dom.search.toBoolean()
    override val isOptional = dom.optional.toBoolean()
    override val isPrivate = dom.private.toBoolean()
    override val isInitial = dom.initial.toBoolean()
    override val isRemovable = dom.removable.toBoolean()
    override val isPartOf = dom.partOf.toBoolean()
    override val isUnique = dom.unique.toBoolean()
    override val isDoNotOptimize = dom.doNotOptimize.toBoolean()
    override val isEncrypted = dom.encrypted.toBoolean()

    override fun toString() = "Modifiers(module=$extensionName, isCustom=$isCustom)"
}
