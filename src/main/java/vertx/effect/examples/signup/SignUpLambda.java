package vertx.effect.examples.signup;

import io.vertx.core.Future;
import jsonvalues.JsInstant;
import jsonvalues.JsLong;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import lombok.Builder;
import vertx.effect.Failures;
import vertx.effect.Timer;
import vertx.effect.Val;
import vertx.effect.examples.signup.email.EmailEntity;
import vertx.effect.exp.IfElse;
import vertx.effect.exp.JsObjExp;
import vertx.effect.exp.Pair;
import vertx.effect.λ;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static vertx.effect.RetryPolicies.equalJitter;
import static vertx.effect.RetryPolicies.limitRetries;
import static vertx.effect.examples.signup.ClientEntity.*;
import static vertx.effect.examples.signup.email.EmailEntity.*;
import static vertx.effect.examples.signup.email.EmailFailures.CONNECTION_TIMEOUT;
import static vertx.effect.examples.signup.email.EmailFailures.UNKNOWN_HOST;
import static vertx.mongodb.effect.MongoFailures.MONGO_CONNECT_TIMEOUT_CODE;
import static vertx.mongodb.effect.MongoFailures.MONGO_READ_TIMEOUT_CODE;

@Builder
class SignUpLambda implements λ<JsObj, JsObj> {

    private λ<String, Optional<JsObj>> findByEmail;
    private λ<JsObj, String> insert;
    private λ<String, JsObj> getAddresses;
    private λ<JsObj, Void> sendEmail;
    private Supplier<Val<Long>> count;
    private Supplier<Val<Instant>> getTimestamp;
    private λ<String, JsObj> parseToJson;
    private Function<Duration, Timer> createTimer;


    @Builder.Default
    private Function<JsObj, JsObj> createEmail =
            client -> {
                String to = EMAIL_LENS.get.apply(client);
                String name = NAME_LENS.get.apply(client);
                return bodyLens.set.apply("Welcome " + name + "!")
                                   .andThen(subjectLens.set.apply("signup"))
                                   .andThen(EmailEntity.contentTypeLens.set.apply("text/html"))
                                   .andThen(toLens.set.apply(to))
                                   .apply(client);
            };


    @Override
    public Val<JsObj> apply(final JsObj client) {
        String email = EMAIL_LENS.get.apply(client);
        return
                IfElse.<JsObj>predicate(findByEmail.apply(email)
                                                   .map(Optional::isPresent))
                        .consequence(Val.fail(new ClientAlreadyExists(email)))
                        .alternative(JsObjExp.parallel("users",
                                                       count.get()
                                                            .retry(Failures.anyOf(MONGO_CONNECT_TIMEOUT_CODE,
                                                                                  MONGO_READ_TIMEOUT_CODE),
                                                                   limitRetries(3))
                                                            .map(JsLong::of)
                                                            .recover(e -> JsLong.of(-1)),
                                                       "timestamp",
                                                       getTimestamp.get()
                                                                   .map(JsInstant::of),
                                                       "id",
                                                       Pair.sequential(insert.apply(client)
                                                                             .retry(Failures.anyOf(MONGO_CONNECT_TIMEOUT_CODE),
                                                                                    limitRetries(3))
                                                                             .map(JsStr::of),
                                                                       IfElse.<Void>predicate(EMAIL_VALIDATED_LENS.get.apply(client))
                                                                               .consequence(Val.NULL)
                                                                               .alternative(sendEmailAsync(createEmail.apply(client))
                                                                               ))
                                                           .map(pair -> pair._1),
                                                       "addresses",
                                                       getAddresses.apply(client.getStr("address"))
                                                                   .repeat(resp -> resp.getInt("status_code") != 200,
                                                                                   limitRetries(3).limitRetriesByCumulativeDelay(Duration.ofSeconds(2)))
                                                                   .retry(limitRetries(3))
                                                                   .flatMap(resp -> parseToJson.apply(resp.getStr("body")))
                                                                   .map(json -> json.getArray("results"))

                                     )
                        );
    }


    private Val<Void> sendEmailAsync(final JsObj client) {
        return Val.effect(() -> {
                              sendEmail.apply(client)
                                       .retry(Failures.anyOf(CONNECTION_TIMEOUT,
                                                             UNKNOWN_HOST),
                                              limitRetries(10)
                                                      .append(equalJitter(Duration.ofMillis(10),
                                                                          Duration.ofMillis(10000),
                                                                          createTimer)))
                                       .get();
                              return Future.succeededFuture(null);
                          }
        );

    }
}
