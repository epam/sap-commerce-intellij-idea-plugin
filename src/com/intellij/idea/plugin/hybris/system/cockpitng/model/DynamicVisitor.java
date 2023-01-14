// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:dynamicVisitor interface.
 */
public interface DynamicVisitor extends DomElement, Positioned {

    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    @Required
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the beanId child.
     *
     * @return the value of the beanId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("beanId")
    @Required
    GenericAttributeValue<String> getBeanId();


    /**
     * Returns the value of the scriptingConfig child.
     *
     * @return the value of the scriptingConfig child.
     */
    @NotNull
    @SubTag("scriptingConfig")
    ScriptingConfig getScriptingConfig();


}
