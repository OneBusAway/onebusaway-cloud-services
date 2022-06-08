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

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import org.onebusaway.cloud.api.Credential;
import org.onebusaway.cloud.api.ExternalResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class CloudWatchServices {
    private final Logger _log = LoggerFactory.getLogger(CloudWatchServices.class);
    private static final int MAX_DATUM_LENGTH = 20;

    Map<Credential, AmazonCloudWatch> _clientMap = new HashMap<>();

    public ExternalResult publishMetric(String namespace, String metricName, String dimensionName, String dimensionValue, double value) {
        return publishMetric(new CredentialContainer().getDefaultCredential(),
                namespace, metricName, dimensionName, dimensionValue, value);
    }
    public ExternalResult publishMetric(Credential credential, String namespace, String metricName, String dimensionName, String dimensionValue, double value) {
        ArrayList<Dimension> dims = null;

        if (dimensionName != null && dimensionValue != null) {
            dims = new ArrayList<>(1);
            Dimension dim = new Dimension().withName(dimensionName).withValue(dimensionValue);
            dims.add(dim);
        }
        MetricDatum metricDatum = getMetricAsMetricDatum(metricName, dims, value);
        return publishMetric(credential, namespace, metricDatum);
    }

    public ExternalResult publishMultiDimensionalMetric(String namespace, String metricName, String[] dimensionName, String[] dimensionValue, double value) {
        return publishMultiDimensionalMetric(new CredentialContainer().getDefaultCredential(),
                namespace, metricName, dimensionName, dimensionValue, value);
    }
    public ExternalResult publishMultiDimensionalMetric(Credential credential, String namespace, String metricName, String[] dimensionName, String[] dimensionValue, double value) {
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

        MetricDatum metricDatum = getMetricAsMetricDatum(metricName, dims, value);
        return publishMetric(credential, namespace, metricDatum);
    }

    public ExternalResult publishMetrics(String namespace, List<String> metricNames, List<String> dimensionNames,
                                         List<String> dimensionValues, List<Double> values) {
        return publishMetrics(new CredentialContainer().getDefaultCredential(),
                namespace, metricNames, dimensionNames, dimensionValues, values);
    }

        public ExternalResult publishMetrics(Credential credential, String namespace, List<String> metricNames, List<String> dimensionNames,
                                         List<String> dimensionValues, List<Double> values) {

        if(metricNames == null || values == null) {
            throw new IllegalStateException("Invalid input: metricNames=" + metricNames + " and values=" + values);
        }

        if(metricNames.size() != values.size()) {
            throw new IllegalStateException("Input array lengths must match: metricNames=" + metricNames.size() +
                    " vs values " + values.size());
        }

        if (dimensionNames != null && dimensionValues != null &&
                dimensionNames.size() != metricNames.size() || dimensionValues.size() != metricNames.size()) {
            throw new IllegalStateException("Input array lengths must match: dimensionNames=" + metricNames.size() +
                    " vs dimensionValues " + dimensionValues.size());
        }

        MetricDatum[] metricDatums = new MetricDatum[metricNames.size()];
        for(int i = 0; i < metricNames.size(); i++){
            ArrayList<Dimension> dims = null;
            if (dimensionNames != null && dimensionValues != null){
                dims = new ArrayList<>(1);
                Dimension dim = new Dimension().withName(dimensionNames.get(i)).withValue(dimensionValues.get(i));
                dims.add(dim);
            }
            MetricDatum metricDatum = getMetricAsMetricDatum(metricNames.get(i), dims, values.get(i));
            metricDatums[i] = metricDatum;
        }
        if (metricDatums.length > MAX_DATUM_LENGTH) {
            ExternalResult lastResult = null;
            List<MetricDatum[]> metricData = splitArray(metricDatums, MAX_DATUM_LENGTH);
            for (MetricDatum[] metricDatum : metricData) {
                lastResult = publishMetric(credential, namespace, metricDatum);
            }
            return lastResult;
        } else {
            return publishMetric(credential, namespace, metricDatums);
        }
    }

    private MetricDatum getMetricAsMetricDatum(String metricName, List<Dimension> dims, double value){
        MetricDatum datum = new MetricDatum().withMetricName(metricName).withValue(value).withUnit(StandardUnit.Count);
        if (dims != null) {
            if (datum.getDimensions() == null) {
                datum.setDimensions(new ArrayList<Dimension>());
            }
            datum.getDimensions().addAll(dims);
        }
        return datum;
    }

    public ExternalResult publishMetric(String namespace, MetricDatum ... datum) {
        return publishMetric(new CredentialContainer().getDefaultCredential(), namespace, datum);
    }
    public ExternalResult publishMetric(Credential credential, String namespace, MetricDatum ... datum) {
        PutMetricDataRequest pmdr = new PutMetricDataRequest().withNamespace(namespace).withMetricData(datum);
        for(MetricDatum md : datum){
            _log.debug("cloudwatch(" + namespace + ":" + md.getMetricName()
                    + " (" + md.getDimensions() + ") "
                    + " " + md.getValue() + ")");
        }
        PutMetricDataResult result = getClient(credential).putMetricData(pmdr);
        return new AwsExternalResult(true, result.toString(), null);
    }

    // split array up into smaller chunks - code found online
    private <T extends Object> List<T[]> splitArray(T[] array, int splitSize) {

        int numberOfArrays = array.length / splitSize;
        int remainder = array.length % splitSize;

        int start = 0;
        int end = 0;

        List<T[]> list = new ArrayList<T[]>();
        for (int i = 0; i < numberOfArrays; i++) {
            end += splitSize;
            list.add(Arrays.copyOfRange(array, start, end));
            start = end;
        }

        if(remainder > 0) {
            list.add(Arrays.copyOfRange(array, start, (start + remainder)));
        }
        return list;
    }

    // cached AWS client per credential
    private AmazonCloudWatch getClient(Credential credential) {
        if (_clientMap.get(credential) == null) {
            _clientMap.put(credential, AmazonCloudWatchClientBuilder.standard()
                    .withCredentials(new ProfileCredentialsProvider(credential.getKey()))
                    .withRegion(credential.getValue())
                    .build());
        }
        return _clientMap.get(credential);
    }
}
