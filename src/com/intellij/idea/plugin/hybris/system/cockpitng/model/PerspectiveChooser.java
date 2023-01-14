// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/perspectiveChooser

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/perspectiveChooser:perspective-chooserElemType interface.
 */
public interface PerspectiveChooser extends DomElement {

    /**
     * Returns the value of the defaultPerspective child.
     *
     * @return the value of the defaultPerspective child.
     */
    @NotNull
    @SubTag("defaultPerspective")
    DefaultPerspective getDefaultPerspective();


    /**
     * Returns the list of authority children.
     *
     * @return the list of authority children.
     */
    @NotNull
    @SubTagList("authority")
    java.util.List<Authority> getAuthorities();

    /**
     * Adds new child to the list of authority children.
     *
     * @return created child
     */
    @SubTagList("authority")
    Authority addAuthority();


    /**
     * Returns the list of format children.
     *
     * @return the list of format children.
     */
    @NotNull
    @SubTagList("format")
    java.util.List<Format> getFormats();

    /**
     * Adds new child to the list of format children.
     *
     * @return created child
     */
    @SubTagList("format")
    Format addFormat();


}
