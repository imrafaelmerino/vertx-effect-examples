package vertx.effect.examples.signup;

import com.mongodb.client.MongoCollection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.shell.ShellServiceOptions;
import io.vertx.ext.shell.term.TelnetTermOptions;
import jsonvalues.JsObj;
import vertx.effect.Failures;
import vertx.effect.RegisterJsValuesCodecs;
import vertx.effect.RetryPolicy;
import vertx.effect.VertxRef;
import vertx.effect.examples.FunctionsModule;
import vertx.effect.examples.codecs.RegisterInstantCodec;
import vertx.effect.examples.signup.email.SendEmailModule;
import vertx.effect.examples.signup.email.SendEmailModuleBuilder;
import vertx.effect.examples.signup.geocode.GeolocationModule;
import vertx.effect.examples.signup.geocode.GeolocationModuleBuilder;
import vertx.effect.examples.signup.mongodb.ClientDAOModule;
import vertx.effect.examples.signup.mongodb.ClientDAOModuleBuilder;
import vertx.effect.exp.ListExp;
import vertx.effect.httpserver.HttpServerBuilder;
import vertx.mongodb.effect.MongoVertxClient;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static vertx.effect.examples.signup.MainInputs.*;
import static vertx.effect.examples.signup.SignupFailures.CLIENT_EXISTS;

public class Main {

    private static Vertx vertx;
    private static VertxRef vertxRef;
    private static FunctionsModule functionsModule;
    private static GeolocationModule geolocationModule;
    private static MongoVertxClient mongoVertxClient;
    private static ClientDAOModule clientDAOModule;
    private static SendEmailModule emailModule;
    private static SignUpModule signUpModule;

    private static final Function<Integer, RetryPolicy<Throwable>> retryPolicy =
            attempts -> (error, remaining) -> vertxRef.delay(7,
                                                             SECONDS);

    public static void main(String[] args) {
        vertx = Vertx.vertx();

        vertxRef = new VertxRef(vertx);

        vertxRef.registerConsumer(VertxRef.EVENTS_ADDRESS,
                                  System.out::println);

        mongoVertxClient = new MongoVertxClient(MONGODB_CONNECTION_STR);
        clientDAOModule = getClientDAOModuleBuilder(mongoVertxClient).createModule();

        emailModule = getSendEmailModuleBuilder().createModule();
        geolocationModule = getGeolocationModuleBuilder().createModule();
        functionsModule = new FunctionsModule();

        signUpModule = new SignUpModuleBuilder()
                .setEmailModule(emailModule)
                .setFunctionsModule(functionsModule)
                .setGeolocationModule(geolocationModule)
                .setClientDAOModule(clientDAOModule)
                .createModule();

        deploy();
    }

    private static void deploy() {
        ListExp.sequential(vertxRef.deployVerticle(new RegisterJsValuesCodecs()),
                           vertxRef.deployVerticle(new RegisterInstantCodec()),
                           vertxRef.deployVerticle(mongoVertxClient),
                           vertxRef.deployVerticle(functionsModule),
                           vertxRef.deployVerticle(geolocationModule),
                           vertxRef.deployVerticle(emailModule),
                           vertxRef.deployVerticle(clientDAOModule),
                           vertxRef.deployVerticle(signUpModule))
               .flatMap($ -> vertxRef.startShellService(
                       new ShellServiceOptions().setTelnetOptions(new TelnetTermOptions().setHost("localhost")
                                                                                         .setPort(TELNET_PORT))))
               .onSuccess($ -> System.out.println("Telnet server listening on " + TELNET_PORT))
               .flatMap($ -> new HttpServerBuilder(vertx,
                                                   reqHandler()).start(HTTP_SERVER_PORT))
               .onSuccess(server -> System.out.println("Http server listening on " + HTTP_SERVER_PORT))
               .get();
    }

