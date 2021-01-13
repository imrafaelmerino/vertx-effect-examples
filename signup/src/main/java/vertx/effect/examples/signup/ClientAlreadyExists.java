package vertx.effect.examples.signup;

import io.vertx.core.eventbus.ReplyException;

import static io.vertx.core.eventbus.ReplyFailure.RECIPIENT_FAILURE;
import static vertx.effect.examples.signup.SignupFailures.CLIENT_EXISTS;

public class ClientAlreadyExists extends ReplyException {

    public ClientAlreadyExists(String email) {
        super(RECIPIENT_FAILURE,
              CLIENT_EXISTS,
              "Client " + email + " already exists");
    }
}
