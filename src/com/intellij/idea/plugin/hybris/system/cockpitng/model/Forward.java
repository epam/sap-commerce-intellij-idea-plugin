// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:forwardElemType interface.
 */
public interface Forward extends DomElement {

    /**
     * Returns the value of the input child.
     *
     * @return the value of the input child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("input")
    @Required
    GenericAttributeValue<String> getInput();


    /**
     * Returns the value of the output child.
     *
     * @return the value of the output child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("output")
    @Required
    GenericAttributeValue<String> getOutput();


}
