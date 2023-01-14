// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/dashboard

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/dashboard:dashboardElemType interface.
 */
public interface Dashboard extends DomElement {

    /**
     * Returns the value of the defaultGridId child.
     *
     * @return the value of the defaultGridId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("defaultGridId")
    GenericAttributeValue<String> getDefaultGridId();


    /**
     * Returns the list of grid children.
     *
     * @return the list of grid children.
     */
    @NotNull
    @SubTagList("grid")
    @Required
    java.util.List<Grid> getGrids();

    /**
     * Adds new child to the list of grid children.
     *
     * @return created child
     */
    @SubTagList("grid")
    Grid addGrid();


}
