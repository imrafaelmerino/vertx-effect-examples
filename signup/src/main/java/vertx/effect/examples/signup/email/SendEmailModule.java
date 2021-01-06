package vertx.effect.examples.signup.email;

import io.vertx.core.DeploymentOptions;
import jsonvalues.JsObj;
import vertx.effect.*;

import java.util.Properties;
import java.util.function.Function;

import static vertx.effect.examples.signup.email.EmailFailures.CONNECTION_TIMEOUT;
import static vertx.effect.examples.signup.email.EmailFailures.UNKNOWN_HOST;

public class SendEmailModule extends VertxModule {


    public λ<JsObj, Void> sendEmail;

    private final String host;
    private final Properties props;
    private final String user;
    private final byte[] password;
    private final String from;
    private final String fromName;
    private final String configSet;
    private final int instances;
    private final String address;
    private final String validateAddress;
    private final int failureAttempts;
    private Function<Integer, RetryPolicy<Throwable>> retryPolicy;

    public SendEmailModule(final String host,
                           final Properties props,
                           final String user,
                           final byte[] password,
                           final String from,
                           final String fromName,
                           final String configSet,
                           final int instances,
                           final String address,
                           final int failureAttempts,
                           final Function<Integer, RetryPolicy<Throwable>> retryPolicy) {
        this.host = host;
        this.props = props;
        this.user = user;
        this.password = password;
        this.from = from;
        this.fromName = fromName;
        this.configSet = configSet;
        this.instances = instances;
        this.address = address;
        this.validateAddress = String.format("validate-%s",
                                             address);
        this.failureAttempts = failureAttempts;
        this.retryPolicy = retryPolicy;
    }

    @Override
    protected void deploy() {

        deploy(address,
               new SendEmailLambda(host,
                                   props,
                                   user,
                                   password,
                                   from,
                                   fromName,
                                   configSet),
               new DeploymentOptions().setWorker(true)
                                      .setInstances(instances)
        );

        deploy(validateAddress,
               Validators.validateJsObj(Email.spec)
        );

    }

    @Override
    protected void initialize() {
        λ<JsObj, Void> send = ask(address);
        λ<JsObj, JsObj> validate = ask(validateAddress);

        sendEmail = email -> validate.apply(email)
                                     .flatMap(_email -> send.apply(email)
                                                            .retry(Failures.anyOf(CONNECTION_TIMEOUT,
                                                                                  UNKNOWN_HOST),
                                                                   failureAttempts,
                                                                   retryPolicy.apply(failureAttempts))
                                     )
        ;
    }

}
