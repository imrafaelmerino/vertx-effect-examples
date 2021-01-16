package vertx.effect.examples.codecs;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import vertx.effect.core.Functions;


import java.time.Instant;

public class RegisterInstantCodec extends AbstractVerticle {

    @Override
    public void start(Promise<Void> promise) {
        try {
            this.vertx.eventBus()
                      .registerDefaultCodec(Instant.class,
                                            InstantCodec.INSTANCE);

            promise.complete();
        } catch (Exception var3) {
            promise.fail(new ReplyException(ReplyFailure.RECIPIENT_FAILURE,
                                            3002,
                                            Functions.getErrorMessage(var3)));
        }

    }
}
