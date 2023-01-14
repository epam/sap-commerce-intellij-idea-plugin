// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:panel interface.
 */
public interface Panel extends DomElement, AbstractPanel {

    /**
     * Returns the list of attribute children.
     *
     * @return the list of attribute children.
     */
    @NotNull
    @SubTagList("attribute")
    java.util.List<Attribute> getAttributes();

    /**
     * Adds new child to the list of attribute children.
     *
     * @return created child
     */
    @SubTagList("attribute")
    Attribute addAttribute();


    /**
     * Returns the list of custom children.
     * <pre>
     * <h3>Element http://www.hybris.com/cockpitng/component/editorArea:custom documentation</h3>
     * Allows to insert custom html into section. Html code may contain
     * 									SpEL expressions regarding edited object - SpEL expressions need to be in braces
     *                                    {}
     * </pre>
     *
     * @return the list of custom children.
     */
    @NotNull
    @SubTagList("custom")
    java.util.List<CustomElement> getCustoms();

    /**
     * Adds new child to the list of custom children.
     *
     * @return created child
     */
    @SubTagList("custom")
    CustomElement addCustom();


}
