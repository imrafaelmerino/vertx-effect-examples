package vertx.effect.examples.signup.email;

import vertx.effect.RetryPolicy;
import vertx.effect.exp.Cons;

import java.util.Properties;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class SendEmailModuleBuilder {

    private String host;
    private Properties props;
    private String user;
    private byte[] password;
    private String from;
    private String fromName;
    private String configSet;
    private String address;
    private int instances = 1;

    private Function<Integer, RetryPolicy<Throwable>> retryPolicy =
            attempts -> (remaining, Throwable) -> Cons.NULL;

    private int failureAttempts=3;


    public SendEmailModuleBuilder setAddress(final String address) {
        this.address = requireNonNull(address);
        return this;
    }

    public SendEmailModuleBuilder setRetryPolicy(Function<Integer, RetryPolicy<Throwable>> retryPolicy) {
        this.retryPolicy = requireNonNull(retryPolicy);
        return this;
    }

    public SendEmailModuleBuilder setFailureAttempts(final int attempts) {
        this.failureAttempts = attempts;
        return this;
    }

    public SendEmailModuleBuilder setInstances(final int instances) {
        this.instances = instances;
        return this;
    }

    public SendEmailModuleBuilder setHost(final String host) {
        this.host = requireNonNull(host);
        return this;
    }

    public SendEmailModuleBuilder setConfigSet(final String configSet) {
        this.configSet = requireNonNull(configSet);
        return this;
    }

    public SendEmailModuleBuilder setProps(final Properties props) {
        this.props = requireNonNull(props);
        return this;
    }

    public SendEmailModuleBuilder setUser(final String user) {
        this.user = requireNonNull(user);
        return this;
    }

    public SendEmailModuleBuilder setPassword(final byte[] password) {
        this.password = requireNonNull(password);
        return this;
    }

    public SendEmailModuleBuilder setFrom(final String from) {
        this.from = requireNonNull(from);
        return this;
    }

    public SendEmailModuleBuilder setFromName(final String fromName) {
        this.fromName = requireNonNull(fromName);
        return this;
    }

    public SendEmailModule createModule() {
        return new SendEmailModule(host,
                                   props,
                                   user,
                                   password,
                                   from,
                                   fromName,
                                   configSet,
                                   instances,
                                   address,
                                   failureAttempts,
                                   retryPolicy
        );
    }
}