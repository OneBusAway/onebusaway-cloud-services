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

public class CredentialContainer {
    private String profile;

    public CredentialContainer(String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public static CredentialContainer getDefault() {
        return new CredentialContainer("default");
    }
}
