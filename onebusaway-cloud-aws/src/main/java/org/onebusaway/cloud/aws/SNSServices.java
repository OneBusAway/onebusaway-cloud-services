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

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * Encapsulate and cache references to AWS SNS sdk.
 */
public class SNSServices {
    private AmazonSNS _sns;
    private Boolean _initialized = null;

    public boolean publish(String topic, String message) {
        AmazonSNS sns = getSns();
        if (sns == null) return false;
        PublishRequest pr = new PublishRequest(topic, message);
        PublishResult result = _sns.publish(pr);
        return result.getMessageId() != null;
    }

    public AmazonSNS getSns() {
        if (_initialized == null) {
            try {
                _sns = AmazonSNSClientBuilder.defaultClient();
                _initialized = true;
            } catch (Exception any) {
                _initialized = false;
            }
        }
        if (!_initialized) return null;
        return _sns;
    }
}
