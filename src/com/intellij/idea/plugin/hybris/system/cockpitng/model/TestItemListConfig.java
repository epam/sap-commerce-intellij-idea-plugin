// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/test

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/test:testItemListConfig interface.
 */
public interface TestItemListConfig extends DomElement {

    /**
     * Returns the list of properties children.
     *
     * @return the list of properties children.
     */
    @NotNull
    @SubTagList("properties")
    java.util.List<Property> getPropertieses();

    /**
     * Adds new child to the list of properties children.
     *
     * @return created child
     */
    @SubTagList("properties")
    Property addProperties();


}
