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

package com.intellij.idea.plugin.hybris.system.type.meta.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CaseInsensitive {

    private CaseInsensitive() {
    }

    public static class NoCaseMultiMap<V> {

        private final CaseInsensitiveConcurrentHashMap<String, Collection<V>> myMap = new CaseInsensitiveConcurrentHashMap<>();

        public void putValue(final @NotNull String key, final @NotNull V value) {
            myMap
                .computeIfAbsent(key, s -> new LinkedList<>())
                .add(value);
        }

        public Set<Map.Entry<String, Collection<V>>> entrySet() {
            return myMap.entrySet();
        }

        @NotNull
        public Collection<V> values() {
            return myMap.values().parallelStream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        }

        public void putAllValues(@NotNull final NoCaseMultiMap<V> map) {
            map.entrySet().forEach(entry ->
                myMap
                    .computeIfAbsent(entry.getKey(), s -> new LinkedList<>())
                    .addAll(entry.getValue())
            );
        }

        @Nullable
        public Collection<V> get(final @NotNull String key) {
            return myMap.get(key);
        }

        public void clear() {
            myMap.clear();
        }

    }

    public static class CaseInsensitiveConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

        @Serial
        private static final long serialVersionUID = 4394959693646791943L;
        private final transient Object nullKey = new Object();

        @Override
        public V get(final Object key) {
            return super.get(convertKey(key));
        }

        @Override
        public void putAll(final Map<? extends K, ? extends V> map) {
            for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }

        @Override
        public V put(@NotNull final K key, @NotNull final V value) {
            return super.put(convertKey(key), value);
        }

        @Override
        public V putIfAbsent(final K key, final V value) {
            return super.putIfAbsent(convertKey(key), value);
        }

        @Override
        public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
            return super.computeIfAbsent(convertKey(key), mappingFunction);
        }

        @Override
        public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return super.computeIfPresent(convertKey(key), remappingFunction);
        }

        @Override
        public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return super.compute(convertKey(key), remappingFunction);
        }

        @SuppressWarnings("unchecked")
        protected <T> T convertKey(final Object key) {
            if (key != null) {
                return (T) key.toString().toLowerCase();
            }
            return (T) nullKey;
        }
    }

}