    private static ClientDAOModuleBuilder getClientDAOModuleBuilder(final MongoVertxClient mongoVertxClient) {
        Supplier<MongoCollection<JsObj>> collectionSupplier =
                mongoVertxClient.getCollection(MONGODB_DATABASE,
                                               MONGODB_CLIENT_COLLECTION);

        return new ClientDAOModuleBuilder(collectionSupplier)
                .setInsertFailureAttempts(MONGODB_INSERT_FAILURE_ATTEMPTS)
                .setMaxQueryTime(MONGODB_MAX_QUERY_TIME)
                .setQueriesFailureAttempts(MONGODB_QUERY_FAILURE_ATTEMPTS)
                .setInsertRetryPolicy(retryPolicy)
                .setQueryRetryPolicy(retryPolicy);
    }

    private static GeolocationModuleBuilder getGeolocationModuleBuilder() {
        return new GeolocationModuleBuilder(GEOCODE_API_KEY,
                                            new HttpClientOptions().setTrustAll(true)
                                                                   .setSsl(true)
                                                                   .setDefaultHost(GEOCODE_API_HOST)
                                                                   .setDefaultPort(443)
                                                                   .setConnectTimeout(GEOCODE_API_CONNECT_TIMEOUT)
        ).setFailureAttempts(GEOCODE_API_FAILURE_ATTEMPTS)
         .setNot2XXAttempts(GEOCODE_API_NOT_2XX_ATTEMPTS)
         .setReqTimeout(GEOCODE_API_REQ_TIMEOUT)
         .setRetryPolicy(retryPolicy);
    }

    private static Handler<HttpServerRequest> reqHandler() {
        return request -> {
            if (request.method() == HttpMethod.POST && "/clients/signup".equals(request.path())) {
                request.bodyHandler(buffer -> functionsModule.str2JsObj.andThen(signUpModule.validate)
                                                                       .andThen(signUpModule.signup)
                                                                       .apply(buffer.toString())
                                                                       .onComplete(signupHttpRespHandler(request))
                                                                       .get());
            }
            else if (request.method() == HttpMethod.GET) {
                clientDAOModule.findByEmail.apply(request.getParam("email"))
                                           .onComplete(getClientHttpRespHandler(request))
                                           .get();
            }


        };
    }

    private static Handler<AsyncResult<Optional<JsObj>>> getClientHttpRespHandler(final HttpServerRequest request) {
        return event -> {
            if (event.succeeded()) {
                Optional<JsObj> opt = event.result();
                request.response()
                       .setStatusCode(opt.isEmpty() ? 404 : 200)
                       .end(opt.isEmpty() ? "" : event.result()
                                                      .toString());
            }
            else {
                request.response()
                       .setStatusCode(500)
                       .end(event.cause()
                                 .toString());
            }
        };
    }

    private static Handler<AsyncResult<JsObj>> signupHttpRespHandler(final HttpServerRequest request) {
        return event -> {
            if (event.succeeded()) {
                request.response()
                       .setStatusCode(201)
                       .end(event.result()
                                 .toString());
            }
            else {
                Throwable cause = event.cause();
                if (Failures.anyOf(CLIENT_EXISTS)
                            .test(cause)) {
                    request.response()
                           .setStatusCode(409)
                           .end("");

                }
                else request.response()
                            .setStatusCode(500)
                            .end(event.cause()
                                      .toString());
            }
        };
    }

    private static SendEmailModuleBuilder getSendEmailModuleBuilder() {
        Properties props = new Properties();
        props.put("mail.transport.protocol",
                  "smtp"
        );
        props.put("mail.smtp.port",
                  EMAIL_API_PORT
        );
        props.put("mail.smtp.starttls.enable",
                  "true"
        );
        props.put("mail.smtp.auth",
                  "true"
        );
        props.put("mail.smtp.connectiontimeout",
                  EMAIL_API_CONNECTION_TIMEOUT
        );
        props.put("mail.smtp.timeout",
                  EMAIL_API_TIMEOUT
        );
        return new SendEmailModuleBuilder()
                .setFrom(EMAIL_FROM)
                .setFromName(EMAIL_FROM_NAME)
                .setHost(EMAIL_API_HOST)
                .setUser(EMAIL_API_USER)
                .setPassword(EMAIL_API_PASSWORD.getBytes())
                .setInstances(EMAIL_VERTICLE_INSTANCES)
                .setProps(props);
    }
}
