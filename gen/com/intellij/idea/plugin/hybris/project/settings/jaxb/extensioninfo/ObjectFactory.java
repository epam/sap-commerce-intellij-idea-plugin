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

package com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.intellij.idea.plugin.hybris.project.settings.jaxb package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.intellij.idea.plugin.hybris.project.settings.jaxb
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExtensionInfo }
     */
    public ExtensionInfo createExtensioninfo() {
        return new ExtensionInfo();
    }

    /**
     * Create an instance of {@link RequiresExtensionType }
     */
    public RequiresExtensionType createRequiresExtensionType() {
        return new RequiresExtensionType();
    }

    /**
     * Create an instance of {@link WebModuleType }
     */
    public WebModuleType createWebmoduleType() {
        return new WebModuleType();
    }

    /**
     * Create an instance of {@link HmcModuleType }
     */
    public HmcModuleType createHmcmoduleType() {
        return new HmcModuleType();
    }

    /**
     * Create an instance of {@link CoreModuleType }
     */
    public CoreModuleType createCoremoduleType() {
        return new CoreModuleType();
    }

    /**
     * Create an instance of {@link ExtensionType }
     */
    public ExtensionType createExtensionType() {
        return new ExtensionType();
    }

    /**
     * Create an instance of {@link MetaType }
     */
    public MetaType createMetaType() {
        return new MetaType();
    }

}
