// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:ComposedHandlerType interface.
 */
public interface ComposedHandler extends DomElement {

    /**
     * Returns the value of the handlerBean child.
     *
     * @return the value of the handlerBean child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("handlerBean")
    @Required
    GenericAttributeValue<String> getHandlerBean();


    /**
     * Returns the value of the handlerId child.
     *
     * @return the value of the handlerId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("handlerId")
    @Required
    GenericAttributeValue<String> getHandlerId();


    /**
     * Returns the list of additionalParams children.
     *
     * @return the list of additionalParams children.
     */
    @NotNull
    @SubTagList("additionalParams")
    @Required
    java.util.List<AdditionalParam> getAdditionalParamses();

    /**
     * Adds new child to the list of additionalParams children.
     *
     * @return created child
     */
    @SubTagList("additionalParams")
    AdditionalParam addAdditionalParams();


}
