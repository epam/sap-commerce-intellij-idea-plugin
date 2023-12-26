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

public class FieldDesc implements Comparable<FieldDesc> {

    final static int STR = 1;
    final static int BOOL = 2;
    final static int OBJ = 3;
    final static int ATTR = 4;
    final static int DOUBLE = 5;
    final static int SIMPLE = 6;

    public FieldDesc(final String name, final String def) {
        this.name = name;
        this.def = def;
    }

    public FieldDesc(final int clType, final String name, final String type, final String elementType, final String def, final boolean required) {
        this.clType = clType;
        this.name = name;
        this.type = type;
        this.elementType = elementType;
        this.def = def;
        this.required = required;
    }

    int clType = STR;
    String name;
    String type;
    String elementType;
    String def;
    boolean required;

    int idx;
    String tagName;
    String elementName;
    String comment;
    FieldDesc[] choice;
    boolean choiceOpt;

    String documentation;
    String simpleTypesString;
    int duplicateIndex = -1;
    int realIndex;
    String contentQualifiedName;

    public int compareTo(final FieldDesc o) {
        return name.compareTo(o.name);
    }

    public String toString() {
        return "Field: " + name + ';' + type + ';' + elementName + ';' + elementType;
    }

}
