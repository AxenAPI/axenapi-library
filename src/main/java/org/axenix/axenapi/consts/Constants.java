
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

package org.axenix.axenapi.consts;

public class Constants {
    public static final String USER_TOKEN_KEY = Headers.ACCESS_TOKEN.name();
    public static final String SERVICE_TOKEN_KEY = Headers.SERVICE_ACCESS_TOKEN.name();
    public static final String MESSAGE_ID_KEY = Headers.MESSAGE_ID.name();

    public static final String WITHOUT_RESPONSE_MESSAGE_ENG = "No return value";
    public static final String WITH_REPLY_TOPIC_RESPONSE_MESSAGE_ENG = "Returns the reply %s to the topic passed through the replyTopic header. The return value is not intercepted";
    public static final String WITH_FIXED_REPLY_TOPIC_RESPONSE_MESSAGE_ENG = "Returns the response %s to topic %s. The return value is not intercepted";

    public static final String WITHOUT_RESPONSE_MESSAGE = "Возвращаемое значение отсутствует";
    public static final String WITH_REPLY_TOPIC_RESPONSE_MESSAGE = "Возвращает ответ %s в топик, передаваемый через хедер replyTopic. Возвращаемое значение не перехватывается";
    public static final String WITH_FIXED_REPLY_TOPIC_RESPONSE_MESSAGE = "Возвращает ответ %s в топик %s. Возвращаемое значение не перехватывается";


}
