// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:virtual-socketsElemType interface.
 */
public interface VirtualSockets extends DomElement {

    /**
     * Returns the list of input children.
     * <pre>
     * <h3>Element null:input documentation</h3>
     * Input virtual socket
     * </pre>
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
     * <pre>
     * <h3>Element null:output documentation</h3>
     * Output virtual socket
     * </pre>
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


}
