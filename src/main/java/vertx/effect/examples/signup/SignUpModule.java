package vertx.effect.examples.signup;

import jsonvalues.JsObj;
import vertx.effect.Validators;
import vertx.effect.VertxModule;
import vertx.effect.examples.FunctionsModule;
import vertx.effect.examples.signup.email.SendEmailModule;
import vertx.effect.examples.signup.geocode.GeolocationModule;
import vertx.effect.examples.signup.mongodb.ClientDAOModule;
import vertx.effect.λ;

import static java.util.Objects.requireNonNull;
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
public class SignUpModule extends VertxModule {

    private final SendEmailModule emailModule;
    private final GeolocationModule geolocationModule;
    private final ClientDAOModule clientModule;
    private final FunctionsModule functionsModule;
    private final String validateAddress;
    private final String signupAddress;


    SignUpModule(final SendEmailModule emailModule,
                 final GeolocationModule geolocationModule,
                 final ClientDAOModule clientModule,
                 final FunctionsModule functionsModule,
                 final String validateAddress,
                 final String signupAddress) {
        this.emailModule = requireNonNull(emailModule);
        this.geolocationModule = requireNonNull(geolocationModule);
        this.clientModule = requireNonNull(clientModule);
        this.validateAddress = requireNonNull(validateAddress);
        this.signupAddress = requireNonNull(signupAddress);
        this.functionsModule = requireNonNull(functionsModule);
    }

    public λ<JsObj, JsObj> validate;
    public λ<JsObj, JsObj> signup;


    @Override
    protected void deploy() {
        deploy(validateAddress,
               Validators.validateJsObj(CLIENT_SPEC));

        deploy(signupAddress,
               new SignUpLambdaBuilder().setCount(clientModule.countAll)
                                        .setFindByEmail(clientModule.findByEmail)
                                        .setGetAddresses(geolocationModule.getAddresses)
                                        .setGetTimestamp(functionsModule.getTimestamp)
                                        .setInsert(clientModule.insert)
                                        .setSendEmail(emailModule.sendEmail)
                                        .createSignUpLambda());
    }

    @Override
    protected void initialize() {
        validate = ask(validateAddress);
        signup = ask(signupAddress);
    }

}
