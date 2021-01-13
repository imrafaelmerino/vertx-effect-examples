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
        SignUpLambdaBuilder builder =
                new SignUpLambdaBuilder()
                        .setFindByEmail(email -> Cons.success(Optional.of(gen.get())))
                        .setCount(fail(RuntimeException::new))
                        .setGetAddresses(fail(RuntimeException::new))
                        .setSendEmail(fail(RuntimeException::new))
                        .setGetTimestamp(fail(RuntimeException::new))
                        .setInsert(fail(RuntimeException::new));


        builder.createSignUpLambda()
               .apply(gen.get())
               .onComplete(it -> context.verify(() -> {
                   Assertions.assertTrue(it.failed());
                   context.completeNow();
               }))
               .get();


    }

    @Test
    public void test_client_inserts_mongodb_fails(VertxTestContext context) {


        Supplier<JsObj> gen = ClientEntity.CLIENT_GEN.apply(new Random());
        SignUpLambdaBuilder builder =
                new SignUpLambdaBuilder()
                        .setFindByEmail(email -> Cons.success(Optional.ofNullable(null)))
                        .setCount(e -> Cons.success(10L))
                        .setGetAddresses(e -> Cons.success(JsArray.empty()))
                        .setSendEmail(fail(RuntimeException::new))
                        .setGetTimestamp(e -> Cons.success(Instant.now()))
                        .setInsert(e -> Cons.failure(new RuntimeException("Something bad happened!")));


        builder.createSignUpLambda()
               .apply(gen.get())
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
        SignUpLambdaBuilder builder =
                new SignUpLambdaBuilder()
                        .setFindByEmail(email -> Cons.success(Optional.ofNullable(null)))
                        .setCount(e -> Cons.success(10L))
                        .setGetAddresses(e -> Cons.success(JsArray.empty()))
                        .setGetTimestamp(e -> Cons.success(now))
                        .setSendEmail(fail(RuntimeException::new))
                        .setInsert(e -> Cons.success("id"));


        builder.createSignUpLambda()
               .apply(gen.get())
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
