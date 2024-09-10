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

package com.grookage.leia.provider.refresher;

import com.grookage.leia.provider.TimeBasedDataProvider;
import com.grookage.leia.provider.suppliers.LeiaSupplier;

import java.util.concurrent.TimeUnit;

public abstract class AbstractLeiaRefresher<T> implements LeiaRefresher<T> {

    private final TimeBasedDataProvider<T> dataProvider;

    protected AbstractLeiaRefresher(final LeiaSupplier<T> supplier,
                                    final int configRefreshTimeSeconds) {
        this.dataProvider = new TimeBasedDataProvider<>(
                supplier,
                null,
                configRefreshTimeSeconds,
                TimeUnit.SECONDS
        );
        this.dataProvider.start();
    }

    @Override
    public T getConfiguration() {
        return dataProvider.getData();
    }

    @Override
    public void refresh() {
        dataProvider.update();
    }
}
