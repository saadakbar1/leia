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

package com.grookage.leia.provider;

import com.grookage.leia.provider.exceptions.RefresherErrorCode;
import com.grookage.leia.provider.exceptions.RefresherException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Slf4j
@AllArgsConstructor
public class TimeBasedDataProvider<T> implements DataProvider<T> {
    private final AtomicReference<T> dataReference;
    private final AtomicLong lastUpdatedTimestamp;
    private final Supplier<T> dataSupplier;
    private final ScheduledExecutorService executorService;
    private final T initialDefaultValue;
    private final int delay;
    private final TimeUnit timeUnit;
    private final TimeBasedDataProvider<T>.Updater updater;
    private final String supplierName;
    private final BiFunction<T, T, Boolean> shouldUpdate;

    public TimeBasedDataProvider(Supplier<T> dataSupplier, int delay, TimeUnit timeUnit) {
        this(dataSupplier, null, delay, timeUnit);
    }

    public TimeBasedDataProvider(Supplier<T> dataSupplier, T initialDefaultValue, int delay, TimeUnit timeUnit) {
        this(dataSupplier, initialDefaultValue, delay, timeUnit, (t1, t2) -> true);
    }

    public TimeBasedDataProvider(Supplier<T> dataSupplier, T initialDefaultValue, int delay, TimeUnit timeUnit, BiFunction<T, T, Boolean> shouldUpdate) {
        this.dataSupplier = dataSupplier;
        this.initialDefaultValue = initialDefaultValue;
        this.delay = delay;
        this.timeUnit = timeUnit;
        this.dataReference = new AtomicReference<>();
        this.supplierName = dataSupplier.getClass().getSimpleName();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.updater = new Updater();
        this.lastUpdatedTimestamp = new AtomicLong(0L);
        this.shouldUpdate = shouldUpdate;
    }

    public void start() {
        this.executorService.scheduleWithFixedDelay(this.updater, this.delay, this.delay, this.timeUnit);
        refreshData(()->{
            throw RefresherException.error(RefresherErrorCode.REFRESH_FAILED);
        });
    }

    public void stop() {
        this.executorService.shutdown();
    }

    public T getData() {
        T data = this.dataReference.get();
        return data == null ? this.initialDefaultValue : data;
    }

    public long getLastSuccessfullyUpdatedTimestamp() {
        return this.lastUpdatedTimestamp.get();
    }

    public void update() {
        this.updater.run();
    }

    public class Updater implements Runnable {
        public Updater() {
            //NOOP
        }

        public void run() {
            refreshData(() -> {});
        }
    }

    private void refreshData(Runnable failureHandler) {
        try {
            T data = dataSupplier.get();
            if (data != null) {
                if (Boolean.TRUE.equals(shouldUpdate.apply(dataReference.get(), data))) {
                    dataReference.set(data);
                    lastUpdatedTimestamp.set(System.currentTimeMillis());
                    log.info("[LeiaRefresher.update] Successfully Updated data reference for {}..", supplierName);
                } else {
                    log.info("[LeiaRefresher.update] Failed because shouldUpdate returned false, supplierName: {}",
                             supplierName);
                }
            } else {
                log.warn(dataReference.get() == null
                         ? "[LeiaRefresher.update] Data Update Unsuccessful. Existing value will be returned for {}.."
                         : "[LeiaRefresher.update] Data Update Unsuccessful. Skipped updating data reference for {}..",
                         supplierName);
               failureHandler.run();
            }
        } catch (Exception e) {
            log.error("[LeiaRefresher.update] Error while getting data from data Supplier " + supplierName, e);
            failureHandler.run();
        }
    }

}
