package vertx.effect.examples.slides.vertxeffectintro;


import io.vertx.core.Future;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vertx.effect.Val;
import vertx.effect.exp.Cons;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
                                         ChronoUnit.MILLIS));


        Assertions.assertNotEquals(100L,
                                   Duration.between(now.result(),
                                                    nowPlus100Millis.result())
                                           .toMillis());

        //we can't refactor Future.succeededFuture(Instant.now())

        //only use future.result() for testing
    }

    @Test
    public void val_is_referential_transparent() {

        Val<Instant> now = Cons.of(()->Future.succeededFuture(Instant.now()));

        Val<Instant> nowPlus100Millis =
                now.map(it -> it.plus(100,
                                         ChronoUnit.MILLIS));


        Assertions.assertNotEquals(100L,
                                   Duration.between(now.get().result(),
                                                    nowPlus100Millis.get().result())
                                           .toMillis());

        //we CAN refactor Future.succeededFuture(Instant.now())

        // what would've happened if now = Cons.success(Instant.now())

        // remember java is strict and values are immutable
    }

    


}
