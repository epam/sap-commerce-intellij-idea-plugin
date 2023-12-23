/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

/*
 * XSD/DTD Model generator tool
 *
 * By Gregory Shrago
 * 2002 - 2006
 */
package com.intellij.idea.plugin.hybris.system.type.model.generator;

import java.util.HashMap;
import java.util.Map;

public class NamespaceDesc {

    public NamespaceDesc(
        final String name,
        final String pkgName,
        final String superClass,
        final String prefix,
        final String factoryClass,
        final String helperClass,
        final String imports,
        final String intfs
    ) {
        this.name = name;
        this.pkgName = pkgName;
        this.superClass = superClass;
        this.prefix = prefix;
        this.factoryClass = factoryClass;
        this.helperClass = helperClass;
        this.imports = imports;
        this.intfs = intfs;
    }

    public NamespaceDesc(final String name) {
        this(name, "generated", "java.lang.Object", "", null, null, null, null);
        skip = true;
    }


    public NamespaceDesc(final String name, final NamespaceDesc def) {
        this.name = name;
        this.pkgName = def.pkgName;
        this.superClass = def.superClass;
        this.prefix = def.prefix;
        this.factoryClass = def.factoryClass;
        this.helperClass = def.helperClass;
        this.imports = def.imports;
        this.intfs = def.intfs;
    }

    final Map<String, String> props = new HashMap<>();
    final String name;
    String pkgName;
    String superClass;
    String prefix;
    String factoryClass;
    String helperClass;
    String imports;
    String intfs;
    boolean skip;
    String[] pkgNames;
    String enumPkg;


    public String toString() {
        return "NS:" + name + ' ' + (skip ? "skip" : "") + pkgName;
    }
}
