// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/notifications:NotificationLinkReferenceContext interface.
 */
public interface NotificationLinkReferenceContext extends DomElement {

    /**
     * Returns the value of the parameter child.
     *
     * @return the value of the parameter child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("parameter")
    @Required
    GenericAttributeValue<String> getParameter();


    /**
     * Returns the value of the value child.
     *
     * @return the value of the value child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("value")
    GenericAttributeValue<String> getValue();


    /**
     * Returns the value of the evaluate child.
     *
     * @return the value of the evaluate child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("evaluate")
    GenericAttributeValue<Boolean> getEvaluate();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeMode();


}
