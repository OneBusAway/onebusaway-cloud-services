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
package org.onebusaway.cloud.aws;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import org.onebusaway.cloud.api.ExternalResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class CloudWatchServices {
    private final Logger _log = LoggerFactory.getLogger(CloudWatchServices.class);

    private AmazonCloudWatch _client;
    public ExternalResult publishMetric(String namespace, String metricName, String dimensionName, String dimensionValue, double value) {
        ArrayList<Dimension> dims = null;

        if (dimensionName != null && dimensionValue != null) {
            dims = new ArrayList<>(1);
            Dimension dim = new Dimension().withName(dimensionName).withValue(dimensionValue);
            dims.add(dim);
        }

        return publishMetric(namespace, metricName, dims, value);
    }

    public ExternalResult publishMultiDimensionalMetric(String namespace, String metricName, String[] dimensionName, String[] dimensionValue, double value) {
        ArrayList<Dimension> dims = null;

        if (dimensionName != null && dimensionValue != null) {
            if (dimensionName.length != dimensionValue.length) {
                throw new IllegalStateException("Input array lengths must match: " + dimensionName + " vs " + dimensionValue);
            }
            dims = new ArrayList<>(1);
            for (int i = 0; i<dimensionName.length; i++) {
                if (dimensionName[i] != null && dimensionValue[i] != null) {
                    Dimension dim = new Dimension().withName(dimensionName[i]).withValue(dimensionValue[i]);
                    dims.add(dim);
                }
            }
        } else if (dimensionName != dimensionValue) { // the should both be null for consistency
            throw new IllegalStateException("Dimension mismatch: name=" + dimensionName + " vs value=" + dimensionValue);
        }

        return publishMetric(namespace, metricName, dims, value);
    }

    public ExternalResult publishMetric(String namespace, String metricName, List<Dimension> dims, double value) {
        MetricDatum datum = new MetricDatum().withMetricName(metricName).withValue((double)value).withUnit(StandardUnit.Count);
        if (dims != null) {
            if (datum.getDimensions() == null) {
                datum.setDimensions(new ArrayList<Dimension>());
            }
            datum.getDimensions().addAll(dims);
        }
        PutMetricDataRequest pmdr = new PutMetricDataRequest().withNamespace(namespace).withMetricData(datum);
        _log.debug("cloudwatch(" + namespace + ":" + metricName
                + " (" + dims + ") "
                + " " + value + ")");
        PutMetricDataResult result = getClient().putMetricData(pmdr);

        return new AwsExternalResult(true, result.toString(), null);

    }

    private AmazonCloudWatch getClient() {
        if (_client == null) {
            _client = AmazonCloudWatchClientBuilder.defaultClient();
        }
        return _client;
    }
}
