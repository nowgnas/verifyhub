package com.verifyhub.verification.adapter.out.provider;

import com.verifyhub.common.exception.ProviderCallFailedException;
import com.verifyhub.common.exception.ProviderTimeoutException;
import com.verifyhub.verification.domain.ProviderRequest;
import com.verifyhub.verification.domain.ProviderRequestResult;
import com.verifyhub.verification.domain.ProviderResult;
import com.verifyhub.verification.domain.ProviderResultRequest;
import com.verifyhub.verification.domain.ProviderType;
import com.verifyhub.verification.port.out.ProviderClientPort;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProviderClientResilienceDecorator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final ExecutorService executorService;

    @Autowired
    public ProviderClientResilienceDecorator(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry
    ) {
        this(circuitBreakerRegistry, retryRegistry, timeLimiterRegistry, Executors.newCachedThreadPool());
    }

    public ProviderClientResilienceDecorator(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry,
            ExecutorService executorService
    ) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.timeLimiterRegistry = timeLimiterRegistry;
        this.executorService = executorService;
    }

    public ProviderRequestResult requestVerification(ProviderClientPort delegate, ProviderRequest request) {
        return execute(delegate.providerType(), () -> delegate.requestVerification(request));
    }

    public ProviderResult requestResult(ProviderClientPort delegate, ProviderResultRequest request) {
        return execute(delegate.providerType(), () -> delegate.requestResult(request));
    }

    @PreDestroy
    public void close() {
        executorService.shutdownNow();
    }

    private <T> T execute(ProviderType providerType, Callable<T> callable) {
        String instanceName = resilienceInstanceName(providerType);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceName);
        Retry retry = retryRegistry.retry(instanceName);
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(instanceName);
        Callable<T> decorated = Retry.decorateCallable(retry, CircuitBreaker.decorateCallable(circuitBreaker, callable));

        try {
            return timeLimiter.executeFutureSupplier(() -> executorService.submit(decorated));
        } catch (TimeoutException exception) {
            throw new ProviderTimeoutException();
        } catch (ProviderTimeoutException | ProviderCallFailedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ProviderCallFailedException(exception.getMessage());
        }
    }

    private String resilienceInstanceName(ProviderType providerType) {
        String lowerCase = providerType.name().toLowerCase(Locale.ROOT);
        return lowerCase + "Provider";
    }
}
