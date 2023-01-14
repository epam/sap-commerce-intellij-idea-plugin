// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:CustomType interface.
 */
public interface Custom extends DomElement, AbstractAction {

    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("label")
    @Required
    GenericAttributeValue<String> getLabel();


    /**
     * Returns the value of the handler child.
     *
     * @return the value of the handler child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("handler")
    GenericAttributeValue<String> getHandler();


    /**
     * Returns the value of the composedHandler child.
     *
     * @return the value of the composedHandler child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("composedHandler")
    GenericAttributeValue<String> getComposedHandler();


    /**
     * Returns the value of the align child.
     *
     * @return the value of the align child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("align")
    GenericAttributeValue<Align> getAlign();


    /**
     * Returns the value of the primary child.
     *
     * @return the value of the primary child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("primary")
    GenericAttributeValue<Boolean> getPrimary();


    /**
     * Returns the value of the validate-visible-only child.
     *
     * @return the value of the validate-visible-only child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("validate-visible-only")
    GenericAttributeValue<Boolean> getValidateVisibleOnly();

    /**
     * Returns the list of parameter children.
     *
     * @return the list of parameter children.
     */
    @NotNull
    @SubTagList("parameter")
    java.util.List<Parameter> getParameters();

    /**
     * Adds new child to the list of parameter children.
     *
     * @return created child
     */
    @SubTagList("parameter")
    Parameter addParameter();

}