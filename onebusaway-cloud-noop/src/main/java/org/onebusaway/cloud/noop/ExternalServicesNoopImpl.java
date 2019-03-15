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
package org.onebusaway.cloud.noop;

import org.onebusaway.cloud.api.ExternalResult;
import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.InputStreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalServicesNoopImpl implements ExternalServices {
    private final Logger _log = LoggerFactory.getLogger(ExternalServicesNoopImpl.class);

    @Override
    public ExternalResult publishMessage(String topic, String messageContents) {
        _log.info("publishMessage({" + topic + "}, {" + messageContents + "})");
        return new AlwaysTrueExternalResult();
    }

    @Override
    public ExternalResult publishMetric(String topic, String metricName, String dimensionName, String dimensionValue, double value) {
        _log.info("publishMetric({" + topic + ":" + metricName + "}, {"
                + dimensionName + "=" + dimensionValue +"}, {" + value + "})");
        return new AlwaysTrueExternalResult();
    }

    @Override
    public ExternalResult getFileAsStream(String url, InputStreamConsumer callback, String profile) {
        _log.info("getFileAsStream({" + url + "}, " + profile + " }");
        return new AlwaysTrueExternalResult();
    }

    @Override
    public ExternalResult getFileAsStream(String url, InputStreamConsumer callback) {
        _log.info("getFileAsStream({" + url + " }");
        return new AlwaysTrueExternalResult();
    }

    @Override
    public boolean isInstancePrimary() {
        return true;
    }
}