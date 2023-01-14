// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/fulltextsearch

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/fulltextsearch:FieldListType interface.
 */
public interface FieldList extends DomElement {

    /**
     * Returns the list of field children.
     *
     * @return the list of field children.
     */
    @NotNull
    @SubTagList("field")
    java.util.List<Field> getFields();

    /**
     * Adds new child to the list of field children.
     *
     * @return created child
     */
    @SubTagList("field")
    Field addField();


    /**
     * Returns the value of the includeSubtypes child.
     *
     * @return the value of the includeSubtypes child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("includeSubtypes")
    GenericAttributeValue<Boolean> getIncludeSubtypes();


    /**
     * Returns the value of the disable-subtypes-checkbox child.
     *
     * @return the value of the disable-subtypes-checkbox child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disable-subtypes-checkbox")
    GenericAttributeValue<Boolean> getDisableSubtypesCheckbox();


    /**
     * Returns the value of the disable-attributes-comparator child.
     *
     * @return the value of the disable-attributes-comparator child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disable-attributes-comparator")
    GenericAttributeValue<Boolean> getDisableAttributesComparator();


}
