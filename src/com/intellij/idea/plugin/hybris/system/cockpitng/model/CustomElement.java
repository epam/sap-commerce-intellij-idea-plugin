// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:customElement interface.
 */
public interface CustomElement extends DomElement, AbstractPositioned {


    /**
     * Returns the value of the default child.
     * <pre>
     * <h3>Element http://www.hybris.com/cockpitng/component/editorArea:default documentation</h3>
     * Defines default element definition - if no specific definition exists for
     * 								current locale, then default is used. Definition may contain localization properties in
     * 								[] braces and/or SpEL expressions in {} braces.
     * </pre>
     *
     * @return the value of the default child.
     */
    @NotNull
    @SubTag("default")
    @Required
    GenericDomValue<String> getDefault();


    /**
     * Returns the list of locale children.
     * <pre>
     * <h3>Element http://www.hybris.com/cockpitng/component/editorArea:locale documentation</h3>
     * Defines element definition for specified locale. Definition may contain
     * 								localization properties in [] braces and/or SpEL expressions in {} braces.
     * </pre>
     *
     * @return the list of locale children.
     */
    @NotNull
    @SubTagList("locale")
    java.util.List<LocaleCustomElement> getLocales();

    /**
     * Adds new child to the list of locale children.
     *
     * @return created child
     */
    @SubTagList("locale")
    LocaleCustomElement addLocale();


}
