// Generated on Wed Jan 18 00:35:36 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/summaryview

package com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/summaryview:actions interface.
 */
public interface Actions extends DomElement, Positioned {

    /**
     * Returns the value of the component-id child.
     *
     * @return the value of the component-id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("component-id")
    @Required
    GenericAttributeValue<String> getComponentId();


    /**
     * Returns the value of the group child.
     *
     * @return the value of the group child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("group")
    GenericAttributeValue<String> getGroup();


    /**
     * Returns the value of the renderer child.
     *
     * @return the value of the renderer child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("renderer")
    GenericAttributeValue<String> getRenderer();

}
