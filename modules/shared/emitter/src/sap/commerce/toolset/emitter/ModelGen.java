/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package sap.commerce.toolset.emitter;

import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.ArrayUtil;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ModelGen {

    private final ModelDesc model = new ModelDesc();
    private final Map<String, String> schemaLocationMap = new HashMap<>();
    private final ModelLoader loader;
    private final Emitter emitter;
    private final FileManager fileManager;


    public ModelGen(final ModelLoader loader) {
        this(loader, new JetBrainsEmitter(), new MergingFileManager());
    }

    public ModelGen(final ModelLoader loader, final Emitter emitter, final FileManager fileManager) {
        this.loader = loader;
        this.emitter = emitter;
        this.fileManager = fileManager;
    }

    public ModelDesc getModel() {
        return model;
    }

    public static Element loadXml(final File configXml) throws Exception {
        return JDOMUtil.load(configXml);
    }

    public void loadConfig(final File configXml) throws Exception {
        loadConfig(loadXml(configXml));
    }

    public void setConfig(final String schema, final String location, final NamespaceDesc desc, final String... schemasToSkip) {
        schemaLocationMap.put(schema, location);
        for (String sch : schemasToSkip) {
            if (sch != null && sch.length() > 0) {
                model.nsdMap.put(sch, new NamespaceDesc(sch));
            }
        }
        model.nsdMap.put("", new NamespaceDesc("", "", "com.intellij.util.xml.DomElement", "", null, null, null, null));
        model.nsdMap.put(desc.name, desc);
    }

    public void loadConfig(final Element element) {
        final Element namespaceEl = element.getChild("namespaces");
        for (Element e : namespaceEl.getChildren("schemaLocation")) {
            final String name = e.getAttributeValue("name");
            final String file = e.getAttributeValue("file");
            schemaLocationMap.put(name, file);
        }
        for (Element e : namespaceEl.getChildren("reserved-name")) {
            final String name = e.getAttributeValue("name");
            final String replacement = e.getAttributeValue("replace-with");
            model.name2replaceMap.put(name, replacement);
        }
        NamespaceDesc def = new NamespaceDesc("", "generated", "java.lang.Object", "", null, null, null, null);
        for (Element nsElement : namespaceEl.getChildren("namespace")) {
            final String name = nsElement.getAttributeValue("name");
            final NamespaceDesc nsDesc = new NamespaceDesc(name, def);

            final String skip = nsElement.getAttributeValue("skip");
            final String prefix = nsElement.getAttributeValue("prefix");
            final String superC = nsElement.getAttributeValue("super");
            final String imports = nsElement.getAttributeValue("imports");
            final String packageS = nsElement.getAttributeValue("package");
            final String packageEnumS = nsElement.getAttributeValue("enums");
            final String interfaces = nsElement.getAttributeValue("interfaces");
            final ArrayList<String> list = new ArrayList<>();
            for (Element pkgElement : nsElement.getChildren("package")) {
                final String pkgName = pkgElement.getAttributeValue("name");
                final String fileName = pkgElement.getAttributeValue("file");
                list.add(fileName);
                list.add(pkgName);
            }
            for (Element pkgElement : nsElement.getChildren("property")) {
                final String propertyName = pkgElement.getAttributeValue("name");
                final String propertyValue = pkgElement.getAttributeValue("value");
                nsDesc.props.put(propertyName, propertyValue);
            }

            if (skip != null) {
                nsDesc.skip = skip.equalsIgnoreCase("true");
            }
            if (prefix != null) {
                nsDesc.prefix = prefix;
            }
            if (superC != null) {
                nsDesc.superClass = superC;
            }
            if (imports != null) {
                nsDesc.imports = imports;
            }
            if (packageS != null) {
                nsDesc.pkgName = packageS;
            }
            if (packageEnumS != null) {
                nsDesc.enumPkg = packageEnumS;
            }
            if (interfaces != null) {
                nsDesc.intfs = interfaces;
            }
            if (!list.isEmpty()) {
                nsDesc.pkgNames = ArrayUtil.toStringArray(list);
            }
            if (name.length() == 0) {
                def = nsDesc;
            }
            model.nsdMap.put(name, nsDesc);
        }
    }

    public void perform(final File outputRoot, final File... modelRoots) throws Exception {
        loadModel(modelRoots);
        emitter.emit(fileManager, model, outputRoot);

        Util.log("Done.");
    }

    public void loadModel(final File... modelRoots) throws Exception {
        final XMLEntityResolver resolver = xmlResourceIdentifier -> {
            String esid = xmlResourceIdentifier.getExpandedSystemId();
            if (esid == null) {
                final String location = schemaLocationMap.get(xmlResourceIdentifier.getNamespace());
                if (location != null) {
                    esid = location;
                } else {
                    return null;
                }
            }
            // Util.log("resolving "+esid);
            File f = null;
            for (File root : modelRoots) {
                if (root == null) {
                    continue;
                }
                if (root.isDirectory()) {
                    final String fileName = esid.substring(esid.lastIndexOf('/') + 1);
                    f = new File(root, fileName);
                } else {
                    f = root;
                }
            }
            if (f == null || !f.exists()) {
                Util.logerr("unable to resolve: " + esid);
                return null;
            }
            esid = f.getPath();
            return new XMLInputSource(null, esid, null);
        };
        final ArrayList<File> files = new ArrayList<>();
        for (File root : modelRoots) {
            if (null != root.listFiles()) {
                //noinspection ConstantConditions
                files.addAll(Arrays.asList(root.listFiles()));
            }
        }
        loader.loadModel(model, files, resolver);
        Util.log(model.jtMap.size() + " java types loaded");
    }

}
