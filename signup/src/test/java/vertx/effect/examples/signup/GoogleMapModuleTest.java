package vertx.effect.examples.signup;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import vertx.effect.RegisterJsValuesCodecs;
import vertx.effect.VertxRef;
import vertx.effect.examples.signup.geocode.GeolocationModule;
import vertx.effect.examples.signup.geocode.GeolocationModuleBuilder;
import vertx.effect.exp.Pair;

@ExtendWith(VertxExtension.class)
public class GoogleMapModuleTest {

    static GeolocationModule googleMapModule;


    @BeforeAll
    public static void prepare(VertxTestContext context,
                               Vertx vertx) {

        VertxRef ref = new VertxRef(vertx);

        ref.registerConsumer(VertxRef.EVENTS_ADDRESS,
                             System.out::println
        );

        HttpClientOptions options =
                new HttpClientOptions().setDefaultHost("maps.googleapis.com")
                                       .setDefaultPort(443)
                                       .setSsl(true)
                                       .setConnectTimeout(2000)
                                       .setTcpKeepAlive(true)
                                       .setTrustAll(true);

        googleMapModule =
                new GeolocationModuleBuilder("AIzaSyDxs_R8QRp8NLGHS6ArfHIRxhEicdCY9m4",
                                             options)
                        .createModule();

        Pair.sequential(ref.deployVerticle(new RegisterJsValuesCodecs()),
                        ref.deployVerticle(googleMapModule)
        )
            .onComplete(it -> {
                if (it.succeeded()) context.completeNow();
                else context.failNow(it.cause());
            })
            .get();
    }

    @Test
    public void test(VertxTestContext context) {

        googleMapModule.getAddresses
                .apply("Calle las Cruces 49, La Torre de Esteban Hambrán")
                .get()
                .onComplete(it -> {
                    if (it.succeeded()) {
                        context.verify(() -> Assertions.assertTrue(it.result()
                                                                     .isNotEmpty()));
                        context.completeNow();
                    }
                    else context.failNow(it.cause());
                });
    }
}
