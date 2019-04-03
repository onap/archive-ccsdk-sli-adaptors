package org.onap.ccsdk.sli.adaptors.resource.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import java.util.Collections;
import org.onap.ccsdk.sli.adaptors.lock.comp.LockHelperImpl;
import org.onap.ccsdk.sli.adaptors.lock.dao.ResourceLockDaoImpl;
import org.onap.ccsdk.sli.adaptors.ra.ResourceAllocator;
import org.onap.ccsdk.sli.adaptors.ra.ResourceLockNode;
import org.onap.ccsdk.sli.adaptors.ra.alloc.DbAllocationRule;
import org.onap.ccsdk.sli.adaptors.ra.comp.EndPointAllocatorImpl;
import org.onap.ccsdk.sli.adaptors.ra.rule.dao.RangeRuleDaoImpl;
import org.onap.ccsdk.sli.adaptors.ra.rule.dao.ResourceRuleDaoImpl;
import org.onap.ccsdk.sli.adaptors.rm.comp.ResourceManagerImpl;
import org.onap.ccsdk.sli.adaptors.rm.dao.jdbc.AllocationItemJdbcDaoImpl;
import org.onap.ccsdk.sli.adaptors.rm.dao.jdbc.ResourceDaoImpl;
import org.onap.ccsdk.sli.adaptors.rm.dao.jdbc.ResourceJdbcDaoImpl;
import org.onap.ccsdk.sli.adaptors.rm.dao.jdbc.ResourceLoadJdbcDaoImpl;
import org.onap.ccsdk.sli.adaptors.util.db.CachedDataSourceWrap;
import org.onap.ccsdk.sli.adaptors.util.db.DataSourceWrap;
import org.onap.ccsdk.sli.adaptors.util.speed.SpeedUtil;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.springframework.jdbc.core.JdbcTemplate;

public class ResourceModule extends AbstractLightyModule {

    private final DbLibService dbLibService;

    private DataSourceWrap dataSourceWrap;
    private CachedDataSourceWrap cachedDataSourceWrap;
    private JdbcTemplate rmJdbcTemplate;
    private JdbcTemplate lockJdbcTemplate;
    private ResourceLockDaoImpl resourceLockDao;
    private LockHelperImpl lockHelper;
    private ResourceJdbcDaoImpl resourceJdbcDao;
    private AllocationItemJdbcDaoImpl allocationItemJdbcDao;
    private ResourceLoadJdbcDaoImpl resourceLoadJdbcDao;
    private ResourceDaoImpl resourceDao;
    private ResourceManagerImpl resourceManager;
    private ResourceRuleDaoImpl resourceRuleDao;
    private RangeRuleDaoImpl rangeRuleDao;
    private ResourceAllocator resourceAllocator;
    private ResourceLockNode resourceLockNode;
    private SpeedUtil speedUtil;
    private EndPointAllocatorImpl endPointAllocator;
    private DbAllocationRule dbAllocationRule;

