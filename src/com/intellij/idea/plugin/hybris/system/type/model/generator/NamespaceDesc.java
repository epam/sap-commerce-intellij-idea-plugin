/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

/**
 * @author Konstantin Bulenkov
 */
public class NamespaceDesc {

    public NamespaceDesc(
        String name,
        String pkgName,
        String superClass,
        String prefix,
        String factoryClass,
        String helperClass,
        String imports,
        String intfs
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

    public NamespaceDesc(String name) {
        this(name, "generated", "java.lang.Object", "", null, null, null, null);
        skip = true;
    }


    public NamespaceDesc(String name, NamespaceDesc def) {
        this.name = name;
        this.pkgName = def.pkgName;
        this.superClass = def.superClass;
        this.prefix = def.prefix;
        this.factoryClass = def.factoryClass;
        this.helperClass = def.helperClass;
        this.imports = def.imports;
        this.intfs = def.intfs;
    }

    final Map<String, String> props = new HashMap<String, String>();
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
        return "NS:" + name + " " + (skip ? "skip" : "") + pkgName;
    }
}
