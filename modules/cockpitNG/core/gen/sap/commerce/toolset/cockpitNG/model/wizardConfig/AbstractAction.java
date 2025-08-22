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

// Generated on Thu Jan 19 16:23:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package sap.commerce.toolset.cockpitNG.model.wizardConfig;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:AbstractActionType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface AbstractAction extends DomElement {

	String VISIBLE = "visible";

	/**
	 * Returns the value of the visible child.
	 * @return the value of the visible child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute (VISIBLE)
	GenericAttributeValue<String> getVisible();


	/**
	 * Returns the value of the default-target child.
	 * @return the value of the default-target child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("default-target")
	GenericAttributeValue<String> getDefaultTarget();


	/**
	 * Returns the list of if children.
	 * @return the list of if children.
	 */
	@NotNull
	@SubTagList ("if")
	java.util.List<If> getIfs();
	/**
	 * Adds new child to the list of if children.
	 * @return created child
	 */
	@SubTagList ("if")
	If addIf();


	/**
	 * Returns the value of the save-all child.
	 * @return the value of the save-all child.
	 */
	@NotNull
	@SubTag ("save-all")
	GenericDomValue<String> getSaveAll();


	/**
	 * Returns the list of save children.
	 * @return the list of save children.
	 */
	@NotNull
	@SubTagList ("save")
	java.util.List<Save> getSaves();
	/**
	 * Adds new child to the list of save children.
	 * @return created child
	 */
	@SubTagList ("save")
	Save addSave();


}
