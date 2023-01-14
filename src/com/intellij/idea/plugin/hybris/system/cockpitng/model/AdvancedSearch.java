// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/advancedsearch

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/advancedsearch:advanced-searchElemType interface.
 */
public interface AdvancedSearch extends DomElement {

    /**
     * Returns the value of the connection-operator child.
     *
     * @return the value of the connection-operator child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("connection-operator")
    GenericAttributeValue<ConnectionOperator> getConnectionOperator();


    /**
     * Returns the value of the disable-auto-search child.
     *
     * @return the value of the disable-auto-search child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disable-auto-search")
    GenericAttributeValue<Boolean> getDisableAutoSearch();


    /**
     * Returns the value of the disable-simple-search child.
     *
     * @return the value of the disable-simple-search child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disable-simple-search")
    GenericAttributeValue<Boolean> getDisableSimpleSearch();


    /**
     * Returns the value of the field-list child.
     *
     * @return the value of the field-list child.
     */
    @NotNull
    @SubTag("field-list")
    @Required
    FieldList getFieldList();


    /**
     * Returns the value of the sort-field child.
     *
     * @return the value of the sort-field child.
     */
    @NotNull
    @SubTag("sort-field")
    SortField getSortField();


}
