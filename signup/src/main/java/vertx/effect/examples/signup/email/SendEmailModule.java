package vertx.effect.examples.signup.email;

import io.vertx.core.DeploymentOptions;
import jsonvalues.JsObj;
import vertx.effect.*;

import java.util.Properties;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static vertx.effect.examples.signup.email.EmailFailures.CONNECTION_TIMEOUT;
import static vertx.effect.examples.signup.email.EmailFailures.UNKNOWN_HOST;

public class SendEmailModule extends VertxModule {

    public λ<JsObj, Void> sendEmail;
    public λ<JsObj, JsObj> validateEmail;

    public final String sendEmailAddress;
    public final String validateEmailAddress;

    private final String host;
    private final Properties props;
    private final String user;
    private final byte[] password;
    private final String from;
    private final String fromName;
    private final String configSet;
    private final int instances;
    private final int failureAttempts;
    private final Function<Integer, RetryPolicy<Throwable>> retryPolicy;

    public SendEmailModule(final String host,
                           final Properties props,
                           final String user,
                           final byte[] password,
                           final String from,
                           final String fromName,
                           final String configSet,
                           final int instances,
                           final String validateEmailAddress,
                           final String sendEmailAddress,
                           final int failureAttempts,
                           final Function<Integer, RetryPolicy<Throwable>> retryPolicy) {
        this.host = requireNonNull(host);
        this.props = requireNonNull(props);
        this.user = requireNonNull(user);
        this.password = requireNonNull(password);
        this.from = requireNonNull(from);
        this.fromName = requireNonNull(fromName);
        this.configSet = configSet;
        if (instances < 1) throw new IllegalArgumentException("instances < 1");
        this.instances = instances;
        this.sendEmailAddress = requireNonNull(sendEmailAddress);
        this.validateEmailAddress = requireNonNull(validateEmailAddress);
        if (failureAttempts < 1) throw new IllegalArgumentException("failureAttempts < 1");
        this.failureAttempts = failureAttempts;
        this.retryPolicy = requireNonNull(retryPolicy);
    }

    @Override
    protected void deploy() {

        λ<JsObj, Void> send = new SendEmailLambda(host,
                                                  props,
                                                  user,
                                                  password,
                                                  from,
                                                  fromName,
                                                  configSet);

        λ<JsObj, Void> reactiveSend = email -> send.apply(email)
                                                   .retry(Failures.anyOf(CONNECTION_TIMEOUT,
                                                                         UNKNOWN_HOST),
                                                          failureAttempts,
                                                          retryPolicy.apply(failureAttempts));
        deploy(sendEmailAddress,
               reactiveSend,
               new DeploymentOptions().setWorker(true)
                                      .setInstances(instances)
        );

        deploy(validateEmailAddress,
               Validators.validateJsObj(EmailEntity.spec)
        );


    }

    @Override
    protected void initialize() {
        sendEmail = ask(sendEmailAddress);
        validateEmail = ask(validateEmailAddress);
    }

}
