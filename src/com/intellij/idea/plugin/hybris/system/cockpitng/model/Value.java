// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/valuechooser

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/valuechooser:value interface.
 */
public interface Value extends DomElement {

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
     * Returns the value of the expression child.
     *
     * @return the value of the expression child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("expression")
    @Required
    GenericAttributeValue<String> getExpression();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the bean-factory child.
     *
     * @return the value of the bean-factory child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("bean-factory")
    GenericAttributeValue<String> getBeanFactory();


    /**
     * Returns the value of the factory-method child.
     *
     * @return the value of the factory-method child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("factory-method")
    GenericAttributeValue<String> getFactoryMethod();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


}
