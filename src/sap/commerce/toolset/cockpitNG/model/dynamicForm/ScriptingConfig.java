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

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Namespace;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisConstants;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:scriptingConfig interface.
 */
@Namespace(HybrisConstants.COCKPIT_NG_NAMESPACE_KEY)
public interface ScriptingConfig extends DomElement {

	/**
	 * Returns the value of the visibleIfLanguage child.
	 * @return the value of the visibleIfLanguage child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("visibleIfLanguage")
	GenericAttributeValue<ScriptingLanguage> getVisibleIfLanguage();


	/**
	 * Returns the value of the visibleIfScriptType child.
	 * @return the value of the visibleIfScriptType child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("visibleIfScriptType")
	GenericAttributeValue<Scripting> getVisibleIfScriptType();


	/**
	 * Returns the value of the disabledIfLanguage child.
	 * @return the value of the disabledIfLanguage child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("disabledIfLanguage")
	GenericAttributeValue<ScriptingLanguage> getDisabledIfLanguage();


	/**
	 * Returns the value of the disabledIfScriptType child.
	 * @return the value of the disabledIfScriptType child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("disabledIfScriptType")
	GenericAttributeValue<Scripting> getDisabledIfScriptType();


	/**
	 * Returns the value of the gotoTabIfLanguage child.
	 * @return the value of the gotoTabIfLanguage child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("gotoTabIfLanguage")
	GenericAttributeValue<ScriptingLanguage> getGotoTabIfLanguage();


	/**
	 * Returns the value of the gotoTabIfScriptType child.
	 * @return the value of the gotoTabIfScriptType child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("gotoTabIfScriptType")
	GenericAttributeValue<Scripting> getGotoTabIfScriptType();


	/**
	 * Returns the value of the computedValueLanguage child.
	 * @return the value of the computedValueLanguage child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("computedValueLanguage")
	GenericAttributeValue<ScriptingLanguage> getComputedValueLanguage();


	/**
	 * Returns the value of the computedValueScriptType child.
	 * @return the value of the computedValueScriptType child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("computedValueScriptType")
	GenericAttributeValue<Scripting> getComputedValueScriptType();


}
