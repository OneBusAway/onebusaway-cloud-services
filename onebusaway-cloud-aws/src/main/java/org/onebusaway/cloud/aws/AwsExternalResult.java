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

import org.onebusaway.cloud.api.ExternalResult;

/**
 * An AWS specific result.
 */
public class AwsExternalResult implements ExternalResult {
    private boolean _success = false;
    private String _errorMessage = null;
    private String _responseMessage = null;

    public AwsExternalResult(boolean success) {
        _success = success;
    }

    public AwsExternalResult(boolean success, String errorMessage, String responseMessage) {
        _success = success;
        _errorMessage = errorMessage;
        _responseMessage = responseMessage;
    }

    @Override
    public boolean getSuccess() {
        return _success;
    }

    @Override
    public String getErrorMessage() {
        return _errorMessage;
    }

    @Override
    public String getResponseMessage() {
        return _responseMessage;
    }
}
