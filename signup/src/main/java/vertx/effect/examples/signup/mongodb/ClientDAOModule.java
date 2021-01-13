package vertx.effect.examples.signup.mongodb;

import com.mongodb.client.MongoCollection;
import io.vertx.core.DeploymentOptions;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import vertx.effect.*;
import vertx.mongodb.effect.*;
import vertx.mongodb.effect.functions.Count;
import vertx.mongodb.effect.functions.FindOne;
import vertx.mongodb.effect.functions.InsertOne;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static vertx.effect.examples.signup.ClientEntity.ADDRESS_FIELD;
import static vertx.effect.examples.signup.ClientEntity.EMAIL_FIELD;


public class ClientDAOModule extends MongoModule {


    public λ<JsObj, String> insert;
    public λ<String, Optional<JsObj>> findByEmail;
    public λ<Void, Long> countAll;

    private final String insertAddress;
    private final String findByEmailAddress;
    private final String countAllByAddress;
    private final int insertClientInstances;
    private final int maxQueryTime;
    private final int insertFailureAttempts;
    private final int queriesFailureAttempts;
    private final Function<Integer, RetryPolicy<Throwable>> insertRetryPolicy;
    private final Function<Integer, RetryPolicy<Throwable>> queryRetryPolicy;


    ClientDAOModule(final Supplier<MongoCollection<JsObj>> collection,
                    final int insertInstances,
                    final int maxQueryTime,
                    final int insertFailureAttempts,
                    final int queriesFailureAttempts,
                    final Function<Integer, RetryPolicy<Throwable>> insertRetryPolicy,
                    final Function<Integer, RetryPolicy<Throwable>> queryRetryPolicy,
                    final String insertAddress,
                    final String findByEmailAddress,
                    final String countAllByAddress) {
        super(collection);
        if (insertInstances < 1) throw new IllegalArgumentException("insertInstances < 1");
        if (maxQueryTime < 0) throw new IllegalArgumentException("maxQueryTime < 0 ");
        if (queriesFailureAttempts <= 0) throw new IllegalArgumentException("queriesFailureAttempts < 0 ");
        if (insertFailureAttempts <= 0) throw new IllegalArgumentException("insertFailureAttempts < 0 ");
        this.insertClientInstances = insertInstances;
        this.maxQueryTime = maxQueryTime;
        this.insertFailureAttempts = insertFailureAttempts;
        this.queriesFailureAttempts = queriesFailureAttempts;
        this.insertRetryPolicy = insertRetryPolicy;
        this.queryRetryPolicy = queryRetryPolicy;
        this.insertAddress = insertAddress;
        this.findByEmailAddress = findByEmailAddress;
        this.countAllByAddress = countAllByAddress;
    }


    @Override
    protected void deploy() {

        λc<JsObj, String> insert = new InsertOne<>(collectionSupplier,
                                                   Converters.insertOneResult2HexId);

        λ<JsObj, String> resilientInsert =
                client -> insert.apply(client)
                                .retry(Failures.anyOf(MongoFailures.MONGO_CONNECT_TIMEOUT_CODE,
                                                      MongoFailures.MONGO_READ_TIMEOUT_CODE),
                                       insertFailureAttempts,
                                       insertRetryPolicy.apply(insertFailureAttempts));
        deploy(insertAddress,
               resilientInsert,
               new DeploymentOptions().setInstances(insertClientInstances));

        λc<String, JsObj> findOneCommand =
                (context, email) -> findOneByEmail().apply(email);
        deploy("find-one-client-command",
               findOneCommand
        );


    }


    @Override
    protected void initialize() {

        this.insert = client -> this.<JsObj, String>ask(insertAddress)
                .apply(client)
                .retry(Failures.anyOf(MongoFailures.MONGO_CONNECT_TIMEOUT_CODE),
                       3);


        this.findByEmail = e -> vertxRef.spawn(findByEmailAddress,
                                               findOneByEmail())
                                        .apply(e)
                                        .map(Optional::ofNullable);


        λc<JsObj, Long> count =
                vertxRef.spawn(countAllByAddress,
                               new Count(collectionSupplier));

        this.countAll = message ->
                count.apply(JsObj.empty())
                     .retry(Failures.anyOf(MongoFailures.MONGO_CONNECT_TIMEOUT_CODE,
                                           MongoFailures.MONGO_READ_TIMEOUT_CODE),
                            queriesFailureAttempts,
                            queryRetryPolicy.apply(queriesFailureAttempts));

    }

    private λ<String, JsObj> findOneByEmail() {
        λc<FindMessage, JsObj> findOne = new FindOne(collectionSupplier);

        return email -> {
            Val<JsObj> val = findOne.apply(new FindMessageBuilder().filter(JsObj.empty()
                                                                                .set(String.format("%s.%s",
                                                                                                   EMAIL_FIELD,
                                                                                                   ADDRESS_FIELD),
                                                                                     JsStr.of(email))
            )
                                                                   .maxTime(maxQueryTime,
                                                                            MILLISECONDS)
                                                                   .create());
            return val
                    .retry(Failures.anyOf(MongoFailures.MONGO_CONNECT_TIMEOUT_CODE,
                                          MongoFailures.MONGO_READ_TIMEOUT_CODE),
                           queriesFailureAttempts,
                           queryRetryPolicy.apply(queriesFailureAttempts));
        };
    }


}
