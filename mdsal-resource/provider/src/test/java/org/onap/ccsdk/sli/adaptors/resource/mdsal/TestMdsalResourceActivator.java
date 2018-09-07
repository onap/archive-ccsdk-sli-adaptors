/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2018 Samsung. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.adaptors.resource.mdsal;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;

public class TestMdsalResourceActivator {

    MdsalResourceActivator mdsal;

    @Before
    public void setup() {
        mdsal = new MdsalResourceActivator();
    }


    @Test (expected = ConfigurationException.class)
    public void testStartResource() throws Exception {
        BundleContext ctx = new BundleContext() {
            @Override
            public String getProperty(String key) {
                return null;
            }

            @Override
            public Bundle getBundle() {
                return null;
            }

            @Override
            public Bundle installBundle(String location, InputStream input) throws BundleException {
                return null;
            }

            @Override
            public Bundle installBundle(String location) throws BundleException {
                return null;
            }

            @Override
            public Bundle getBundle(long id) {
                return null;
            }

            @Override
            public Bundle[] getBundles() {
                return new Bundle[0];
            }

            @Override
            public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {

            }

            @Override
            public void addServiceListener(ServiceListener listener) {

            }

            @Override
            public void removeServiceListener(ServiceListener listener) {

            }

            @Override
            public void addBundleListener(BundleListener listener) {

            }

            @Override
            public void removeBundleListener(BundleListener listener) {

            }

            @Override
            public void addFrameworkListener(FrameworkListener listener) {

            }

            @Override
            public void removeFrameworkListener(FrameworkListener listener) {

            }

            @Override
            public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
                return new ServiceReference[0];
            }

            @Override
            public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
                return new ServiceReference[0];
            }

            @Override
            public ServiceReference<?> getServiceReference(String clazz) {
                return null;
            }

            @Override
            public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
                return null;
            }

            @Override
            public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
                return null;
            }

            @Override
            public <S> S getService(ServiceReference<S> reference) {
                return null;
            }

            @Override
            public boolean ungetService(ServiceReference<?> reference) {
                return false;
            }

            @Override
            public File getDataFile(String filename) {
                return null;
            }

            @Override
            public Filter createFilter(String filter) throws InvalidSyntaxException {
                return null;
            }

            @Override
            public Bundle getBundle(String location) {
                return null;
            }
        };

        mdsal.start(ctx);

    }

    @Test
    public void testStopResource() throws Exception {
        BundleContext ctx = new BundleContext() {
            @Override
            public String getProperty(String key) {
                return null;
            }

            @Override
            public Bundle getBundle() {
                return null;
            }

            @Override
            public Bundle installBundle(String location, InputStream input) throws BundleException {
                return null;
            }

            @Override
            public Bundle installBundle(String location) throws BundleException {
                return null;
            }

            @Override
            public Bundle getBundle(long id) {
                return null;
            }

            @Override
            public Bundle[] getBundles() {
                return new Bundle[0];
            }

            @Override
            public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {

            }

            @Override
            public void addServiceListener(ServiceListener listener) {

            }

            @Override
            public void removeServiceListener(ServiceListener listener) {

            }

            @Override
            public void addBundleListener(BundleListener listener) {

            }

            @Override
            public void removeBundleListener(BundleListener listener) {

            }

            @Override
            public void addFrameworkListener(FrameworkListener listener) {

            }

            @Override
            public void removeFrameworkListener(FrameworkListener listener) {

            }

            @Override
            public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
                return null;
            }

            @Override
            public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
                return new ServiceReference[0];
            }

            @Override
            public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
                return new ServiceReference[0];
            }

            @Override
            public ServiceReference<?> getServiceReference(String clazz) {
                return null;
            }

            @Override
            public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
                return null;
            }

            @Override
            public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
                return null;
            }

            @Override
            public <S> S getService(ServiceReference<S> reference) {
                return null;
            }

            @Override
            public boolean ungetService(ServiceReference<?> reference) {
                return false;
            }

            @Override
            public File getDataFile(String filename) {
                return null;
            }

            @Override
            public Filter createFilter(String filter) throws InvalidSyntaxException {
                return null;
            }

            @Override
            public Bundle getBundle(String location) {
                return null;
            }
        };

        mdsal.stop(ctx);

    }
}
