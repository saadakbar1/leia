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

package com.grookage.leia.models.attributes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.grookage.leia.models.qualifiers.QualifierInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "allocationType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ArrayAttribute.class, name = "ARRAY"),
        @JsonSubTypes.Type(value = BooleanAttribute.class, name = "BOOLEAN"),
        @JsonSubTypes.Type(value = ByteAttribute.class, name = "BYTES"),
        @JsonSubTypes.Type(value = DoubleAttribute.class, name = "DOUBLE"),
        @JsonSubTypes.Type(value = EnumAttribute.class, name = "ENUM"),
        @JsonSubTypes.Type(value = FloatAttribute.class, name = "FLOAT"),
        @JsonSubTypes.Type(value = IntegerAttribute.class, name = "INTEGER"),
        @JsonSubTypes.Type(value = LongAttribute.class, name = "LONG"),
        @JsonSubTypes.Type(value = MapAttribute.class, name = "MAP"),
        @JsonSubTypes.Type(value = ObjectAttribute.class, name = "OBJECT"),
        @JsonSubTypes.Type(value = StringAttribute.class, name = "STRING")
})
public abstract class SchemaAttribute {

    private DataType type;

    @Pattern(regexp = "[A-Za-z0-9]*", message = "Schema name doesn't comply to the naming standard")
    private String name;

    private boolean optional;

    private QualifierInfo qualifierInfo;

    public abstract <T> T accept(SchemaAttributeAcceptor<T> attributeAcceptor);
}
