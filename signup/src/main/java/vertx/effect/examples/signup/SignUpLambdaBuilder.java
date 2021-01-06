package vertx.effect.examples.signup;

import jsonvalues.JsArray;
import jsonvalues.JsObj;
import vertx.effect.λ;

import java.time.Instant;
import java.util.Optional;

 class SignUpLambdaBuilder {

    private λ<String, Optional<JsObj>> findByEmail;
    private λ<JsObj, String> insert;
    private λ<String, JsArray> getAddresses;
    private λ<JsObj, Void> sendEmail;
    private λ<Void, Long> count;
    private λ<Void, Instant> getTimestamp;

    public SignUpLambdaBuilder setFindByEmail(final λ<String, Optional<JsObj>> findByEmail) {
        this.findByEmail = findByEmail;
        return this;
    }

    public SignUpLambdaBuilder setInsert(final λ<JsObj, String> insert) {
        this.insert = insert;
        return this;
    }

    public SignUpLambdaBuilder setGetAddresses(final λ<String, JsArray> getAddresses) {
        this.getAddresses = getAddresses;
        return this;
    }

    public SignUpLambdaBuilder setSendEmail(final λ<JsObj, Void> sendEmail) {
        this.sendEmail = sendEmail;
        return this;
    }

    public SignUpLambdaBuilder setCount(final λ<Void, Long> count) {
        this.count = count;
        return this;
    }

    public SignUpLambdaBuilder setGetTimestamp(final λ<Void, Instant> getTimestamp) {
        this.getTimestamp = getTimestamp;
        return this;
    }

    public SignUpLambda createSignUpLambda() {
        return new SignUpLambda(findByEmail,
                                insert,
                                getAddresses,
                                sendEmail,
                                count,
                                getTimestamp);
    }
}