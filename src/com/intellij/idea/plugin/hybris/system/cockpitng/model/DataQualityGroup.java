// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/summaryview

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/summaryview:dataQualityGroup interface.
 */
public interface DataQualityGroup extends DomElement, Positioned {

    /**
     * Returns the value of the warning child.
     *
     * @return the value of the warning child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("warning")
    GenericAttributeValue<Double> getWarning();


    /**
     * Returns the value of the error child.
     *
     * @return the value of the error child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("error")
    GenericAttributeValue<Double> getError();


    /**
     * Returns the value of the domain-id child.
     *
     * @return the value of the domain-id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("domain-id")
    @Required
    GenericAttributeValue<String> getDomainId();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("label")
    GenericAttributeValue<String> getLabel();

}
