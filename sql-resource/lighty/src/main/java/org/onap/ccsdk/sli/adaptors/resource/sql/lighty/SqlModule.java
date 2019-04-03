package org.onap.ccsdk.sli.adaptors.resource.sql.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResourcePropertiesProviderImpl;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

public class SqlModule extends AbstractLightyModule implements LightyModule {

    private final SqlResourcePropertiesProviderImpl sqlPropertiesProvider;
    private final SqlResource sqlResource;

    public SqlModule(DbLibService dbService) {
        this.sqlPropertiesProvider = new SqlResourcePropertiesProviderImpl();
        this.sqlResource = new SqlResource(sqlPropertiesProvider, dbService);
    }

    public SqlResourcePropertiesProviderImpl getSqlPropertiesProvider() {
        return this.sqlPropertiesProvider;
    }

    public SqlResource getSqlResource() {
        return sqlResource;
    }

    @Override
    protected boolean initProcedure() {
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }
}