package vertx.effect.examples.signup.geocode;

import io.vertx.core.http.HttpClientOptions;
import jsonvalues.JsArray;
import jsonvalues.JsObj;
import vertx.effect.RetryPolicy;
import vertx.effect.examples.signup.httpreq.ResilientGetBuilder;
import vertx.effect.exp.Cons;
import vertx.effect.httpclient.HttpClientModule;
import vertx.effect.λ;

import java.nio.charset.Charset;
import java.util.function.Function;

import static java.net.URLEncoder.encode;
import static java.util.Objects.requireNonNull;

public class GeolocationModule extends HttpClientModule {

    public final λ<String, JsArray> getAddresses;

    public GeolocationModule(final String apiKey,
                             final HttpClientOptions options,
                             final int reqTimeout,
                             final int failureAttempts,
                             final int not2XXAttempts,
                             final Function<Integer, RetryPolicy<Throwable>> retryPolicy,
                             final String httpClientAddress) {

        super(requireNonNull(options),
              httpClientAddress
        );
        this.getAddresses = address ->
                new ResilientGetBuilder<>(this.get,
                                          String.format("/maps/api/geocode/json?address=%s&key=%s",
                                                        encode(address,
                                                               Charset.availableCharsets().get("utf-8")
                                                        ),
                                                        apiKey
                                          ),
                                          resp -> Cons.success(JsObj.parse(resp.getStr("body"))
                                                                    .getArray("results")
                                                              )
                ).setReqTimeout(reqTimeout)
                 .setNot2XXAttempts(not2XXAttempts)
                 .setFailureAttempts(failureAttempts)
                 .setRetryPolicy(retryPolicy)
                 .createResilientGet()
                 .apply(address)
                 .recover(e->JsArray.empty());
    }
}
