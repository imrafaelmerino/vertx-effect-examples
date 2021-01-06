package vertx.effect.examples.signup.geocode;

import io.vertx.core.http.HttpClientOptions;
import vertx.effect.RetryPolicy;
import vertx.effect.exp.Cons;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class GeolocationModuleBuilder {
    private final String apiKey;
    private final HttpClientOptions options;
    private int reqTimeout = 3000;
    private int failureAttempts = 3;
    private int not2XXAttempts = 2;
    private String httpClientAddress = "google-map-http-client";

    private Function<Integer, RetryPolicy<Throwable>> retryPolicy =
            attempts -> (remaining, Throwable) -> Cons.NULL;


    public GeolocationModuleBuilder(final String apiKey,
                                    final HttpClientOptions options) {
        this.apiKey = requireNonNull(apiKey);
        this.options = requireNonNull(options);
    }

    public GeolocationModuleBuilder setReqTimeout(final int reqTimeout) {
        this.reqTimeout = reqTimeout;
        return this;
    }

    public GeolocationModuleBuilder setHttpClientAddress(final String address) {
        this.httpClientAddress = address;
        return this;
    }
    public GeolocationModuleBuilder setFailureAttempts(final int failureAttempts) {
        this.failureAttempts = failureAttempts;
        return this;
    }

    public GeolocationModuleBuilder setNot2XXAttempts(final int not2XXAttempts) {
        this.not2XXAttempts = not2XXAttempts;
        return this;
    }

    public GeolocationModuleBuilder setRetryPolicy(final Function<Integer, RetryPolicy<Throwable>> retryPolicy) {
        this.retryPolicy = requireNonNull(retryPolicy);
        return this;
    }

    public GeolocationModule createModule() {
        return new GeolocationModule(apiKey,
                                     options,
                                     reqTimeout,
                                     failureAttempts,
                                     not2XXAttempts,
                                     retryPolicy,
                                     httpClientAddress);
    }
}