    public ResourceModule(final DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    @Override
    protected boolean initProcedure() {
        this.dataSourceWrap = new DataSourceWrap();
        this.dataSourceWrap.setDataSource(dbLibService);

        this.cachedDataSourceWrap = new CachedDataSourceWrap();
        this.cachedDataSourceWrap.setDataSource(dataSourceWrap);

        this.rmJdbcTemplate = new JdbcTemplate();
        this.rmJdbcTemplate.setDataSource(dataSourceWrap);

        this.lockJdbcTemplate = new JdbcTemplate();
        this.lockJdbcTemplate.setDataSource(cachedDataSourceWrap);

        this.resourceLockDao = new ResourceLockDaoImpl();
        this.resourceLockDao.setJdbcTemplate(lockJdbcTemplate);

        this.lockHelper = new LockHelperImpl();
        this.lockHelper.setResourceLockDao(resourceLockDao);
        this.lockHelper.setRetryCount(10);
        this.lockHelper.setLockWait(5);

        this.resourceJdbcDao = new ResourceJdbcDaoImpl();
        this.resourceJdbcDao.setJdbcTemplate(rmJdbcTemplate);

        this.allocationItemJdbcDao = new AllocationItemJdbcDaoImpl();
        this.allocationItemJdbcDao.setJdbcTemplate(rmJdbcTemplate);

        this.resourceLoadJdbcDao = new ResourceLoadJdbcDaoImpl();
        this.resourceLoadJdbcDao.setJdbcTemplate(rmJdbcTemplate);

        this.resourceDao = new ResourceDaoImpl();
        this.resourceDao.setResourceJdbcDao(resourceJdbcDao);
        this.resourceDao.setAllocationItemJdbcDao(allocationItemJdbcDao);
        this.resourceDao.setResourceLoadJdbcDao(resourceLoadJdbcDao);

        this.resourceManager = new ResourceManagerImpl();
        this.resourceManager.setLockHelper(lockHelper);
        this.resourceManager.setResourceDao(resourceDao);
        this.resourceManager.setLockTimeout(600);

        this.resourceRuleDao = new ResourceRuleDaoImpl();
        this.resourceRuleDao.setJdbcTemplate(rmJdbcTemplate);

        this.rangeRuleDao = new RangeRuleDaoImpl();
        this.rangeRuleDao.setJdbcTemplate(rmJdbcTemplate);

        this.resourceLockNode = new ResourceLockNode();
        this.resourceLockNode.setLockHelper(lockHelper);

        this.speedUtil = new SpeedUtil();

        this.dbAllocationRule = new DbAllocationRule();
        this.dbAllocationRule.setResourceRuleDao(resourceRuleDao);
        this.dbAllocationRule.setRangeRuleDao(rangeRuleDao);

        this.endPointAllocator = new EndPointAllocatorImpl();
        this.endPointAllocator.setResourceManager(resourceManager);
        this.endPointAllocator.setAllocationRuleMap(
                Collections.singletonMap("DEFAULT", Collections.singletonList(dbAllocationRule)));

        this.resourceAllocator = new ResourceAllocator();
        this.resourceAllocator.setResourceManager(resourceManager);
        this.resourceAllocator.setEndPointAllocator(endPointAllocator);
        this.resourceAllocator.setSpeedUtil(speedUtil);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public DataSourceWrap getDataSourceWrap() {
        return dataSourceWrap;
    }

    public CachedDataSourceWrap getCachedDataSourceWrap() {
        return cachedDataSourceWrap;
    }

    public JdbcTemplate getRmJdbcTemplate() {
        return rmJdbcTemplate;
    }

    public JdbcTemplate getLockJdbcTemplate() {
        return lockJdbcTemplate;
    }

    public ResourceLockDaoImpl getResourceLockDao() {
        return resourceLockDao;
    }

    public LockHelperImpl getLockHelper() {
        return lockHelper;
    }

    public ResourceJdbcDaoImpl getResourceJdbcDao() {
        return resourceJdbcDao;
    }

    public AllocationItemJdbcDaoImpl getAllocationItemJdbcDao() {
        return allocationItemJdbcDao;
    }

    public ResourceLoadJdbcDaoImpl getResourceLoadJdbcDao() {
        return resourceLoadJdbcDao;
    }

    public ResourceDaoImpl getResourceDao() {
        return resourceDao;
    }

    public ResourceManagerImpl getResourceManager() {
        return resourceManager;
    }

    public ResourceRuleDaoImpl getResourceRuleDao() {
        return resourceRuleDao;
    }

    public RangeRuleDaoImpl getRangeRuleDao() {
        return rangeRuleDao;
    }

    public ResourceAllocator getResourceAllocator() {
        return resourceAllocator;
    }

    public ResourceLockNode getResourceLockNode() {
        return resourceLockNode;
    }

    public SpeedUtil getSpeedUtil() {
        return speedUtil;
    }

    public EndPointAllocatorImpl getEndPointAllocator() {
        return endPointAllocator;
    }

    public DbAllocationRule getDbAllocationRule() {
        return dbAllocationRule;
    }
}
