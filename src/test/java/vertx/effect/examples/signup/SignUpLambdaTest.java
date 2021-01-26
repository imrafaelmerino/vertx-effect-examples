package vertx.effect.examples.signup;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import jsonvalues.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import vertx.effect.exp.Cons;

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
                            .findByEmail(email -> Cons.success(Optional.of(gen.get())))
                            .count(fail(RuntimeException::new))
                            .getAddresses(fail(RuntimeException::new))
                            .sendEmail(fail(RuntimeException::new))
                            .getTimestamp(fail(RuntimeException::new))
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
                            .findByEmail(email -> Cons.success(Optional.ofNullable(null)))
                            .count(e -> Cons.success(10L))
                            .getAddresses(e -> Cons.success(JsArray.empty()))
                            .sendEmail(fail(RuntimeException::new))
                            .getTimestamp(e -> Cons.success(Instant.now()))
                            .insert(e -> Cons.failure(new RuntimeException("Something bad happened!")))
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
                            .findByEmail(email -> Cons.success(Optional.ofNullable(null)))
                            .count(e -> Cons.success(10L))
                            .getAddresses(e -> Cons.success(JsArray.empty()))
                            .getTimestamp(e -> Cons.success(now))
                            .sendEmail(fail(RuntimeException::new))
                            .insert(e -> Cons.success("id"))
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
