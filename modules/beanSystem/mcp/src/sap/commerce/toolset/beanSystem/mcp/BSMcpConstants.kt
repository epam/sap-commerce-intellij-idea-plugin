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

package sap.commerce.toolset.beanSystem.mcp

object BSMcpConstants {

    object Descriptions {
        const val BEAN_DETAIL = """Controls how much information is returned per bean, to balance completeness against token usage:
            |- BASIC: bean identity only (name, shortName, extends, template, extension, and the custom/abstract/deprecated flags). No properties.
            |- MEMBERS: the above plus each bean's declared properties as {name, type, referencedType}.
            |- FULL: the above plus description, deprecatedSince, superEquals, imports, annotations, and per-property description/deprecated. Only non-empty values are included.
            |Default: BASIC. Prefer the smallest level that answers the question. Properties are the bean's DECLARED properties, not inherited ones."""
        const val ENUM_DETAIL = """Controls how much information is returned per enum, to balance completeness against token usage:
            |- BASIC: enum identity only (name, shortName, extension, and the custom/deprecated flags). No values.
            |- MEMBERS: the above plus the enum's value names.
            |- FULL: the above plus description and deprecatedSince. Only non-empty values are included.
            |Default: BASIC. Prefer the smallest level that answers the question."""
        const val ENUM_DETAIL_MEMBERS_DEFAULT = """Controls how much information is returned per enum, to balance completeness against token usage:
            |- BASIC: enum identity only (name, shortName, extension, and the custom/deprecated flags). No values.
            |- MEMBERS: the above plus the enum's value names.
            |- FULL: the above plus description and deprecatedSince. Only non-empty values are included.
            |Default: MEMBERS. Prefer the smallest level that answers the question."""
    }
}
