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

package com.intellij.idea.plugin.hybris.beans.meta

import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaBean
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaEnum
import com.intellij.util.xml.DomElement

class BeansMetaHelper {

    companion object {
        fun getShortName(name: String?) = name?.split(".")?.lastOrNull()
        fun isDeprecated(it: BeansGlobalMetaClassifier<DomElement>): Boolean {
            if (it is BeansMetaEnum) {
                return it.isDeprecated
            }

            if (it is BeansMetaBean) {
                return it.isDeprecated
            }

            return false
        }
    }
}