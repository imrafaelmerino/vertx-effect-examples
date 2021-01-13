package vertx.effect.examples.signup;

import jsonvalues.JsArray;
import jsonvalues.JsObj;
import vertx.effect.examples.signup.email.EmailEntity;
import vertx.effect.λ;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static vertx.effect.examples.signup.ClientEntity.EMAIL_LENS;
import static vertx.effect.examples.signup.ClientEntity.NAME_LENS;
import static vertx.effect.examples.signup.email.EmailEntity.*;

class SignUpLambdaBuilder {

    private λ<String, Optional<JsObj>> findByEmail;
    private λ<JsObj, String> insert;
    private λ<String, JsArray> getAddresses;
    private λ<JsObj, Void> sendEmail;
    private λ<Void, Long> count;
    private λ<Void, Instant> getTimestamp;

    private Function<JsObj, JsObj> createEmail = client -> {
        String to = EMAIL_LENS.get.apply(client);
        String name = NAME_LENS.get.apply(client);
        return bodyLens.set.apply("Welcome "+ name+"!")
                           .andThen(subjectLens.set.apply("signup"))
                           .andThen(EmailEntity.contentTypeLens.set.apply("text/html"))
                           .andThen(toLens.set.apply(to))
                           .apply(client);
    };

    SignUpLambdaBuilder setFindByEmail(final λ<String, Optional<JsObj>> findByEmail) {
        this.findByEmail = findByEmail;
        return this;
    }

    SignUpLambdaBuilder setInsert(final λ<JsObj, String> insert) {
        this.insert = insert;
        return this;
    }

    SignUpLambdaBuilder setCreateEmail(final Function<JsObj, JsObj> createEmail) {
        this.createEmail = Objects.requireNonNull(createEmail);
        return this;
    }

    SignUpLambdaBuilder setGetAddresses(final λ<String, JsArray> getAddresses) {
        this.getAddresses = getAddresses;
        return this;
    }

    SignUpLambdaBuilder setSendEmail(final λ<JsObj, Void> sendEmail) {
        this.sendEmail = sendEmail;
        return this;
    }

    SignUpLambdaBuilder setCount(final λ<Void, Long> count) {
        this.count = count;
        return this;
    }

    SignUpLambdaBuilder setGetTimestamp(final λ<Void, Instant> getTimestamp) {
        this.getTimestamp = getTimestamp;
        return this;
    }

    SignUpLambda createSignUpLambda() {
        return new SignUpLambda(findByEmail,
                                insert,
                                getAddresses,
                                sendEmail,
                                count,
                                getTimestamp,
                                createEmail);
    }
}