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

package com.intellij.idea.plugin.hybris.impex.tableFormatting.actions;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Aleksandr Nosov <nosovae.dev@gmail.com>
 */
public abstract class AbstractImpexTableFormatAction extends EditorAction {

    AbstractImpexTableFormatAction(EditorActionHandler defaultHandler) {
        super(defaultHandler);
    }

    @Override
    public void update(final AnActionEvent event) {
        super.update(event);

        if (ActionPlaces.isPopupPlace(event.getPlace())) {
            event.getPresentation().setVisible(event.getPresentation().isEnabled());
        }
    }

    @Override
    public void update(
        final Editor editor, final Presentation presentation, final DataContext dataContext
    ) {
        super.update(editor, presentation, dataContext);
        final VirtualFile file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE);
        final boolean enabled = file != null && file.getName().endsWith(HybrisConstants.HYBRIS_IMPEX_XML_FILE_ENDING);
        presentation.setEnabledAndVisible(enabled);
    }
}
