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

// Generated on Thu Jan 19 16:25:07 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package sap.commerce.toolset.cockpitNG.model.itemEditor;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Mergeable;
import sap.commerce.toolset.xml.FalseAttributeValue;
import sap.commerce.toolset.xml.TrueAttributeValue;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:attribute interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Attribute extends DomElement, AbstractPositioned, Mergeable {

	String QUALIFIER = "qualifier";
	String READONLY = "readonly";
	String VISIBLE = "visible";

	/**
	 * Returns the value of the qualifier child.
	 * @return the value of the qualifier child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (QUALIFIER)
	@Required
	GenericAttributeValue<String> getQualifier();


	/**
	 * Returns the value of the label child.
	 * @return the value of the label child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("label")
	GenericAttributeValue<String> getLabel();


	/**
	 * Returns the value of the visible child.
	 * @return the value of the visible child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (VISIBLE)
	TrueAttributeValue getVisible();


	/**
	 * Returns the value of the readonly child.
	 * @return the value of the readonly child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (READONLY)
	FalseAttributeValue getReadonly();


	/**
	 * Returns the value of the editor child.
	 * @return the value of the editor child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("editor")
	GenericAttributeValue<String> getEditor();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("description")
	GenericAttributeValue<String> getDescription();


	/**
	 * Returns the list of editor-parameter children.
	 * @return the list of editor-parameter children.
	 */
	@NotNull
	@SubTagList ("editor-parameter")
	java.util.List<Parameter> getEditorParameters();
	/**
	 * Adds new child to the list of editor-parameter children.
	 * @return created child
	 */
	@SubTagList ("editor-parameter")
	Parameter addEditorParameter();


}
