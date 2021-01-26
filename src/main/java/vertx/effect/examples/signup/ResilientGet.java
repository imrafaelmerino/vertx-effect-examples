package vertx.effect.examples.signup;

import jsonvalues.JsObj;
import lombok.Builder;
import vertx.effect.*;
import vertx.effect.exp.Cons;
import vertx.effect.httpclient.GetReq;

import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static vertx.effect.Failures.*;
import static vertx.effect.httpclient.HttpResp.STATUS_CODE_LENS;

@Builder
class ResilientGet<O> implements λ<String, O> {

    private λc<GetReq, JsObj> get;
    @Builder.Default
    private int failureAttempts = 3;
    @Builder.Default
    private int not2XXAttempts = 2;
    @Builder.Default
    private Function<Integer, RetryPolicy<Throwable>> retryPolicy = attempts -> (error, remaining) -> Cons.NULL;
    @Builder.Default
    private int reqTimeout = 3000;
    private String uri;
    private λ<JsObj, O> mapResp;


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
                                   not2XXAttempts
                       )
                       .flatMap(mapResp);
    }
}
