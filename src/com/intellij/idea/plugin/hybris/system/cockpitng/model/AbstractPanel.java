// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:abstractPanel interface.
 */
public interface AbstractPanel extends DomElement, AbstractPositioned {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    @Required
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the colspan child.
     *
     * @return the value of the colspan child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("colspan")
    GenericAttributeValue<String> getColspan();


    /**
     * Returns the value of the rowspan child.
     *
     * @return the value of the rowspan child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("rowspan")
    GenericAttributeValue<String> getRowspan();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("description")
    GenericAttributeValue<String> getDescription();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();

}
