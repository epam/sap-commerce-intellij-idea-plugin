// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:IfType interface.
 */
public interface If extends DomElement {

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
     * Returns the value of the target child.
     *
     * @return the value of the target child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("target")
    GenericAttributeValue<String> getTarget();


}
