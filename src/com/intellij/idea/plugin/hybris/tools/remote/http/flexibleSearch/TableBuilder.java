/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.tools.remote.http.flexibleSearch;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TableBuilder {

    private final List<String[]> rows = new LinkedList<>();

    public void addHeaders(final Collection<String> headers) {
        addRow(headers);
    }

    public void addRow(final Collection<String> cols) {
        rows.add(cols.toArray(new String[]{}));
    }

    private int[] colWidths() {
        int cols = -1;

        for (String[] row : rows)
            cols = Math.max(cols, row.length);

        final int[] widths = new int[cols];

        for (String[] row : rows) {
            for (int colNum = 0; colNum < row.length; colNum++) {
                widths[colNum] =
                    Math.max(
                        widths[colNum],
                        StringUtils.length(row[colNum])
                    );
            }
        }

        return widths;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();

        final int[] colWidths = colWidths();

        for (final String[] row : rows) {
            for (int colNum = 0; colNum < row.length; colNum++) {
                final var cellValue = StringUtils.defaultString(row[colNum])
                    .replace("&quot;", "\"");
                buf.append(
                    StringUtils.rightPad(
                        cellValue, colWidths[colNum]
                    )
                );

                if (colNum < row.length - 1) {
                    buf.append(HybrisConstants.FXS_TABLE_RESULT_SEPARATOR)
                        .append(' ');
                }
            }

            buf.append('\n');
        }

        return buf.toString();
    }

}
