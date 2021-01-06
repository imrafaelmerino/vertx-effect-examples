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
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static vertx.effect.examples.signup.Client.*;

 class SignUpLambda implements λ<JsObj, JsObj> {

    private final λ<String, Optional<JsObj>> findByEmail;
    private final λ<JsObj, String> insert;
    private final λ<String, JsArray> getAddresses;
    private final λ<JsObj, Void> sendEmail;
    private final λ<Void, Long> count;
    private final λ<Void, Instant> getTimestamp;

    public SignUpLambda(final λ<String, Optional<JsObj>> findByEmail,
                        final λ<JsObj, String> insert,
                        final λ<String, JsArray> getAddresses,
                        final λ<JsObj, Void> sendEmail,
                        final λ<Void, Long> count,
                        final λ<Void, Instant> getTimestamp) {
        this.findByEmail = requireNonNull(findByEmail);
        this.insert = requireNonNull(insert);
        this.getAddresses = requireNonNull(getAddresses);
        this.sendEmail = requireNonNull(sendEmail);
        this.count = requireNonNull(count);
        this.getTimestamp = requireNonNull(getTimestamp);
    }

    @Override
    public Val<JsObj> apply(final JsObj body) {
        return
                IfElse.<JsObj>predicate(findByEmail.apply(EMAIL_LENS.get.apply(body))
                                                   .map(Optional::isPresent))
                        .consequence(Cons.failure(new IllegalStateException("already exists")))
                        .alternative(JsObjExp.parallel("users",
                                                       count.apply(null)
                                                            .map(JsLong::of),
                                                       "timestamp",
                                                       getTimestamp.apply(null)
                                                                   .map(JsInstant::of),
                                                       "id",
                                                       Pair.sequential(insert.apply(body.delete("address"))
                                                                             .map(JsStr::of),
                                                                       IfElse.<Void>predicate(Cons.success(IS_EMAIL_VALIDATED_LENS.get.apply(body)))
                                                                             .consequence(Cons.NULL)
                                                                             .alternative(Cons.of(sendEmailAsync(body)))
                                                       )
                                                           ._1(),
                                                       "addresses",
                                                       getAddresses.apply(ADDRESS_LENS.get.apply(body))
                                     )
                        );
    }

    private Supplier<Future<Void>> sendEmailAsync(final JsObj body) {
        return () -> {
            sendEmail.apply(body);
            return Future.succeededFuture(null);
        };
    }
}
