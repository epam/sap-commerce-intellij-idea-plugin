/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.cockpitNG.model.core;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:SocketEvents interface.
 * <pre>
 * <h3>Type null:SocketEvents documentation</h3>
 * Groups socket events
 * </pre>
 */
public interface SocketEvents extends DomElement {

    /**
     * Returns the list of socket-event children.
     *
     * @return the list of socket-event children.
     */
    @NotNull
    @SubTagList("socket-event")
    java.util.List<SocketEvent> getSocketEvents();

    /**
     * Adds new child to the list of socket-event children.
     *
     * @return created child
     */
    @SubTagList("socket-event")
    SocketEvent addSocketEvent();


}
