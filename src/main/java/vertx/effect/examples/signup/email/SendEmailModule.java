package vertx.effect.examples.signup.email;

import jsonvalues.JsObj;
import lombok.Builder;
import vertx.effect.*;

import java.time.Duration;
import java.util.Properties;

import static vertx.effect.RetryPolicies.equalJitter;
import static vertx.effect.RetryPolicies.limitRetries;
import static vertx.effect.examples.signup.email.EmailFailures.CONNECTION_TIMEOUT;
import static vertx.effect.examples.signup.email.EmailFailures.UNKNOWN_HOST;


@Builder
public class SendEmailModule extends VertxModule {

    public λ<JsObj, Void> sendEmail;
    public λ<JsObj, JsObj> validateEmail;
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
    }

    @Override
    protected void initialize() {
        sendEmail = vertxRef.spawn(sendEmailAddress,
                                   new SendEmailLambda(host,
                                                       props,
                                                       user,
                                                       password,
                                                       from,
                                                       fromName,
                                                       configSet));
        validateEmail = vertxRef.spawn(validateEmailAddress,
                                       Validators.validateJsObj(EmailEntity.spec));
    }

}
