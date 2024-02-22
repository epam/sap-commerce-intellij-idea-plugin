/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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

// Generated on Thu Jan 19 16:23:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Mergeable;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:ViewType interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface View extends DomElement, Mergeable {

	/**
	 * Returns the value of the simple content.
	 * @return the value of the simple content.
	 */
	@NotNull
	@Required
	String getValue();
	/**
	 * Sets the value of the simple content.
	 * @param value the new value to set
	 */
	void setValue(@NotNull String value);


	/**
	 * Returns the value of the src child.
	 * @return the value of the src child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("src")
	GenericAttributeValue<String> getSrc();


	/**
	 * Returns the value of the lang child.
	 * @return the value of the lang child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("lang")
	@Required
	GenericAttributeValue<Lang> getLang();


	/**
	 * Returns the value of the id child.
	 * @return the value of the id child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("id")
	GenericAttributeValue<String> getId();


	/**
	 * Returns the value of the position child.
	 * @return the value of the position child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("position")
	GenericAttributeValue<Integer> getPosition();


	/**
	 * Returns the value of the renderer child.
	 * @return the value of the renderer child.
	 */
	@NotNull
	@SubTag ("renderer")
	Renderer getRenderer();


}
