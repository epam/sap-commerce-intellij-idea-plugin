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

// Generated on Thu Jan 19 16:25:49 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package sap.commerce.toolset.cockpitNG.model.dynamicForm;

import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Mergeable;
import sap.commerce.toolset.cockpitNG.model.config.hybris.Positioned;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:abstractDynamicElement interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface AbstractDynamicElement extends DomElement, Positioned, Mergeable {

	/**
	 * Returns the value of the id child.
	 * @return the value of the id child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("id")
	@Required
	GenericAttributeValue<String> getId();


	/**
	 * Returns the value of the visibleIf child.
	 * @return the value of the visibleIf child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("visibleIf")
	GenericAttributeValue<String> getVisibleIf();


	/**
	 * Returns the value of the disabledIf child.
	 * @return the value of the disabledIf child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("disabledIf")
	GenericAttributeValue<String> getDisabledIf();


	/**
	 * Returns the value of the modelProperty child.
	 * <pre>
	 * <h3>Attribute null:modelProperty documentation</h3>
	 * Overrides modelProperty attribute from dynamicForms element.
	 * </pre>
	 * @return the value of the modelProperty child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("modelProperty")
	GenericAttributeValue<String> getModelProperty();


	/**
	 * Returns the value of the triggeredOn child.
	 * <pre>
	 * <h3>Attribute null:triggeredOn documentation</h3>
	 * Dynamic forms actions will be triggered on change of elements specified here as a comma
	 * 							separated
	 * 							values.
	 * 							By default it is set to "*" therefore it is triggered on every model change.
	 * </pre>
	 * @return the value of the triggeredOn child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("triggeredOn")
	GenericAttributeValue<String> getTriggeredOn();


	/**
	 * Returns the value of the qualifier child.
	 * <pre>
	 * <h3>Attribute null:qualifier documentation</h3>
	 * Qualifier name of element on which actions are performed.
	 * </pre>
	 * @return the value of the qualifier child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("qualifier")
	@Required
	GenericAttributeValue<String> getQualifier();


	/**
	 * Returns the value of the scriptingConfig child.
	 * @return the value of the scriptingConfig child.
	 */
	@NotNull
	@SubTag ("scriptingConfig")
	ScriptingConfig getScriptingConfig();


}
