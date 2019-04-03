/*
 * ============LICENSE_START==========================================
 * Copyright (c) 2019 PANTHEON.tech s.r.o.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 */
package org.onap.ccsdk.sli.adaptors.resource.sql.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResourcePropertiesProviderImpl;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the sql-resource-provider artifact.
 */
public class SqlModule extends AbstractLightyModule {

    private final DbLibService dbService;

    private SqlResourcePropertiesProviderImpl sqlPropertiesProvider;
    private SqlResource sqlResource;

    public SqlModule(DbLibService dbService) {
        this.dbService = dbService;
    }

    @Override
    protected boolean initProcedure() {
        this.sqlPropertiesProvider = new SqlResourcePropertiesProviderImpl();
        this.sqlResource = new SqlResource(sqlPropertiesProvider, dbService);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public SqlResource getSqlResource() {
        return sqlResource;
    }
}
