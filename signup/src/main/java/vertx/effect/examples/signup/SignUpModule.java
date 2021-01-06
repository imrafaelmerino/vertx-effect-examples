package vertx.effect.examples.signup;

import jsonvalues.JsObj;
import vertx.effect.Validators;
import vertx.effect.VertxModule;
import vertx.effect.examples.signup.email.SendEmailModule;
import vertx.effect.examples.signup.geocode.GeolocationModule;
import vertx.effect.examples.signup.mongodb.ClientDAOModule;
import vertx.effect.exp.Cons;
import vertx.effect.λ;

import java.time.Instant;

import static java.util.Objects.requireNonNull;
import static vertx.effect.examples.signup.Client.CLIENT_SPEC;


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
    private static final String VALIDATE_AND_SIGNUP_ADDRESS = "validate_and_signup";

    public SignUpModule(final SendEmailModule emailModule,
                        final GeolocationModule geolocationModule,
                        final ClientDAOModule clientModule) {
        this.emailModule = requireNonNull(emailModule);
        this.geolocationModule = requireNonNull(geolocationModule);
        this.clientModule = requireNonNull(clientModule);
    }

    public λ<JsObj, JsObj> validateAndSignUp;


    λ<Void, Instant> getTimestamp = nill -> Cons.success(Instant.now());


    @Override
    protected void deploy() {

        λ<JsObj, JsObj> validate = Validators.validateJsObj(CLIENT_SPEC);

        deploy("validate",
               validate);

        final λ<JsObj, JsObj> signup =
                new SignUpLambdaBuilder().setCount(clientModule.countAll)
                                         .setFindByEmail(clientModule.findByEmail)
                                         .setGetAddresses(geolocationModule.getAddresses)
                                         .setGetTimestamp(getTimestamp)
                                         .setInsert(clientModule.insert)
                                         .setSendEmail(emailModule.sendEmail)
                                         .createSignUpLambda();

        deploy("signup",
               signup);

        deploy(VALIDATE_AND_SIGNUP_ADDRESS,
               validate.andThen(signup));


    }

    @Override
    protected void initialize() {
        validateAndSignUp = ask(VALIDATE_AND_SIGNUP_ADDRESS);
    }

}
