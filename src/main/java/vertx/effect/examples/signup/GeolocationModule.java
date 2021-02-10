package vertx.effect.examples.signup;

import io.vertx.core.http.HttpClientOptions;
import jsonvalues.JsObj;
import lombok.Builder;
import vertx.effect.httpclient.GetReq;
import vertx.effect.httpclient.HttpClientModule;
import vertx.effect.λ;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static java.net.URLEncoder.encode;

public class GeolocationModule extends HttpClientModule {
    private static final String DEFAULT_ADDRESS = "google-map-http-client";

    public final λ<String, JsObj> getAddresses;

    @Builder
    public GeolocationModule(final String apiKey,
                             final HttpClientOptions options,
                             final int reqTimeout,
                             final String verticleAddress) {

        super(options,
              verticleAddress == null ? DEFAULT_ADDRESS : verticleAddress
        );

        this.getAddresses = address ->
                this.get.apply(new GetReq().timeout(reqTimeout,
                                                    TimeUnit.MILLISECONDS)
                                           .uri(String.format("/maps/api/geocode/json?address=%s&key=%s",
                                                              encode(address,
                                                                     Charset.availableCharsets()
                                                                            .get("utf-8")
                                                              ),
                                                              apiKey
                                                )
                                           )
                );
    }
}
