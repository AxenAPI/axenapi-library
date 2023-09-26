
/*
 * Copyright [yyyy] [name of copyright owner]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.axenix.axenapi.service.impl;

import org.axenix.axenapi.consts.Constants;
import org.axenix.axenapi.consts.Headers;
import org.axenix.axenapi.model.TokenInfo;
import org.axenix.axenapi.service.TokenProducerService;
import org.axenix.axenapi.utils.KafkaHeaderAccessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TokenProducerServiceImpl implements TokenProducerService {
    ConcurrentMap<UUID, TokenInfo> tokens = new ConcurrentHashMap<>();

    @Override
    public void readTokenFromParams(Map<String, Object> params, UUID messageId) {
        TokenInfo tokenInfo = new TokenInfo(
                KafkaHeaderAccessor.fromStringHeader(params, Headers.ACCESS_TOKEN),
                KafkaHeaderAccessor.fromStringHeader(params, Headers.SERVICE_ACCESS_TOKEN)
        );
        tokens.put(messageId, tokenInfo);
    }

    @Override
    public Map<String, Object> replaceTokenIntoParams(Map<String, Object> params, UUID messageId) {
        TokenInfo tokenInfo = tokens.getOrDefault(messageId, null);

        if (tokenInfo == null) {
            return params;
        }

        String userToken = tokenInfo.getUserToken();
        String serviceToken = tokenInfo.getServiceToken();

        Map<String, Object> resultParams = new HashMap<>(params);

        if (resultParams.containsKey(Constants.USER_TOKEN_KEY)) {
            if (userToken == null) {
                resultParams.remove(Constants.USER_TOKEN_KEY);
            } else {
                resultParams.replace(Constants.USER_TOKEN_KEY, userToken.getBytes());
            }
        }
        else {
            if (userToken != null) {
                resultParams.put(Constants.USER_TOKEN_KEY, userToken.getBytes());
            }
        }

        if (resultParams.containsKey(Constants.SERVICE_TOKEN_KEY)) {
            if (serviceToken == null) {
                resultParams.remove(Constants.SERVICE_TOKEN_KEY);
            } else {
                resultParams.replace(Constants.SERVICE_TOKEN_KEY, serviceToken.getBytes());
            }
        }
        else {
            if (serviceToken != null) {
                resultParams.put(Constants.SERVICE_TOKEN_KEY, serviceToken.getBytes());
            }
        }

        /* Remove message, this message is already proceed.*/
        tokens.remove(messageId);

        return Collections.unmodifiableMap(resultParams);
    }
}
