package com.example.openmarket.infrastructure.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect que intercepta todos os métodos públicos dos ApplicationServices
 * para coletar métricas de uso (contadores e tempo de execução).
 *
 * <p>Executa na MESMA THREAD do método interceptado (síncrono).</p>
 *
 * <p>Métricas coletadas:</p>
 * <ul>
 *   <li><b>app_service_calls_total</b> - Contador de chamadas por service/method/status</li>
 *   <li><b>app_service_duration_seconds</b> - Tempo de execução por service/method</li>
 * </ul>
 *
 * <p>Exemplo de query no Prometheus/Kibana:</p>
 * <pre>
 *   app_service_calls_total{service="ProductService", method="createProduct", status="success"}
 *   app_service_duration_seconds{service="OrderService", method="placeOrder"}
 * </pre>
 */
// Desativado em favor do Elastic APM que fornece instrumentação mais completa
// Para reativar, descomente as anotações @Aspect e @Component
// @Aspect
// @Component
public class ServiceMetricsAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceMetricsAspect.class);

    private final MeterRegistry meterRegistry;

    public ServiceMetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Pointcut que define QUAIS métodos serão interceptados.
     *
     * execution(* com.example.openmarket.application.service.*Service.*(..))
     *           │  │                                         │       │  │
     *           │  │                                         │       │  └─ qualquer argumento
     *           │  │                                         │       └─ qualquer método
     *           │  │                                         └─ classes que terminam com "Service"
     *           │  └─ qualquer pacote
     *           └─ qualquer tipo de retorno
     */
    @Pointcut("execution(* com.example.openmarket.application.service.*Service.*(..))")
    public void applicationServiceMethods() {}

    /**
     * Advice que executa AO REDOR do método interceptado.
     *
     * Fluxo:
     * 1. Inicia o timer
     * 2. Chama joinPoint.proceed() (executa o método real)
     * 3. Para o timer
     * 4. Incrementa contador de sucesso ou erro
     * 5. Loga em INFO para coleta pelo Filebeat
     * 6. Retorna o resultado (ou propaga a exceção)
     */
    @Around("applicationServiceMethods()")
    public Object measureServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String serviceName = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();

        // Inicia medição de tempo
        Timer.Sample timerSample = Timer.start(meterRegistry);

        try {
            // >>> AQUI EXECUTA O MÉTODO REAL <<<
            Object result = joinPoint.proceed();

            long durationMs = System.currentTimeMillis() - startTime;

            // Registra métricas (protegido contra falhas)
            recordMetricsSafely(timerSample, serviceName, methodName, "success");

            // Log INFO estruturado para Filebeat/Kibana
            log.info("SERVICE_CALL service={} method={} status=success duration_ms={}",
                serviceName, methodName, durationMs);

            return result;

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;

            // Registra métricas (protegido contra falhas)
            recordMetricsSafely(timerSample, serviceName, methodName, "error");

            // Log INFO estruturado para Filebeat/Kibana
            log.info("SERVICE_CALL service={} method={} status=error duration_ms={} exception={} message=\"{}\"",
                serviceName, methodName, durationMs, e.getClass().getSimpleName(), e.getMessage());

            // Propaga a exceção (não engole!)
            throw e;
        }
    }

    /**
     * Registra métricas de forma segura, sem propagar exceções.
     * Se falhar ao registrar métricas, apenas loga o erro e continua.
     */
    private void recordMetricsSafely(Timer.Sample timerSample, String service, String method, String status) {
        try {
            // Para o timer e registra duração
            timerSample.stop(Timer.builder("app.service.duration")
                .description("Tempo de execução dos métodos de ApplicationService")
                .tag("service", service)
                .tag("method", method)
                .register(meterRegistry));

            // Incrementa contador de chamadas
            meterRegistry.counter("app.service.calls",
                "service", service,
                "method", method,
                "status", status
            ).increment();

        } catch (Exception e) {
            log.warn("Falha ao registrar métricas para {}.{}: {}", service, method, e.getMessage());
        }
    }
}
