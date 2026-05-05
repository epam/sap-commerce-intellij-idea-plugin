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

import de.hybris.platform.core.Registry
import groovy.json.JsonOutput

def ctx = Registry.getApplicationContext()

def isDynamicBean(String beanName) { beanName?.matches('.*#\\d+$') }
def isInternalSpringBean(String beanName) { beanName?.startsWith('org.springframework.') }

def allBeans = []

def processBeans(context, level, beansList) {
    context.beanDefinitionNames.each { beanName ->
        def beanClass = context.getType(beanName)?.name
        if (beanClass != null && beanName != null
                && !isDynamicBean(beanName)
                && !isInternalSpringBean(beanName)
        ) {
            def aliases = context.getAliases(beanName)
            def bean = [
                    level: level,
                    id: beanName,
                    class: beanClass
                            .replaceAll('\\$\\$SpringCGLIB\\$\\$.*', '')
                            .replaceAll('\\$\\$EnhancerBySpringCGLIB\\$\\$.*', '')
            ]
            if (aliases) {
                bean.aliases = aliases as List
            }
            beansList.add(bean)
        }
    }
}

// Current context (Web) - level 0
processBeans(ctx, 0, allBeans)

// Parent contexts - level 1, 2, etc.
def parent = ctx.parent
def level = 1
while (parent != null) {
    processBeans(parent, level, allBeans)
    parent = parent.parent
    level++
}

JsonOutput.toJson(allBeans)
