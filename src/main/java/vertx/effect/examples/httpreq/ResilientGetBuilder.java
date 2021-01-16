package vertx.effect.examples.httpreq;

import jsonvalues.JsObj;
import vertx.effect.RetryPolicy;
import vertx.effect.exp.Cons;
import vertx.effect.httpclient.GetReq;
import vertx.effect.λ;
import vertx.effect.λc;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class ResilientGetBuilder<O> {

    private static final int DEFAULT_FAILURE_ATTEMPTS = 3;
    private static final int DEFAULT_NOT_2XX_ATTEMPTS = 2;
    private static final int DEFAULT_REQ_TIMEOUT = 3000;

    private static final Function<Integer, RetryPolicy<Throwable>> DEFAULT_RETRY_POLICY =
            attempts -> (error, remaining) -> Cons.NULL;


    public ResilientGetBuilder(final λc<GetReq, JsObj> get,
                               final String uri,
                               final λ<JsObj, O> mapResp) {
        this.get = requireNonNull(get);
        this.uri = requireNonNull(uri);
        this.mapResp = requireNonNull(mapResp);
    }

    private final λc<GetReq, JsObj> get;
    private final String uri;


    private λ<JsObj, O> mapResp;
    private int failureAttempts = DEFAULT_FAILURE_ATTEMPTS;
    private int not2XXAttempts = DEFAULT_NOT_2XX_ATTEMPTS;
    private int reqTimeout = DEFAULT_REQ_TIMEOUT;
    private Function<Integer, RetryPolicy<Throwable>> retryPolicy = DEFAULT_RETRY_POLICY;


    public ResilientGetBuilder<O> setFailureAttempts(final int failureAttempts) {
        if (failureAttempts < 1) throw new IllegalArgumentException("failure attempts < 1");
        this.failureAttempts = failureAttempts;
        return this;
    }

    public ResilientGetBuilder<O> setNot2XXAttempts(final int not2XXAttempts) {
        if (not2XXAttempts < 1) throw new IllegalArgumentException("not 200 attempts < 1");
        this.not2XXAttempts = not2XXAttempts;
        return this;
    }

    public ResilientGetBuilder<O> setRetryPolicy(final Function<Integer, RetryPolicy<Throwable>> retryPolicy) {
        this.retryPolicy = requireNonNull(retryPolicy);
        return this;
    }

    public ResilientGetBuilder<O> setReqTimeout(final int reqTimeout) {
        if (reqTimeout < 0) throw new IllegalArgumentException("reqTimeout < 0");
        this.reqTimeout = reqTimeout;
        return this;
    }

    public ResilientGet<O> createResilientGet() {
        return new ResilientGet<>(get,
                                  failureAttempts,
                                  not2XXAttempts,
                                  retryPolicy,
                                  reqTimeout,
                                  uri,
                                  mapResp
        );
    }
}