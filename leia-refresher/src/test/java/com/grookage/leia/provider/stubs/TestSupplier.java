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

package com.grookage.leia.provider.stubs;

import com.grookage.leia.provider.suppliers.LeiaSupplier;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor
public class TestSupplier implements LeiaSupplier<TestDetails> {

    private static final AtomicReference<Boolean> testReference = new AtomicReference<>(true);


    public boolean referenceUnset() {
        return !testReference.get();
    }

    public void unmark() {
        testReference.set(false);
    }

    public void mark() {
        testReference.set(true);
    }

    @Override
    public void start() {
        //NOOP
    }

    @Override
    public void stop() {
        //NOOP
    }

    @Override
    public TestDetails get() {
        return testReference.get() ?
                TestDetails.getTestableDetails() :
                TestDetails.getTestableDetailsDefault();
    }
}
