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

package com.grookage.leia.provider.exceptions;

import lombok.Getter;

@Getter
public enum RefresherErrorCode {


    BAD_REQUEST(400),

    INTERNAL_SERVER_ERROR(500),
    INTERNAL_ERROR(500);

    final int status;

    RefresherErrorCode(int status) {
        this.status = status;
    }
}
