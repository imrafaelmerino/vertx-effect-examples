package vertx.effect.examples.signup;

import com.mongodb.client.MongoCollection;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import jsonvalues.JsObj;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import vertx.effect.RegisterJsValuesCodecs;
import vertx.effect.RetryPolicy;
import vertx.effect.VertxRef;
import vertx.effect.examples.signup.mongodb.ClientDAOModule;
import vertx.effect.examples.signup.mongodb.ClientDAOModuleBuilder;
import vertx.effect.exp.Triple;
import vertx.mongodb.effect.MongoVertxClient;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;

@ExtendWith(VertxExtension.class)
public class ClientDaoModuleTest {

    static ClientDAOModule daoModule;

    @BeforeAll
    public static void prepare(final Vertx vertx,
                               final VertxTestContext context) {

        VertxRef vertxRef = new VertxRef(vertx);

        vertxRef.registerConsumer(VertxRef.EVENTS_ADDRESS,
                                  System.out::println);

        Function<Integer, RetryPolicy<Throwable>> retryPolicy =
                attempts -> (error, remaining) -> vertxRef.delay(1,
                                                                 SECONDS);

        String connection = "mongodb://localhost:27017/?connectTimeoutMS=3000&socketTimeoutMS=3000&serverSelectionTimeoutMS=10000";

        MongoVertxClient mongoVertxClient = new MongoVertxClient(connection);

        Supplier<MongoCollection<JsObj>> collectionSupplier =
                mongoVertxClient.getCollection("test",
                                               "Client");

        daoModule = new ClientDAOModuleBuilder(collectionSupplier).setQueryRetryPolicy(retryPolicy)
                                                                  .setQueriesFailureAttempts(25)
                                                                  .createModule();

        Triple.sequential(vertxRef.deployVerticle(new RegisterJsValuesCodecs()),
                          vertxRef.deployVerticle(mongoVertxClient),
                          vertxRef.deployVerticle(daoModule))
              .onComplete(it -> {
                  if (it.succeeded()) context.completeNow();
                  else context.failNow(it.cause());
              })
              .get();

    }

    @Test
    @Timeout(value = 60,timeUnit = SECONDS)
    public void test_find_one(VertxTestContext context) {

        daoModule.findByEmail.apply("hola")
                             .onComplete(it -> {
                                 if (it.succeeded()) context.completeNow();
                                 else context.failNow(it.cause());
                             })
                             .get();
    }

}
