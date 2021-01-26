package vertx.effect.examples.signup;

import io.vertx.core.Future;
import jsonvalues.*;
import lombok.Builder;
import vertx.effect.Val;
import vertx.effect.examples.signup.email.EmailEntity;
import vertx.effect.exp.Cons;
import vertx.effect.exp.IfElse;
import vertx.effect.exp.JsObjExp;
import vertx.effect.exp.Pair;
import vertx.effect.λ;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

import static vertx.effect.examples.signup.ClientEntity.*;
import static vertx.effect.examples.signup.email.EmailEntity.*;

@Builder
class SignUpLambda implements λ<JsObj, JsObj> {

    private λ<String, Optional<JsObj>> findByEmail;
    private λ<JsObj, String> insert;
    private λ<String, JsArray> getAddresses;
    private λ<JsObj, Void> sendEmail;
    private λ<Void, Long> count;
    private λ<Void, Instant> getTimestamp;
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
