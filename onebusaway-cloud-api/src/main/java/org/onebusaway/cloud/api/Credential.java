/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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

/**
 * encapsulates the various types of authentication mechanisms
 * such as api key, external credential, user/password...
 */
public class Credential {

    public enum CredentialType {
        API_KEY_PARAM,
        API_KEY_HEADER,
        EXTERNAL_PROFILE,
        NO_OP
    };

    private CredentialType _type;
    private String _key;
    private String _keyName;
    private String _value;

    public Credential createApiKeyParam(String key) {
        Credential c = new Credential();
        c.setType(CredentialType.API_KEY_PARAM);
        c.setKey(key);
        return c;
    }

    public Credential createApiKeyHeader(String key, String keyName) {
        Credential c = new Credential();
        c.setType(CredentialType.API_KEY_HEADER);
        c.setKey(key);
        c.setKeyName(keyName);
        return c;
    }

    public Credential createExternalProfileKey(String profile, String region) {
        Credential c = new Credential();
        c.setType(CredentialType.EXTERNAL_PROFILE);
        c.setKey(profile);
        c.setValue(region);
        return c;
    }

    public Credential createNoOp() {
        Credential c = new Credential();
        c.setType(CredentialType.NO_OP);
        return c;
    }

    public String getValue() { return _value; }
    public void setValue(String value) {
        _value = value;
    }

    public String getKeyName() { return _keyName; }
    public void setKeyName(String keyName) {
        _keyName = keyName;
    }

    public String getKey() { return _key; }
    public void setKey(String key) {
        _key = key;
    }

    public CredentialType getType() { return _type; }
    public void setType(CredentialType type) {
        _type = type;
    }

    public String toString() {
        return "Credential{" + (getType()==null?"NuLl":getType().toString()) + "(" + _key + ")}";
    }

}
