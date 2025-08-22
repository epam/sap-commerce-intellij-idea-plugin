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
import sap.commerce.toolset.cockpitNG.model.config.hybris.Mergeable;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:AbstractFlowType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface AbstractFlow extends DomElement, Mergeable {

	/**
	 * Returns the value of the id child.
	 * @return the value of the id child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("id")
	@Required
	GenericAttributeValue<String> getId();


	/**
	 * Returns the value of the model child.
	 * @return the value of the model child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("model")
	GenericAttributeValue<String> getModel();


	/**
	 * Returns the list of handler children.
	 * @return the list of handler children.
	 */
	@NotNull
	@SubTagList("handler")
	java.util.List<sap.commerce.toolset.cockpitNG.model.wizardConfig.ComposedHandler> getHandlers();
	/**
	 * Adds new child to the list of handler children.
	 * @return created child
	 */
	@SubTagList ("handler")
	sap.commerce.toolset.cockpitNG.model.wizardConfig.ComposedHandler addHandler();


	/**
	 * Returns the value of the prepare child.
	 * @return the value of the prepare child.
	 */
	@NotNull
	@SubTag("prepare")
	sap.commerce.toolset.cockpitNG.model.wizardConfig.Prepare getPrepare();


	/**
	 * Returns the list of step children.
	 * @return the list of step children.
	 */
	@NotNull
	@SubTagList ("step")
	java.util.List<Step> getSteps();
	/**
	 * Adds new child to the list of step children.
	 * @return created child
	 */
	@SubTagList ("step")
	Step addStep();


	/**
	 * Returns the list of subflow children.
	 * @return the list of subflow children.
	 */
	@NotNull
	@SubTagList ("subflow")
	java.util.List<sap.commerce.toolset.cockpitNG.model.wizardConfig.Subflow> getSubflows();
	/**
	 * Adds new child to the list of subflow children.
	 * @return created child
	 */
	@SubTagList ("subflow")
	sap.commerce.toolset.cockpitNG.model.wizardConfig.Subflow addSubflow();


}
