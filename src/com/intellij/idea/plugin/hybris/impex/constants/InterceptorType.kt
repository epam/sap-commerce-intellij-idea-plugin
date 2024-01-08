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

package com.intellij.idea.plugin.hybris.impex.constants

enum class InterceptorType(val code: String, val title: String, val interceptorClass: String) {
    VALIDATE("validate", "Validate", "de.hybris.platform.servicelayer.interceptor.ValidateInterceptor"),
    PREPARE("prepare", "Prepare", "de.hybris.platform.servicelayer.interceptor.PrepareInterceptor"),
    LOAD("load", "Load", "de.hybris.platform.servicelayer.interceptor.LoadInterceptor"),
    REMOVE("remove", "Remove", "de.hybris.platform.servicelayer.interceptor.RemoveInterceptor"),
    INIT_DEFAULTS("init_defaults", "Init Defaults", "de.hybris.platform.servicelayer.interceptor.InitDefaultsInterceptor");
}