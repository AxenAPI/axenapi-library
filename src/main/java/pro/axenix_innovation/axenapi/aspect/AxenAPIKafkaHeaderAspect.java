
/*
 * Copyright (C) 2023 Axenix Innovations LLC
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

package pro.axenix_innovation.axenapi.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class AxenAPIKafkaHeaderAspect {

//    private final ServiceTokenManager serviceTokenManager;
//    private final TokenProducerService tokenProducerService;
//
//    /**
//     * Changes a user or service token.
//     *
//     * @param joinPoint connection point
//     * @return context.
//     */
//    @Around(
//            "execution(public **.model.security.context.CallContext " +
//                    "**.services.security.SecurityService.getCallContextFromKafkaHeaders(java.util.Map<String, Object>))"
//    )
//    @SuppressWarnings("unchecked")
//    public Object replaceHeaders(ProceedingJoinPoint joinPoint) throws Throwable {
//        /* Getting a list of function arguments for accessing the header map. */
//        List<Object> args = Arrays.asList(joinPoint.getArgs());
//        Map<String, Object> headers = (Map<String, Object>) args.get(0);
//
//        /* Check that the method was called from the current service via a swagger. */
//        /* A sign that we sent a message to Kafka through a swagger - our service token. *///        if (headers.containsKey(Constants.SERVICE_TOKEN_KEY)) {
//            String serviceToken = KafkaHeaderAccessor.fromStringHeader(headers, Headers.SERVICE_ACCESS_TOKEN);
//
//            /* If the call was from another service, then we do not change the existing logic. */
//            if (!Objects.equals(serviceToken, serviceTokenManager.getAccessToken())) {
//                return joinPoint.proceed(joinPoint.getArgs());
//            }
//        }
//
//        if (!headers.containsKey(Constants.MESSAGE_ID_KEY)) {
//            return joinPoint.proceed(joinPoint.getArgs());
//        }
//
//        UUID messageId = KafkaHeaderAccessor.fromUUIDHeader(headers, Headers.MESSAGE_ID);
//
//        /* We replace all the necessary headers and pass new arguments to the function. */
//        headers = tokenProducerService.replaceTokenIntoParams(headers, messageId);
//
//        return joinPoint.proceed(new Object[] { headers });
//    }

}
