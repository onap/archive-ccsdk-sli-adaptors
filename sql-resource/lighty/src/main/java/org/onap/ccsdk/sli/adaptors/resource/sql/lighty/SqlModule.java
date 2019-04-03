package org.onap.ccsdk.sli.adaptors.resource.sql.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResourcePropertiesProviderImpl;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

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

    public SqlResourcePropertiesProviderImpl getSqlPropertiesProvider() {
        return this.sqlPropertiesProvider;
    }

    public SqlResource getSqlResource() {
        return sqlResource;
    }
}
