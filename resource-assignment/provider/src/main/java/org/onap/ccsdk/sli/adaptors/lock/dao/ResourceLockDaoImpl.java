/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.lock.dao;

import java.util.Date;
import java.util.List;
import org.onap.ccsdk.sli.adaptors.lock.data.ResourceLock;
import org.onap.ccsdk.sli.adaptors.util.db.CachedDataSourceWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class ResourceLockDaoImpl implements ResourceLockDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ResourceLockDaoImpl.class);

    private JdbcTemplate jdbcTemplate;

    @Override
    public void add(ResourceLock l) {
        jdbcTemplate.update(
                "INSERT INTO RESOURCE_LOCK (resource_name, lock_holder, lock_count, lock_time, expiration_time)\n" +
                        "VALUES (?, ?, ?, ?, ?)",
                        new Object[] { l.resourceName, l.lockHolder, l.lockCount, l.lockTime, l.expirationTime });
    }

    @Override
    public void update(long id, String lockHolder, Date lockTime, Date expirationTime, int lockCount) {
        jdbcTemplate.update(
                "UPDATE RESOURCE_LOCK SET lock_holder = ?, lock_time = ?, expiration_time = ?, lock_count = ? WHERE resource_lock_id = ?",
                new Object[] { lockHolder, lockTime, expirationTime, lockCount, id });
    }

    @Override
    public ResourceLock getByResourceName(String resourceName) {
        List<ResourceLock> ll = jdbcTemplate.query("SELECT * FROM RESOURCE_LOCK WHERE resource_name = ?",
                new Object[] { resourceName }, (RowMapper<ResourceLock>) (rs, rowNum) -> {
                    ResourceLock rl = new ResourceLock();
                    rl.id = rs.getLong("resource_lock_id");
                    rl.resourceName = rs.getString("resource_name");
                    rl.lockHolder = rs.getString("lock_holder");
                    rl.lockCount = rs.getInt("lock_count");
                    rl.lockTime = rs.getTimestamp("lock_time");
                    rl.expirationTime = rs.getTimestamp("expiration_time");
                    return rl;
                });
        return ll != null && !ll.isEmpty() ? ll.get(0) : null;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM RESOURCE_LOCK WHERE resource_lock_id = ?", new Object[] { id });
    }

    @Override
    public void decrementLockCount(long id) {
        jdbcTemplate.update("UPDATE RESOURCE_LOCK SET lock_count = lock_count - 1 WHERE resource_lock_id = ?",
                new Object[] { id });
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void commit() {
        if (jdbcTemplate.getDataSource() instanceof CachedDataSourceWrap) {
            CachedDataSourceWrap ds = (CachedDataSourceWrap) jdbcTemplate.getDataSource();
            ds.commit();
            ds.releaseConnection();
        }
    }

    @Override
    public void rollback() {
        if (jdbcTemplate.getDataSource() instanceof CachedDataSourceWrap) {
            CachedDataSourceWrap ds = (CachedDataSourceWrap) jdbcTemplate.getDataSource();
            ds.rollback();
            ds.releaseConnection();
        }
    }
}
