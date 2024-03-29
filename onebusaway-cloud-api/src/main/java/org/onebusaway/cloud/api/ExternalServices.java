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

import java.util.List;

/**
 * Abstract common cloud functions into an interface with the hopes of keeping OneBusAway
 * cloud-agnostic.  Use the onebusaway-cloud-noop for trivial implementation of this so
 * as to not integrate with cloud services.
 */
public interface ExternalServices {
    ExternalResult publishMessage(String topic, String messageConents);

    ExternalResult publishMetric(String namespace, String metricName, String dimensionName, String dimensionValue, double value);
    ExternalResult publishMetric(Credential credential, String namespace, String metricName, String dimensionName, String dimensionValue, double value);

    ExternalResult publishMetrics(String namespace, List<String> metricNames, List<String> dimensionNames, List<String> dimensionValues, List<Double> values);
    ExternalResult publishMetrics(Credential credential, String namespace, List<String> metricNames, List<String> dimensionNames, List<String> dimensionValues, List<Double> values);

    ExternalResult publishMultiDimensionalMetric(String namespace, String metricName, String[] dimensionName, String[] dimensionValue, double value);
    ExternalResult publishMultiDimensionalMetric(Credential credential, String namespace, String metricName, String[] dimensionName, String[] dimensionValue, double value);

    /**
     * Read a file from S3 to an InputStream
     *
     * @param url URL to read the file from
     * @param callback Do something with the InputStream
     * @param profile profile to use with S3 (default to "default")
     * @return result
     */
    ExternalResult getFileAsStream(String url, InputStreamConsumer callback, String profile);

    /**
     * Read a file from S3 to an InputStream
     *
     * @param url URL to read the file from
     * @param callback Do something with the InputStream
     * @param profile profile to use with S3 (default to "default")
     * @param region us-east-1 for example
     * @return result
     */
    ExternalResult getFileAsStream(String url, InputStreamConsumer callback, String profile, String region);

    /**
     * Read a file from S3 to an InputStream, using the default S3 profile
     *
     * @param url URL to read the file from
     * @param callback Do something with the InputStream
     * @return result
     */
    ExternalResult getFileAsStream(String url, InputStreamConsumer callback);

    /**
     * Copy a file on disk to S3, using the supplied S3 profile.
     * @param url
     * @param file
     * @param profile
     * @return
     */
    ExternalResult putFile(String url, String file, String profile, String region);

    /**
     * Copy a file on disk to S3 using the default S3 profile.
     * @param url
     * @param file
     * @return
     */
    ExternalResult putFile(String url, String file);

    /**
     * Allow services to determine if they are the primary instance for leadership-election purposes
     * (e.g. if multiple servers in a group are logging metrics.)
     *
     * @return true if this is the primary instance, false otherwise
     */
    boolean isInstancePrimary();
}