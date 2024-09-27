/*
 * Copyright (c) 2024. Koushik R <rkoushik.14@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grookage.leia.client.datasource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

class NamespaceDataSourceTest {

    @Test
    void testNamespaceDataSource() {
        final var supplier = new Supplier<Set<String>>() {

            private static final AtomicReference<Boolean> supplierMarker = new AtomicReference<>(false);

            @Override
            public Set<String> get() {
                return supplierMarker.get() ?
                       Set.of("testNamespace") : Set.of();
            }

            public void mark() {
                supplierMarker.set(true);
            }
        };
        final var suppliedSource = new NamespaceDataSource(supplier);
        Assertions.assertNotNull(suppliedSource.getNamespaces());
        Assertions.assertTrue(suppliedSource.getNamespaces().isEmpty());

        supplier.mark();
        Assertions.assertNotNull(suppliedSource.getNamespaces());
        Assertions.assertFalse(suppliedSource.getNamespaces().isEmpty());
    }
}
