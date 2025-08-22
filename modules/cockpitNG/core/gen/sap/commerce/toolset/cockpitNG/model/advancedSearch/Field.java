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

// Generated on Thu Jan 19 16:22:55 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/advancedsearch

package sap.commerce.toolset.cockpitNG.model.advancedSearch;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Mergeable;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Positioned;
import sap.commerce.toolset.cockpitNG.util.xml.CngOperatorConverter;

/**
 * http://www.hybris.com/cockpitng/config/advancedsearch:FieldType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface Field extends DomElement, Positioned, Mergeable {

	String NAME = "name";
	String OPERATOR = "operator";
	String SELECTED = "selected";
	String EDITOR = "editor";
	String SORTABLE = "sortable";
	String DISABLED = "disabled";
	String MANDATORY = "mandatory";
	String EDITOR_PARAMETER = "editor-parameter";

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (NAME)
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the operator child.
	 * @return the value of the operator child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (OPERATOR)
	@Convert(CngOperatorConverter.class)
	GenericAttributeValue<String> getOperator();


	/**
	 * Returns the value of the selected child.
	 * @return the value of the selected child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (SELECTED)
	GenericAttributeValue<Boolean> getSelected();


	/**
	 * Returns the value of the editor child.
	 * @return the value of the editor child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (EDITOR)
	GenericAttributeValue<String> getEditor();


	/**
	 * Returns the value of the sortable child.
	 * @return the value of the sortable child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (SORTABLE)
	GenericAttributeValue<Boolean> getSortable();


	/**
	 * Returns the value of the disabled child.
	 * @return the value of the disabled child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (DISABLED)
	GenericAttributeValue<Boolean> getDisabled();


	/**
	 * Returns the value of the mandatory child.
	 * @return the value of the mandatory child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (MANDATORY)
	GenericAttributeValue<Boolean> getMandatory();


	/**
	 * Returns the list of editor-parameter children.
	 * @return the list of editor-parameter children.
	 */
	@NotNull
	@SubTagList (EDITOR_PARAMETER)
	java.util.List<Parameter> getEditorParameters();
	/**
	 * Adds new child to the list of editor-parameter children.
	 * @return created child
	 */
	@SubTagList (EDITOR_PARAMETER)
	Parameter addEditorParameter();


}
