package vertx.effect.examples.slides.vertxeffectintro;


import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import vertx.effect.RetryPolicies;
import vertx.effect.Val;

import java.time.Duration;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.MILLIS;

@ExtendWith(VertxExtension.class)
public class ValuesExamples {

    @Test
    public void creates_value_from_constant() {

    }

    @Test
    public void creates_value_from_error() {

    }

    @Test
    public void creates_value_from_supplier() {

    }

    @Test
    public void future_is_not_referential_transparent() {

        Future<Instant> now = Future.succeededFuture(Instant.now());

        Future<Instant> nowPlus100Millis =
                Future.succeededFuture(Instant.now())
                      .map(it -> it.plus(100,
                                         MILLIS)
                      );


        Assertions.assertNotEquals(100,
                                   Duration.between(now.result(),
                                                    nowPlus100Millis.result())
                                           .toMillis());

        //we can't refactor Future.succeededFuture(Instant.now())

        //only use future.result() for testing
    }

    @Test
    public void val_is_referential_transparent() {

        Val<Instant> now = Val.effect(() -> Future.succeededFuture(Instant.now()));

        Val<Instant> nowPlus100Millis =
                now.map(it -> it.plus(100,
                                      MILLIS));


        Assertions.assertNotEquals(100,
                                   Duration.between(now.get()
                                                       .result(),
                                                    nowPlus100Millis.get()
                                                                    .result())
                                           .toMillis());
        // we CAN refactor Future.succeededFuture(Instant.now())
        // what would've happened if now = Cons.success(Instant.now())
        // remember java is strict and values are immutable
    }

    @Test
    public void test_map() {

/*        str(10).map(String::toUpperCase)
               .onSuccess(it -> Character.isUpperCase(it));*/
    }


    @Test
    @RepeatedTest(10)
    public void val_flatmap(VertxTestContext context) {
        int maxLength = 10;
        integer(maxLength)
                .flatMap(n -> str(n))
                .onSuccess(s -> {
                    context.verify(() -> Assertions.assertTrue(s.length() <= maxLength));
                    context.completeNow();
                })
                .get();
    }

    @Test
    @RepeatedTest(10)
    public void val_flatmap_retry_failure(VertxTestContext context) {
        int maxLength = 10;
        integerOrError(maxLength)
                .flatMap(n -> strOrError(n))
                .onSuccess(s -> {
                    context.verify(() -> Assertions.assertTrue(s.length() <= maxLength));
                    context.completeNow();
                })
                .retry(RetryPolicies.limitRetries(5))
                .get();
    }


    static Val<Integer> integer(int max) {
        return Val.effect(() -> Future.succeededFuture(Generators.intGen(max)));
    }

    static Val<String> str(int max) {
        return Val.effect(() -> Future.succeededFuture(Generators.strGen(max)));
    }

    static Val<String> strOrError(int length) {
        return Val.effect(() -> {
            String n = Generators.strGen(length);
            if (n.contains("a")) {
                String message = "Error: " + n + " contains a";
                System.out.println(message);
                return Future.failedFuture(new RuntimeException(message));
            }
            return Future.succeededFuture(n);
        });
    }

    static Val<Integer> integerOrError(int max) {
        return Val.effect(() -> {
            int i = Generators.intGen(max);
            if (i % 7 == 0) {
                String message = "Error: " + i + " divisible by 7";
                System.out.println(message);
                return Future.failedFuture(new RuntimeException(message));
            }
            return Future.succeededFuture(i);
        });
    }


}
