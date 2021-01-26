package vertx.effect.examples.signup;

import io.vertx.core.http.HttpClientOptions;
import jsonvalues.JsArray;
import jsonvalues.JsObj;
import lombok.Builder;
import vertx.effect.RetryPolicy;
import vertx.effect.exp.Cons;
import vertx.effect.httpclient.HttpClientModule;
import vertx.effect.λ;

import java.nio.charset.Charset;
import java.util.function.Function;

import static java.net.URLEncoder.encode;

public class GeolocationModule extends HttpClientModule {
    private static final int DEFAULT_REQ_TIMEOUT = 5000;
    private static final int DEFAULT_FAILURE_ATTEMPTS = 3;
    private static final int DEFAULT_NOT2XXATTEMPTS = 2;
    private static final String DEFAULT_ADDRESS = "google-map-http-client";
    private static final Function<Integer, RetryPolicy<Throwable>> DEFAULT_RETRY_POLICY =
            attempts -> (remaining, error) -> Cons.NULL;

    public final λ<String, JsArray> getAddresses;

    @Builder
    public GeolocationModule(final String apiKey,
                             final HttpClientOptions options,
                             final int reqTimeout,
                             final int failureAttempts,
                             final int not2XXAttempts,
                             final Function<Integer, RetryPolicy<Throwable>> retryPolicy,
                             final String verticleAddress) {

        super(options,
              verticleAddress == null ? DEFAULT_ADDRESS : verticleAddress
        );

        this.getAddresses = address ->
                ResilientGet.<JsArray>builder()
                        .get(this.get)
                        .uri(String.format("/maps/api/geocode/json?address=%s&key=%s",
                                           encode(address,
                                                  Charset.availableCharsets()
                                                         .get("utf-8")
                                           ),
                                           apiKey
                        ))
                        .mapResp(
                                resp -> Cons.success(JsObj.parse(resp.getStr("body"))
                                                          .getArray("results")
                                )
                        )
                        .reqTimeout(reqTimeout > 0 ? reqTimeout : DEFAULT_REQ_TIMEOUT)
                        .not2XXAttempts(not2XXAttempts > 0 ? not2XXAttempts : DEFAULT_NOT2XXATTEMPTS)
                        .failureAttempts(failureAttempts > 0 ? failureAttempts : DEFAULT_FAILURE_ATTEMPTS)
                        .retryPolicy(retryPolicy != null ? retryPolicy : DEFAULT_RETRY_POLICY)
                        .build()
                        .apply(address)
                        .recover(e -> JsArray.empty());
    }
}
