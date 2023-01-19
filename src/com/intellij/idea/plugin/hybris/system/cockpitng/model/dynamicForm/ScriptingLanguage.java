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

// Generated on Thu Jan 19 16:25:49 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model.dynamicForm;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:scriptingLanguage enumeration.
 * <pre>
 * <h3>Enumeration http://www.hybris.com/cockpitng/component/dynamicForms:scriptingLanguage documentation</h3>
 * You can select between Groovy, Spring Expression Language (SpEL), BeanShell and JavaScript. SpEL is the
 * 				default.
 * </pre>
 */
public enum ScriptingLanguage implements com.intellij.util.xml.NamedEnum {
	BEAN_SHELL ("BeanShell"),
	GROOVY ("Groovy"),
	JAVA_SCRIPT ("JavaScript"),
	SP_EL ("SpEL");

	private final String value;
	private ScriptingLanguage(String value) { this.value = value; }
	@Override
	public String getValue() { return value; }

}
