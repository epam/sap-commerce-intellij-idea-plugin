/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

// Generated on Wed Jan 18 00:34:54 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model.core;

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
