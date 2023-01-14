// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:scriptingConfig interface.
 */
public interface ScriptingConfig extends DomElement {

    /**
     * Returns the value of the visibleIfLanguage child.
     *
     * @return the value of the visibleIfLanguage child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("visibleIfLanguage")
    GenericAttributeValue<ScriptingLanguage> getVisibleIfLanguage();


    /**
     * Returns the value of the visibleIfScriptType child.
     *
     * @return the value of the visibleIfScriptType child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("visibleIfScriptType")
    GenericAttributeValue<Scripting> getVisibleIfScriptType();


    /**
     * Returns the value of the disabledIfLanguage child.
     *
     * @return the value of the disabledIfLanguage child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disabledIfLanguage")
    GenericAttributeValue<ScriptingLanguage> getDisabledIfLanguage();


    /**
     * Returns the value of the disabledIfScriptType child.
     *
     * @return the value of the disabledIfScriptType child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disabledIfScriptType")
    GenericAttributeValue<Scripting> getDisabledIfScriptType();


    /**
     * Returns the value of the gotoTabIfLanguage child.
     *
     * @return the value of the gotoTabIfLanguage child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("gotoTabIfLanguage")
    GenericAttributeValue<ScriptingLanguage> getGotoTabIfLanguage();


    /**
     * Returns the value of the gotoTabIfScriptType child.
     *
     * @return the value of the gotoTabIfScriptType child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("gotoTabIfScriptType")
    GenericAttributeValue<Scripting> getGotoTabIfScriptType();


    /**
     * Returns the value of the computedValueLanguage child.
     *
     * @return the value of the computedValueLanguage child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("computedValueLanguage")
    GenericAttributeValue<ScriptingLanguage> getComputedValueLanguage();


    /**
     * Returns the value of the computedValueScriptType child.
     *
     * @return the value of the computedValueScriptType child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("computedValueScriptType")
    GenericAttributeValue<Scripting> getComputedValueScriptType();


}
