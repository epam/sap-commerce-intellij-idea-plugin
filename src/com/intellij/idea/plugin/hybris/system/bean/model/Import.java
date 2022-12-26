// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.bean.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:import interface.
 */
public interface Import extends DomElement {

    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @Required
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the static child.
     *
     * @return the value of the static child.
     */
    @NotNull
    GenericAttributeValue<Boolean> getStatic();


}
