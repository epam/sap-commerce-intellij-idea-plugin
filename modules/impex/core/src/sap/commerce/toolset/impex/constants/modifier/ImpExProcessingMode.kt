/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.constants.modifier

import sap.commerce.toolset.HybrisIcons
import javax.swing.Icon

enum class ImpExProcessingMode(val presentationText: String, val icon: Icon) {
    ANY("any mode", HybrisIcons.ImpEx.MODE_ANY),
    IMPORT("import", HybrisIcons.ImpEx.MODE_IMPORT),
    EXPORT("export", HybrisIcons.ImpEx.MODE_EXPORT);
}
