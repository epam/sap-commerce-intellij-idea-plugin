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

import org.apache.xerces.xs.XSObject;

import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ModelDesc {

    final Map<String, String> name2replaceMap = new HashMap<>();
    final Map<QName, String> qname2FileMap = new HashMap<>();
    final Map<String, NamespaceDesc> nsdMap = new HashMap<>();
    final Map<String, TypeDesc> jtMap = new TreeMap<>();

    public String getNSDPrefix(final TypeDesc td) {
        return getNSDPrefix(td.xsNamespace, td.name, td.type == TypeDesc.TypeEnum.ENUM);
    }

    public String getNSDPrefix(final String namespace, final String name, final boolean isEnum) {
        final int lastIdx = name.lastIndexOf(".");
        if (lastIdx > -1) {
            return name.substring(0, lastIdx + 1);
        }
        final NamespaceDesc nsd = getNSD(namespace);
        if (isEnum && nsd.enumPkg != null) {
            return nsd.enumPkg + ".";
        }
        if (nsd.pkgNames != null) {
            final QName qname = new QName(namespace, name);
            final String files = qname2FileMap.get(qname);
            if (files != null) {
                for (int i = 0; i < nsd.pkgNames.length; i += 2) {
                    String file = nsd.pkgNames[i];
                    String pkg = nsd.pkgNames[i + 1];
                    if (files.contains(":" + file + ":")) {
                        return pkg + ".";
                    }
                }
            }
        }
        return nsd.pkgName != null && nsd.pkgName.length() > 0 ? nsd.pkgName + "." : "";
    }

    public NamespaceDesc getNSD(String namespace) {
        NamespaceDesc nsd = nsdMap.get(namespace);
        if (nsd == null) {
            nsd = nsdMap.get("");
        }
        return nsd;
    }


    public String toJavaTypeName(String tname, String ns) {
        final int lastIndex = tname.lastIndexOf('.');
        String xmlName = lastIndex > -1 ? tname.substring(lastIndex + 1) : tname;
        NamespaceDesc nsd = getNSD(ns);
        if (ns == null || !ns.endsWith(".dtd")) {
            if (xmlName.endsWith(Util.ANONYMOUS_ELEM_TYPE_SUFFIX)) {
                xmlName = xmlName.substring(
                    0,
                    xmlName.length() - Util.ANONYMOUS_ELEM_TYPE_SUFFIX
                        .length()
                );
            } else if (xmlName.endsWith(Util.ANONYMOUS_ATTR_TYPE_SUFFIX)) {
                xmlName = xmlName.substring(
                    0,
                    xmlName.length() - Util.ANONYMOUS_ATTR_TYPE_SUFFIX
                        .length()
                );
            } else if (xmlName.endsWith(Util.TYPE_SUFFIX)) {
                xmlName = xmlName.substring(
                    0,
                    xmlName.length() - Util.TYPE_SUFFIX
                        .length()
                );
            }
        }
        String rc = Util.capitalize(Util.toJavaName(xmlName));
        if (nsd.prefix != null && nsd.prefix.length() > 0 && !rc.startsWith(nsd.prefix)) {
            rc = nsd.prefix + rc;
        }
        if (Util.RESERVED_NAMES_MAP.containsKey(rc)) {
            rc = Util.RESERVED_NAMES_MAP.get(rc);
        }
        if (name2replaceMap.containsKey(rc)) {
            rc = Util.expandProperties(name2replaceMap.get(rc), nsd.props);
        }

        return rc;
    }

    public String toJavaQualifiedTypeName(XSObject xs, Map<String, NamespaceDesc> nsdMap, boolean isEnum) {
        String typeName = toJavaTypeName(xs.getName(), xs.getNamespace());
        return getNSDPrefix(xs.getNamespace(), xs.getName(), isEnum) + typeName;
    }

    public String toJavaQualifiedTypeName(String namespace, String xmlname, boolean isEnum) {
        return getNSDPrefix(namespace, xmlname, isEnum) + toJavaTypeName(xmlname, namespace);
    }

    void dump(final PrintWriter out) {
//    out.println("-- qname2FileMap ---");
//    out.println(qname2FileMap);
        out.println("-- nsdMap ---");
        for (Map.Entry<String, NamespaceDesc> entry : nsdMap.entrySet()) {
            out.println("namespace key: " + entry.getKey());
            dumpNamespace(entry.getValue(), out);
        }
        out.println("-- jtMap ---");
        for (Map.Entry<String, TypeDesc> entry : jtMap.entrySet()) {
            out.println("type key: " + entry.getKey());
            dumpTypeDesc(entry.getValue(), out);
        }
    }

    private void dumpTypeDesc(TypeDesc td, PrintWriter out) {
        final ArrayList<String> superList;
        if (td.supers != null) {
            superList = new ArrayList<String>();
            for (TypeDesc aSuper : td.supers) {
                superList.add(getNSDPrefix(aSuper) + aSuper.name);
            }
        } else {
            superList = null;
        }
        out.println("  name      " + td.name);
        out.println("  type      " + td.type);
        out.println("  xsName    " + td.xsName);
        out.println("  xsNS      " + td.xsNamespace);
        out.println("  dups      " + td.duplicates);
        out.println("  supers    " + (superList != null ? superList : "null"));
        out.println("  doc       " + (td.documentation != null ? td.documentation.length() : "null"));
        for (Map.Entry<String, FieldDesc> entry : td.fdMap.entrySet()) {
            out.println("  field key: " + entry.getKey());
            dumpFieldDesc(td, entry.getValue(), out);
        }
    }

    private void dumpFieldDesc(TypeDesc td, FieldDesc fd, PrintWriter out) {
        out.println("    name      " + fd.name);
        if (td.type == TypeDesc.TypeEnum.ENUM) {
            return;
        }
        out.println("    clType    " + fd.clType);
        out.println("    required  " + fd.required);
        out.println("    index     " + fd.idx + "/" + fd.realIndex);
        out.println("    choiceOpt " + fd.choiceOpt);
        out.println("    choice    " + (fd.choice != null ? fd.choice.length : "null"));
        out.println("    content   " + fd.contentQualifiedName);
        out.println("    dupIdx    " + fd.duplicateIndex);
        out.println("    elName    " + fd.elementName);
        out.println("    elType    " + fd.elementType);
        out.println("    def       " + fd.def);
        out.println("    doc       " + (fd.documentation != null ? fd.documentation.length() : "null"));
    }

    private void dumpNamespace(NamespaceDesc value, PrintWriter out) {
        if (value.skip) {
            return;
        }
        out.println("  name     " + value.name);
        out.println("  prefix   " + value.prefix);
        out.println("  pkgName  " + value.pkgName);
        out.println("  pkgNames " + (value.pkgNames != null ? Arrays.asList(value.pkgNames) : "null"));
        out.println("  enumPkg  " + value.enumPkg);
        out.println("  super    " + value.superClass);
    }
}
