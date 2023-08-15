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
