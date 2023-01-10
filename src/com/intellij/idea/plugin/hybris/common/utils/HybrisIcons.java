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

package com.intellij.idea.plugin.hybris.common.utils;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ReflectionUtil;
import icons.DvcsImplIcons;

import javax.swing.*;

/**
 * Created 1:54 AM 08 June 2015
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public final class HybrisIcons {

    private HybrisIcons() {
        // not allowed
    }

    public static final Icon IMPEX_FILE = getIcon("/icons/fileTypes/impexFile.svg");
    public static final Icon FS_FILE = getIcon("/icons/fileTypes/flexibleSearchFileIcon.svg");
    public static final Icon BEAN_FILE = getIcon("/icons/beanIcon.svg");
    
    public static final Icon HYBRIS = getIcon("/icons/hybrisIcon.svg");
    public static final Icon EXTENSION_INFO = getIcon("/icons/extensionInfo.svg");
    public static final Icon BUSINESS_PROCESS = getIcon("/icons/businessProcess.svg");
    public static final Icon HYBRIS_REMOTE = getIcon("/icons/hybrisRemoteIcon.svg");
    
    public static final Icon WAIT = getIcon("/icons/waitIcon.svg");
    public static final Icon END = getIcon("/icons/endIcon.svg");
    public static final Icon NOTIFY = getIcon("/icons/notifyIcon.svg");
    public static final Icon ACTION = getIcon("/icons/actionIcon.svg");
    public static final Icon SPLIT = getIcon("/icons/splitIcon.svg");
    public static final Icon JOIN = getIcon("/icons/joinIcon.svg");
    public static final Icon SCRIPT = getIcon("/icons/scriptIcon.svg");

    public static final Icon TYPE_SYSTEM = getIcon("/icons/typeSystem.svg");
    public static final Icon ENUM = AllIcons.Nodes.Enum;
    public static final Icon ENUM_VALUE = AllIcons.Nodes.AnonymousClass;
    public static final Icon BEAN = getIcon("/icons/beanIcon.svg");
    public static final Icon SPRING_BEAN = getIcon("icons/springBean.svg");
    public static final Icon LOCALIZED = getIcon("/icons/localized.svg");

    public static final Icon MACROS = getIcon("/icons/macros.png");

    public static final Icon ATOMIC = AllIcons.Actions.GroupByModule;
    public static final Icon ITEM = AllIcons.Nodes.Class;
    public static final Icon MAP = AllIcons.Nodes.Plugin;
    public static final Icon RELATION = AllIcons.Actions.GroupByModuleGroup;
    public static final Icon IMPORT = AllIcons.ToolbarDecorator.Import;
    public static final Icon ANNOTATION = AllIcons.Nodes.Annotationtype;
    public static final Icon PROPERTY = AllIcons.Nodes.Property;
    public static final Icon ATTRIBUTE = getIcon("/icons/attribute.svg");
    public static final Icon RELATION_SOURCE = DvcsImplIcons.Outgoing;
    public static final Icon RELATION_TARGET = DvcsImplIcons.Incoming;
    public static final Icon COLLECTION = getIcon("/icons/collection.svg");
    public static final Icon INDEX = getIcon("/icons/index.svg");
    public static final Icon INDEX_UNIQUE = getIcon("/icons/indexUnique.svg");
    public static final Icon INDEX_FUN = getIcon("/icons/indexFun.svg");
    public static final Icon INDEX_CLUSTER = getIcon("/icons/indexCluster.svg");

    public static final Icon GROUP_BY_BEAN_DTO = getIcon("/icons/groupByBeanDto.svg");
    public static final Icon GROUP_BY_BEAN_EVENT = getIcon("/icons/groupByBeanEvent.svg");
    public static final Icon GROUP_BY_BEAN_WS = getIcon("/icons/groupByBeanWS.svg");

    public static final Icon GUTTER_POPULATOR = getIcon("/icons/gutter/populator.svg");

    public static final Icon CONSOLE_SOLR = getIcon("/icons/console/solr.svg");

    static Icon getIcon(final String path) {
        final Class<?> callerClass = ReflectionUtil.getGrandCallerClass();
        assert callerClass != null : path;
        return IconLoader.getIcon(path, callerClass);
    }
}
