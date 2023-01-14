// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/availableLocales

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/availableLocales:cockpit-localesElemType interface.
 */
public interface CockpitLocales extends DomElement {

    /**
     * Returns the list of cockpit-locale children.
     *
     * @return the list of cockpit-locale children.
     */
    @NotNull
    @SubTagList("cockpit-locale")
    java.util.List<CockpitLocale> getCockpitLocales();

    /**
     * Adds new child to the list of cockpit-locale children.
     *
     * @return created child
     */
    @SubTagList("cockpit-locale")
    CockpitLocale addCockpitLocale();


}
