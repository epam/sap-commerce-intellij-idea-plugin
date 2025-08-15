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

package sap.commerce.toolset

import com.intellij.AbstractBundle
import org.apache.commons.lang3.StringUtils
import org.jetbrains.annotations.PropertyKey

object HybrisI18NBundleUtils : AbstractBundle("i18n.HybrisBundle") {

    @JvmStatic
    fun message(
        @PropertyKey(resourceBundle = "i18n.HybrisBundle") key: String,
        vararg params: Any
    ): String {
        if (StringUtils.isBlank(key)) {
            return ""
        }

        val message = getMessage(key, *params)

        return if (StringUtils.isBlank(message)) key else message
    }

    fun messageFallback(
        @PropertyKey(resourceBundle = "i18n.HybrisBundle") key: String,
        fallback: String,
        vararg params: Any
    ) = messageOrDefault(key, fallback, *params)!!
}