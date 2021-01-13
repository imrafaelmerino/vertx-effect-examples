package vertx.effect.examples.signup;

import vertx.effect.examples.FunctionsModule;
import vertx.effect.examples.signup.email.SendEmailModule;
import vertx.effect.examples.signup.geocode.GeolocationModule;
import vertx.effect.examples.signup.mongodb.ClientDAOModule;
import vertx.effect.λ;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public class SignUpModuleBuilder {

    private SendEmailModule emailModule;
    private GeolocationModule geolocationModule;
    private ClientDAOModule clientModule;
    private FunctionsModule functionsModule;
    private String validateAddress = "validate-signup-message";
    private String signupAddress = "signup";

    public SignUpModuleBuilder setEmailModule(final SendEmailModule emailModule) {
        this.emailModule = requireNonNull(emailModule);
        return this;
    }

    public SignUpModuleBuilder setFunctionsModule(final FunctionsModule functionsModule) {
        this.functionsModule = requireNonNull(functionsModule);
        return this;
    }

    public SignUpModuleBuilder setGeolocationModule(final GeolocationModule geolocationModule) {
        this.geolocationModule = requireNonNull(geolocationModule);
        return this;
    }

    public SignUpModuleBuilder setClientDAOModule(final ClientDAOModule clientModule) {
        this.clientModule = requireNonNull(clientModule);
        return this;
    }

    public SignUpModuleBuilder setValidateAddress(final String validateAddress) {
        this.validateAddress = requireNonNull(validateAddress);
        return this;
    }

    public SignUpModuleBuilder setSignupAddress(final String signupAddress) {
        this.signupAddress = requireNonNull(signupAddress);
        return this;
    }

    public SignUpModule createModule() {
        return new SignUpModule(emailModule,
                                geolocationModule,
                                clientModule,
                                functionsModule,
                                validateAddress,
                                signupAddress);
    }

    public String getValidateAddress() {
        return validateAddress;
    }

    public String getSignupAddress() {
        return signupAddress;
    }

}