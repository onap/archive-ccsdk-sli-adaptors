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

import java.util.Objects;

public class IPAddress extends Identifiable {

    private Status.Values status;
    private String address;

    public void setStatus(Status.Values status) {
        this.status = status;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Status.Values getStatus() {
        return status;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPAddress ipAddress = (IPAddress) o;
        return Objects.equals(status, ipAddress.status) &&
            Objects.equals(address, ipAddress.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, address);
    }
}
