package org.axenix.axenapi.aspect;

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
//     * Меняет пользовательский или сервисный токен.
//     *
//     * @param joinPoint точка соединения
//     * @return Контекст.
//     */
//    @Around(
//            "execution(public **.model.security.context.CallContext " +
//                    "**.services.security.SecurityService.getCallContextFromKafkaHeaders(java.util.Map<String, Object>))"
//    )
//    @SuppressWarnings("unchecked")
//    public Object replaceHeaders(ProceedingJoinPoint joinPoint) throws Throwable {
//        /* Получение списка аргументов функции для доступа к мапе хедеров. */
//        List<Object> args = Arrays.asList(joinPoint.getArgs());
//        Map<String, Object> headers = (Map<String, Object>) args.get(0);
//
//        /* Проверка, что метод был вызван из текущего сервиса через сваггер. */
//        /* Признак того, что мы отправили сообщение в кафку через сваггер - свой сервисный токен. */
//        if (headers.containsKey(Constants.SERVICE_TOKEN_KEY)) {
//            String serviceToken = KafkaHeaderAccessor.fromStringHeader(headers, Headers.SERVICE_ACCESS_TOKEN);
//
//            /* Если вызов был из другого сервиса, то не меняем существующую логику. */
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
//        /* Подменяем все необходимые хедеры и передаем новые аргументы в функцию. */
//        headers = tokenProducerService.replaceTokenIntoParams(headers, messageId);
//
//        return joinPoint.proceed(new Object[] { headers });
//    }

}
