// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.bean.model;

import com.intellij.idea.plugin.hybris.util.xml.FalseAttributeValue;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * null:enum interface.
 */
public interface Enum extends DomElement, AbstractPojo {

    /**
     * Returns the value of the deprecated child.
     * <pre>
     * <h3>Attribute null:deprecated documentation</h3>
     * Marks bean as deprecated. Allows defining a message.
     * </pre>
     *
     * @return the value of the deprecated child.
     */
    @NotNull
    FalseAttributeValue getDeprecated();

    @NotNull
    @Attribute("deprecatedSince")
    GenericAttributeValue<String> getDeprecatedSince();


    /**
     * Returns the value of the class child.
     *
     * @return the value of the class child.
     */
    @NotNull
    @Attribute("class")
    @Required
    GenericAttributeValue<String> getClazz();


    /**
     * Returns the value of the template child.
     *
     * @return the value of the template child.
     */
    @NotNull
    GenericAttributeValue<String> getTemplate();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    GenericDomValue<String> getDescription();


    /**
     * Returns the list of value children.
     *
     * @return the list of value children.
     */
    @NotNull
    @Required
    List<EnumValue> getValues();

    /**
     * Adds new child to the list of value children.
     *
     * @return created child
     */
    GenericDomValue<String> addValue();


}
