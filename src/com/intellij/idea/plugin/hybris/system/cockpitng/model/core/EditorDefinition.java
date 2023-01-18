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

// Generated on Wed Jan 18 00:34:54 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model.core;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.Stubbed;
import com.intellij.util.xml.StubbedOccurrence;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:EditorDefinition interface.
 */
@Stubbed
@StubbedOccurrence
public interface EditorDefinition extends DomElement, ComponentDefinition {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @SubTag("name")
    GenericDomValue<String> getName();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    @SubTag("description")
    GenericDomValue<String> getDescription();


    /**
     * Returns the value of the author child.
     *
     * @return the value of the author child.
     */
    @NotNull
    @SubTag("author")
    GenericDomValue<String> getAuthor();


    /**
     * Returns the value of the version child.
     *
     * @return the value of the version child.
     */
    @NotNull
    @SubTag("version")
    GenericDomValue<String> getVersion();


    /**
     * Returns the value of the keywords child.
     *
     * @return the value of the keywords child.
     */
    @NotNull
    @SubTag("keywords")
    Keywords getKeywords();


    /**
     * Returns the value of the settings child.
     *
     * @return the value of the settings child.
     */
    @NotNull
    @SubTag("settings")
    Settings getSettings();


    /**
     * Returns the value of the view child.
     *
     * @return the value of the view child.
     */
    @NotNull
    @SubTag("view")
    View getView();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @SubTag("type")
    @Required
    GenericDomValue<String> getType();


    /**
     * Returns the value of the editorClassName child.
     *
     * @return the value of the editorClassName child.
     */
    @NotNull
    @SubTag("editorClassName")
    @Required
    GenericDomValue<String> getEditorClassName();


    /**
     * Returns the value of the sockets child.
     *
     * @return the value of the sockets child.
     */
    @NotNull
    @SubTag("sockets")
    Sockets getSockets();


    /**
     * Returns the value of the handlesLocalization child.
     *
     * @return the value of the handlesLocalization child.
     */
    @NotNull
    @SubTag("handlesLocalization")
    GenericDomValue<Boolean> getHandlesLocalization();


}
