// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:socketsElemType interface.
 */
public interface Sockets extends DomElement {

    /**
     * Returns the list of input children.
     *
     * @return the list of input children.
     */
    @NotNull
    @SubTagList("input")
    java.util.List<Socket> getInputs();

    /**
     * Adds new child to the list of input children.
     *
     * @return created child
     */
    @SubTagList("input")
    Socket addInput();


    /**
     * Returns the list of output children.
     *
     * @return the list of output children.
     */
    @NotNull
    @SubTagList("output")
    java.util.List<Socket> getOutputs();

    /**
     * Adds new child to the list of output children.
     *
     * @return created child
     */
    @SubTagList("output")
    Socket addOutput();


    /**
     * Returns the list of forward children.
     *
     * @return the list of forward children.
     */
    @NotNull
    @SubTagList("forward")
    java.util.List<Forward> getForwards();

    /**
     * Adds new child to the list of forward children.
     *
     * @return created child
     */
    @SubTagList("forward")
    Forward addForward();


}
