/*
 * Copyright (c) 2024-2025. Koushik R <rkoushik.14@gmail.com>.
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

package com.grookage.leia.mux.processors.hub;


import com.grookage.leia.models.mux.LeiaMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@SuperBuilder
@Data
public abstract class MessageProcessor {

    private final String name;

    public abstract void processMessages(List<LeiaMessage> messages);

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final var thatKey = (MessageProcessor) obj;
        return (thatKey.getName().equalsIgnoreCase(this.getName()));
    }
}


