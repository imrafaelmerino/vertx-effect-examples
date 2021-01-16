package vertx.effect.examples.signup;

import io.vertx.core.Future;
import jsonvalues.*;
import vertx.effect.Val;
import vertx.effect.exp.Cons;
import vertx.effect.exp.IfElse;
import vertx.effect.exp.JsObjExp;
import vertx.effect.exp.Pair;
import vertx.effect.λ;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static vertx.effect.examples.signup.ClientEntity.*;

class SignUpLambda implements λ<JsObj, JsObj> {

    private final λ<String, Optional<JsObj>> findByEmail;
    private final λ<JsObj, String> insert;
    private final λ<String, JsArray> getAddresses;
    private final λ<JsObj, Void> sendEmail;
    private final λ<Void, Long> count;
    private final λ<Void, Instant> getTimestamp;
    private final Function<JsObj, JsObj> createEmail;

    SignUpLambda(final λ<String, Optional<JsObj>> findByEmail,
                 final λ<JsObj, String> insert,
                 final λ<String, JsArray> getAddresses,
                 final λ<JsObj, Void> sendEmail,
                 final λ<Void, Long> count,
                 final λ<Void, Instant> getTimestamp,
                 final Function<JsObj, JsObj> createEmail) {

        this.findByEmail = requireNonNull(findByEmail);
        this.insert = requireNonNull(insert);
        this.getAddresses = requireNonNull(getAddresses);
        this.sendEmail = requireNonNull(sendEmail);
        this.count = requireNonNull(count);
        this.getTimestamp = requireNonNull(getTimestamp);
        this.createEmail = requireNonNull(createEmail);
    }

    @Override
    public Val<JsObj> apply(final JsObj client) {
        String email = EMAIL_LENS.get.apply(client);
        return
                IfElse.<JsObj>predicate(findByEmail.apply(email)
                                                   .map(Optional::isPresent))
                        .consequence(Cons.failure(new ClientAlreadyExists(email)))
                        .alternative(JsObjExp.parallel("users",
                                                       count.apply(null)
                                                            .map(JsLong::of)
                                                            .recover(e -> JsLong.of(-1)),
                                                       "timestamp",
                                                       getTimestamp.apply(null)
                                                                   .map(JsInstant::of),
                                                       "id",
                                                       Pair.sequential(insert.apply(client)
                                                                             .map(JsStr::of),
                                                                       IfElse.<Void>predicate(EMAIL_VALIDATED_LENS.get.apply(client))
                                                                               .consequence(Cons.NULL)
                                                                               .alternative(sendEmailAsync(createEmail.apply(client))
                                                                               ))
                                                           .map(pair -> pair._1),
                                                       "addresses",
                                                       getAddresses.apply(ADDRESS_LENS.get.apply(client))
                                                                   .recover(e -> JsArray.empty()
                                                                   )
                                     )
                        );
    }


    private Val<Void> sendEmailAsync(final JsObj client) {
        return Cons.of(() -> {
                           sendEmail.apply(client)
                                    .get();
                           return Future.succeededFuture(null);
                       }
        );

    }
}
