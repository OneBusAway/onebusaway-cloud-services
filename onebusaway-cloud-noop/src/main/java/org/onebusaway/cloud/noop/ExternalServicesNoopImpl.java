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

import org.onebusaway.cloud.api.Credential;
import org.onebusaway.cloud.api.ExternalResult;
import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.InputStreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    public ExternalResult publishMetric(Credential credential, String namespace, String metricName, String dimensionName, String dimensionValue, double value) {
        _log.info("publishMetric({" + namespace + ":" + metricName + "}, {"
                + dimensionName + "=" + dimensionValue +"}, {" + value + "})");
        return new AlwaysTrueExternalResult();
    }

    @Override
    public ExternalResult publishMetrics(String topic, List<String> metricNames, List<String> dimensionNames, List<String> dimensionValues, List<Double> values) {
        StringBuilder sb = new StringBuilder();
        sb.append("publishMetrics({");
        sb.append("topic=").append(topic);

        if(metricNames != null) {
            sb.append(", metricNames=[");
            for (String metricName : metricNames) {
                sb.append("metricName").append(",");
            }
            sb.append("],");
        }

        if(values != null){
            sb.append(", values=[");
            for(Double value : values){
                sb.append(value).append(",");
            }
            sb.append("]");
        }

        if(dimensionNames != null){
            sb.append(", dimensionNames=[");
            for(String dnames : dimensionNames){
                sb.append(dnames).append(",");
            }
            sb.append("]");
        }

        if(dimensionValues != null){
            sb.append(", dimensionValues=[");
            for(String dvalues : dimensionValues){
                sb.append(dvalues).append(",");
            }
            sb.append("]");
        }

        sb.append("})");

        _log.info(sb.toString());

        return new AlwaysTrueExternalResult();
    }

    @Override
    public ExternalResult publishMetrics(Credential credential, String namespace, List<String> metricNames, List<String> dimensionNames, List<String> dimensionValues, List<Double> values) {
        return publishMetrics(namespace, metricNames, dimensionNames, dimensionValues, values);
    }


    @Override
    public ExternalResult publishMultiDimensionalMetric(String topic, String metricName, String[] dimensionName, String[] dimensionValue, double value) {
        _log.info("publishMetric({" + topic + ":" + metricName + "}, {"
                + dimensionName + "=" + dimensionValue +"}, {" + value + "})");
        return new AlwaysTrueExternalResult();
    }

    @Override
    public ExternalResult publishMultiDimensionalMetric(Credential credential, String namespace, String metricName, String[] dimensionName, String[] dimensionValue, double value) {
        return publishMultiDimensionalMetric(namespace, metricName, dimensionName, dimensionValue, value);
    }

    @Override
    public ExternalResult getFileAsStream(String url, InputStreamConsumer callback, String profile) {
        _log.info("getFileAsStream({" + url + "}, " + profile + " }");
        return new AlwaysTrueExternalResult();
    }

    @Override
    public ExternalResult getFileAsStream(String url, InputStreamConsumer callback, String profile, String region) {
        _log.info("getFileAsStream({" + url + "}, " + profile + ", " + region +  "}");
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