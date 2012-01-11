/*
 * Copyright 2009 Michael Gerzabek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nu.localhost.tapestry5.springsecurity.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.intercept.RequestKey;

/**
 * Straight forward mapping definition of HttpRequestURIs to intercept by
 * FilterSecurityInterceptor.
 *
 * @author Michael Gerzabek
 *
 */
public class RequestInvocationDefinition {

    private RequestKey requestKey;
    private List<ConfigAttribute> configAttributes;

    public RequestInvocationDefinition(String key, String roles) {
        this.requestKey = new RequestKey(key);
		String[] allAttrs = StringUtils.stripAll(
                StringUtils.splitPreserveAllTokens(roles, ',')
            );
		this.configAttributes = new ArrayList<ConfigAttribute>();
		for (String attr : allAttrs) {
			this.configAttributes.add(new SecurityConfig(attr));
		}

    }

    public RequestKey getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(RequestKey requestKey) {
        this.requestKey = requestKey;
    }

    public List<ConfigAttribute> getConfigAttributeDefinition() {
        return configAttributes;
    }

    public void setConfigAttributeDefinition(
            List<ConfigAttribute> configAttrs) {
        this.configAttributes = configAttrs;
    }
}