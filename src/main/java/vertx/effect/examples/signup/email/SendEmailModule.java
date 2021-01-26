package vertx.effect.examples.signup.email;

import io.vertx.core.DeploymentOptions;
import jsonvalues.JsObj;
import lombok.Builder;
import vertx.effect.*;
import vertx.effect.exp.Cons;

import java.util.Properties;
import java.util.function.Function;

import static vertx.effect.examples.signup.email.EmailFailures.CONNECTION_TIMEOUT;
import static vertx.effect.examples.signup.email.EmailFailures.UNKNOWN_HOST;

@Builder
public class SendEmailModule extends VertxModule {

    public λ<JsObj, Void> sendEmail;
    public λ<JsObj, JsObj> validateEmail;

    @Builder.Default
    private Function<Integer, RetryPolicy<Throwable>> retryPolicy = attempts -> (remaining, error) -> Cons.NULL;
    @Builder.Default
    private int instances = 1;
    @Builder.Default
    private int failureAttempts = 3;
    @Builder.Default
    public String sendEmailAddress = "send-email";
    @Builder.Default
    public String validateEmailAddress = "validate-email";

    private String host;
    private Properties props;
    private String user;
    private byte[] password;
    private String from;
    private String fromName;
    private String configSet;


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
                                                          retryPolicy.apply(failureAttempts)
                                                   );
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
