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

import org.onebusaway.cloud.api.Credential;

public class CredentialContainer {

    public static final String DEFAULT_PROFILE = "default";
    public static final String DEFAULT_REGION = "us-east-1";
    private String profile;
    private String region;

    public CredentialContainer() {
        this.profile = DEFAULT_PROFILE;
        this.region = DEFAULT_REGION;
    }
    public CredentialContainer(String profile) {
        this.profile = profile;
        this.region = DEFAULT_REGION;
    }
    public CredentialContainer(String profile, String region) {
        this.profile = profile;
        this.region = region;
    }

    public String getProfile() {
        return profile;
    }

    public String getRegion() { return region; }

    public static CredentialContainer getDefault() {
        return new CredentialContainer();
    }

    public Credential getDefaultCredential() {
        return new Credential().createExternalProfileKey(DEFAULT_PROFILE, DEFAULT_REGION);
    }

    public String toString() {
        return "{region=" + region + ", profile=" + profile + "}";
    }
}
