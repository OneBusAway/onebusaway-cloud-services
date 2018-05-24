/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.cloud.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ExternalServicesBridgeFactory {

    private final Logger _log = LoggerFactory.getLogger(ExternalServicesBridgeFactory.class);
    public static final String AWS_KEY = "oba.cloud.aws";
    private static final String NOOP_FACTORY = "org.onebusaway.cloud.noop.ExternalServicesNoopFactory";
    private static final String AWS_FACTORY = "org.onebusaway.cloud.aws.ExternalServicesAwsFactory";


    public ExternalServices getExternalServices() {
        if (getPropertySet(AWS_KEY)) {
            ExternalServices es = instantiate(AWS_FACTORY);
            if (es != null) {
                return es;
            }
        }
        return instantiate(NOOP_FACTORY);
    }

    private boolean getPropertySet(String key) {
        return "true".equalsIgnoreCase(System.getProperty(key));
    }

    private ExternalServices instantiate(String className) {
        Class factoryClass = null;
        try {
            factoryClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            reportException(className, e);
            return null;
        }
        Constructor factoryConstructor = null;
        try {
            factoryConstructor = factoryClass.getConstructor(null);
        } catch (NoSuchMethodException e) {
            reportException(className, e);
            return null;
        }

        Object obj = null;
        try {
            obj = factoryConstructor.newInstance(null);
        } catch (InstantiationException e) {
            reportException(className, e);
            return null;
        } catch (IllegalAccessException e) {
            reportException(className, e);
            return null;
        } catch (InvocationTargetException e) {
            reportException(className, e);
            return null;
        }

        ExternalServicesFactory esf = (ExternalServicesFactory) obj;
        return esf.getExternalServices();
    }

    private void reportException(String className, Exception e) {
        _log.info("Exception instantiating " + className, e);
    }

}
