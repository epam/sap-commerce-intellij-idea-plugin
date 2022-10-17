/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.type.system.meta.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
class TSMetaEntityImpl<D extends DomElement> {

    private final Module myModule;
    private final Project myProject;
    private final String myName;
    private final boolean myCustom;

    private final DomAnchor<D> myDomAnchor;

    public TSMetaEntityImpl(final Module module, final Project project, final D dom, final boolean custom) {
        this(module, project, null, dom, custom);
    }

    public TSMetaEntityImpl(final Module module, final Project project, final String name, final D dom, final boolean custom) {
        myModule = module;
        myProject = project;
        myDomAnchor = DomService.getInstance().createAnchor(dom);
        myName = name;
        myCustom = custom;
    }

    @Nullable
    public String getName() {
        return myName;
    }

    @Nullable
    public D retrieveDom() {
        return myDomAnchor.retrieveDomElement();
    }

    @NotNull
    public Project getProject() {
        return myProject;
    }

    @NotNull
    public Module getModule() {
        return myModule;
    }

    public boolean isCustom() {
        return myCustom;
    }
}
