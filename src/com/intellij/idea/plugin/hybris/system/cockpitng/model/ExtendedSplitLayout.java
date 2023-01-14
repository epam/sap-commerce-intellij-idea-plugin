// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/extendedsplitlayout

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/extendedsplitlayout:extended-split-layoutElemType interface.
 */
public interface ExtendedSplitLayout extends DomElement {

    /**
     * Returns the value of the defaultLayout child.
     *
     * @return the value of the defaultLayout child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("defaultLayout")
    GenericAttributeValue<String> getDefaultLayout();


    /**
     * Returns the list of layout-mapping children.
     *
     * @return the list of layout-mapping children.
     */
    @NotNull
    @SubTagList("layout-mapping")
    java.util.List<LayoutMapping> getLayoutMappings();

    /**
     * Adds new child to the list of layout-mapping children.
     *
     * @return created child
     */
    @SubTagList("layout-mapping")
    LayoutMapping addLayoutMapping();


}
