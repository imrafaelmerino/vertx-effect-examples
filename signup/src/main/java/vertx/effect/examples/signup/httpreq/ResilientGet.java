package vertx.effect.examples.signup.httpreq;

import jsonvalues.JsObj;
import vertx.effect.*;
import vertx.effect.httpclient.GetReq;

import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static vertx.effect.Failures.*;
import static vertx.effect.httpclient.HttpResp.STATUS_CODE_LENS;

public class ResilientGet<O> implements λ<String, O> {

    private final λc<GetReq, JsObj> get;
    private final int failureAttempts;
    private final int not200Attempts;
    private final Function<Integer, RetryPolicy<Throwable>> retryPolicy;
    private final int reqTimeout;
    private final String uri;
    private final λ<JsObj,O> mapResp;

    public ResilientGet(final λc<GetReq, JsObj> get,
                        final int failureAttempts,
                        final int not200Attempts,
                        final Function<Integer, RetryPolicy<Throwable>> retryPolicy,
                        final int reqTimeout,
                        final String uri,
                        final λ<JsObj,O> mapResp) {
        this.get = get;
        this.failureAttempts = failureAttempts;
        this.not200Attempts = not200Attempts;
        this.retryPolicy = retryPolicy;
        this.reqTimeout = reqTimeout;
        this.uri = uri;
        this.mapResp = mapResp;
    }

    @Override
    public Val<O> apply(final String address) {

        return this.get.apply(new GetReq().uri(uri)
                                          .timeout(reqTimeout,
                                                   MILLISECONDS
                                                  )
                             )
                       .retry(Failures.anyOf(HTTP_CONNECT_TIMEOUT_CODE,
                                             HTTP_REQUEST_TIMEOUT_CODE,
                                             HTTP_UNKNOWN_HOST_CODE,
                                             HTTP_CONNECTION_WAS_CLOSED_CODE
                                            ),
                              failureAttempts,
                              retryPolicy.apply(failureAttempts)
                             )
                       .retryWhile(resp -> {
                                       Integer statusCode = STATUS_CODE_LENS.get.apply(resp);
                                       return statusCode >= 300 || statusCode < 200;
                                   },
                                   not200Attempts
                                  )
                       .flatMap(mapResp);
    }
}
