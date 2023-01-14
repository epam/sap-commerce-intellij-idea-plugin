// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/perspectiveChooser

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/perspectiveChooser:authority interface.
 */
public interface Authority extends DomElement {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    @Required
    GenericAttributeValue<String> getNameAttr();


    /**
     * Returns the list of perspective children.
     *
     * @return the list of perspective children.
     */
    @NotNull
    @SubTagList("perspective")
    java.util.List<Perspective> getPerspectives();

    /**
     * Adds new child to the list of perspective children.
     *
     * @return created child
     */
    @SubTagList("perspective")
    Perspective addPerspective();


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
     * Returns the list of view children.
     *
     * @return the list of view children.
     */
    @NotNull
    @SubTagList("view")
    java.util.List<View> getViews();

    /**
     * Adds new child to the list of view children.
     *
     * @return created child
     */
    @SubTagList("view")
    View addView();


}
