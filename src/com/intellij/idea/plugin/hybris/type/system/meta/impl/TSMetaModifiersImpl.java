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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModifiers;
import com.intellij.idea.plugin.hybris.type.system.model.Modifiers;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TSMetaModifiersImpl<T extends TSMetaClassifier<?>> extends TSMetaEntityImpl<Modifiers> implements TSMetaModifiers<T> {

    private final boolean myRead;
    private final boolean myWrite;
    private final boolean mySearch;
    private final boolean myOptional;
    private final boolean myPrivate;
    private final boolean myInitial;
    private final boolean myRemovable;
    private final boolean myPartOf;
    private final boolean myUnique;
    private final boolean myDoNotOptimize;
    private final boolean myEncrypted;

    public TSMetaModifiersImpl(final Module module, final Project project, final @NotNull Modifiers dom, final boolean custom) {
        super(module, project, null, dom, custom);
        myRead = Boolean.TRUE.equals(dom.getRead().getValue());
        myWrite = Boolean.TRUE.equals(dom.getWrite().getValue());
        mySearch = Boolean.TRUE.equals(dom.getSearch().getValue());
        myOptional = Boolean.TRUE.equals(dom.getOptional().getValue());
        myPrivate = Boolean.TRUE.equals(dom.getPrivate().getValue());
        myInitial = Boolean.TRUE.equals(dom.getInitial().getValue());
        myRemovable = Boolean.TRUE.equals(dom.getRemovable().getValue());
        myPartOf = Boolean.TRUE.equals(dom.getPartOf().getValue());
        myUnique = Boolean.TRUE.equals(dom.getUnique().getValue());
        myDoNotOptimize = Boolean.TRUE.equals(dom.getDoNotOptimize().getValue());
        myEncrypted = Boolean.TRUE.equals(dom.getEncrypted().getValue());
    }

    @Override
    public boolean isRead() {
        return myRead;
    }

    @Override
    public boolean isWrite() {
        return myWrite;
    }

    @Override
    public boolean isSearch() {
        return mySearch;
    }

    @Override
    public boolean isOptional() {
        return myOptional;
    }

    @Override
    public boolean isPrivate() {
        return myPrivate;
    }

    @Override
    public boolean isInitial() {
        return myInitial;
    }

    @Override
    public boolean isRemovable() {
        return myRemovable;
    }

    @Override
    public boolean isPartOf() {
        return myPartOf;
    }

    @Override
    public boolean isUnique() {
        return myUnique;
    }

    @Override
    public boolean isDoNotOptimize() {
        return myDoNotOptimize;
    }

    @Override
    public boolean isEncrypted() {
        return myEncrypted;
    }

}
