/*
 * Copyright (C) 2018 Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.sli.adaptors.netbox.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

public class Status {

    private Integer value;
    private String label;

    public Integer getValue() {
        return value;
    }

    public void setValue(final Integer value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    public enum Values {
        @SerializedName("1")
        ACTIVE(1, "Active"),
        @SerializedName("2")
        RESERVED(2, "Reserved");

        private final int value;
        private final String label;

        Values(final int value, final String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public Status getStatus() {
            final Status status = new Status();
            status.setValue(value);
            status.setLabel(label);
            return status;
        }

    }
}
