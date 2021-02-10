package vertx.effect.examples.signup;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import jsonvalues.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import vertx.effect.Val;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static vertx.effect.λ.fail;

@ExtendWith(VertxExtension.class)
public class SignUpLambdaTest {

    @Test
    public void test_client_exists(VertxTestContext context) {


        Supplier<JsObj> gen = ClientEntity.CLIENT_GEN.apply(new Random());
        SignUpLambda lambda =
                SignUpLambda.builder()
                            .findByEmail(email -> Val.succeed(Optional.of(gen.get())))
                            .count(()-> Val.fail(new RuntimeException()))
                            .getAddresses(fail(RuntimeException::new))
                            .sendEmail(fail(RuntimeException::new))
                            .getTimestamp(()->Val.fail(new RuntimeException()))
                            .insert(fail(RuntimeException::new))
                            .build();


        lambda.apply(gen.get())
              .onComplete(it -> context.verify(() -> {
                  Assertions.assertTrue(it.failed());
                  context.completeNow();
              }))
              .get();


    }

    @Test
    public void test_client_inserts_mongodb_fails(VertxTestContext context) {


        Supplier<JsObj> gen = ClientEntity.CLIENT_GEN.apply(new Random());
        SignUpLambda lambda =
                SignUpLambda.builder()
                            .findByEmail(email -> Val.succeed(Optional.ofNullable(null)))
                            .count(() -> Val.succeed(10L))
                            .getAddresses(e -> Val.succeed(JsObj.of("status_code",
                                                                    JsInt.of(200),
                                                                    "body",
                                                                    JsStr.of(""))
                                          )
                            )
                            .sendEmail(fail(RuntimeException::new))
                            .getTimestamp(() -> Val.succeed(Instant.now()))
                            .insert(e -> Val.fail(new RuntimeException("Something bad happened!")))
                            .build();


        lambda.apply(gen.get())
              .onComplete(it -> context.verify(() -> {
                  Assertions.assertTrue(it.cause() instanceof RuntimeException);
                  Assertions.assertEquals("Something bad happened!",
                                          it.cause()
                                            .getMessage());
                  context.completeNow();
              }))
              .get();


    }


    @Test
    public void test_client_inserts_mongodb_succeeds(VertxTestContext context) {


        Instant now = Instant.now();
        Supplier<JsObj> gen = ClientEntity.CLIENT_GEN.apply(new Random());
        SignUpLambda lambda =
                SignUpLambda.builder()
                            .findByEmail(email -> Val.succeed(Optional.ofNullable(null)))
                            .count(() -> Val.succeed(10L))
                            .getAddresses(e -> Val.succeed(JsObj.of("status_code",
                                                                    JsInt.of(200),
                                                                    "body",
                                                                    JsStr.of(""))))
                            .getTimestamp(() -> Val.succeed(now))
                            .sendEmail(fail(RuntimeException::new))
                            .insert(e -> Val.succeed("id"))
                            .build();


        lambda.apply(gen.get())
              .onComplete(it -> context.verify(() -> {
                  Assertions.assertEquals(JsObj.of("users",
                                                   JsLong.of(10),
                                                   "addresses",
                                                   JsArray.empty(),
                                                   "id",
                                                   JsStr.of("id"),
                                                   "timestamp",
                                                   JsInstant.of(now)
                                          ),
                                          it.result());
                  context.completeNow();
              }))
              .get();


    }


}
