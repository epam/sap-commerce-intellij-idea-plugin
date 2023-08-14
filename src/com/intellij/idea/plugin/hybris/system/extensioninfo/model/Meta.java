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

// Generated on Mon Jan 09 16:31:21 CET 2023
// DTD/Schema  :    null
package com.intellij.idea.plugin.hybris.system.extensioninfo.model;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.system.extensioninfo.file.MetaKeyConverter;
import com.intellij.idea.plugin.hybris.system.extensioninfo.file.MetaValueConverter;
import com.intellij.spellchecker.xml.NoSpellchecking;
import com.intellij.util.xml.*;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * null:metaType interface.
 * <pre>
 * <h3>Type null:metaType documentation</h3>
 * Configures metadata.
 * </pre>
 */
@ApiStatus.AvailableSince(HybrisConstants.PLATFORM_VERSION_5_0)
public interface Meta extends DomElement {

	String KEY = "key";
	String VALUE = "value";

	/**
	 * Returns the value of the key child.
	 * <pre>
	 * <h3>Attribute null:key documentation</h3>
	 * Metadata key.
	 * </pre>
	 * @return the value of the key child.
	 */
	@NotNull
	@Attribute(KEY)
	@Required
	@Convert(MetaKeyConverter.class)
	@NoSpellchecking
	GenericAttributeValue<String> getKey();

	/**
	 * Returns the value of the value child.
	 * <pre>
	 * <h3>Attribute null:value documentation</h3>
	 * Metadata value.
	 * </pre>
	 * @return the value of the value child.
	 */
	@NotNull
	@Attribute(VALUE)
	@Convert(MetaValueConverter.class)
	@Required
	GenericAttributeValue<String> getValue();

}
