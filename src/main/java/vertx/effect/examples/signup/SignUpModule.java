package vertx.effect.examples.signup;

import jsonvalues.JsObj;
import lombok.Builder;
import vertx.effect.Validators;
import vertx.effect.VertxModule;
import vertx.effect.examples.FunctionsModule;
import vertx.effect.examples.signup.email.SendEmailModule;
import vertx.effect.λ;

import static vertx.effect.examples.signup.ClientEntity.CLIENT_SPEC;


/**
 sign up service: receives a json and:
 - validates input json
 - checks if email doesn't exist in database
 - validates address using Google Map service
 - persists in database
 - sends an email to the user
 -
 */
@Builder
public class SignUpModule extends VertxModule {

    private final SendEmailModule emailModule;
    private final GeolocationModule geolocationModule;
    private final ClientDAOModule clientModule;
    private final FunctionsModule functionsModule;
    @Builder.Default
    private String validateAddress = "validate-signup-message";
    @Builder.Default
    private String signupAddress = "signup";

    public λ<JsObj, JsObj> validate;
    public λ<JsObj, JsObj> signup;


    @Override
    protected void deploy() {
        deploy(validateAddress,
               Validators.validateJsObj(CLIENT_SPEC));

        deploy(signupAddress,
               SignUpLambda.builder()
                           .count(() -> clientModule.countAll.apply(null))
                           .findByEmail(clientModule.findByEmail)
                           .getAddresses(geolocationModule.getAddresses)
                           .getTimestamp(() -> functionsModule.getTimestamp.apply(null))
                           .insert(clientModule.insert)
                           .sendEmail(emailModule.sendEmail)
                           .parseToJson(functionsModule.str2JsObj)
                           .createTimer(d -> vertxRef.sleep(d))
                           .build());
    }

    @Override
    protected void initialize() {
        validate = ask(validateAddress);
        signup = ask(signupAddress);
    }

}
