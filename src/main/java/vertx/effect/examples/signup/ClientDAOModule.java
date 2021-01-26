package vertx.effect.examples.signup;

import com.mongodb.client.MongoCollection;
import io.vertx.core.DeploymentOptions;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import lombok.Builder;
import vertx.effect.*;
import vertx.effect.exp.Cons;
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

    private static final String DEFAULT_INSERT_ADDRESS = "insert_client";
    private static final String DEFAULT_FIND_BY_EMAIL_ADDRESS = "find_one_client_by_email";
    private static final String DEFAULT_COUNT_ALL_ADDRESS = "count_all_clients";
    private static final int DEFAULT_INSERT_INSTANCES = 1;
    private static final int DEFAULT_MAX_QUERY_TIME = 4000;
    private static final int DEFAULT_INSERT_FAILURE_ATTEMPTS = 3;
    private static final int DEFAULT_QUERIES_FAILURE_ATTEMPTS = 3;
    private static final Function<Integer, RetryPolicy<Throwable>> DEFAULT_INSERT_RETRY_POLICY =
            attempts -> (error, remaining) -> Cons.NULL;
    private static final Function<Integer, RetryPolicy<Throwable>> DEFAULT_QUERY_RETRY_POLICY =
            attempts -> (error, remaining) -> Cons.NULL;
    private final String insertAddress;
    private final String findByEmailAddress;
    private final String countAllAddress;
    private final int insertInstances;
    private final int maxQueryTime;
    private final int insertFailureAttempts;
    private final int queriesFailureAttempts;
    private final Function<Integer, RetryPolicy<Throwable>> insertRetryPolicy;
    private final Function<Integer, RetryPolicy<Throwable>> queryRetryPolicy;

    public λ<JsObj, String> insert;
    public λ<String, Optional<JsObj>> findByEmail;
    public λ<Void, Long> countAll;


    @Builder
    public ClientDAOModule(final String insertAddress,
                           final String findByEmailAddress,
                           final String countAllAddress,
                           final int insertInstances,
                           final int maxQueryTime,
                           final int insertFailureAttempts,
                           final int queriesFailureAttempts,
                           final Function<Integer, RetryPolicy<Throwable>> insertRetryPolicy,
                           final Function<Integer, RetryPolicy<Throwable>> queryRetryPolicy,
                           final Supplier<MongoCollection<JsObj>> collectionSupplier) {
        super(collectionSupplier);
        this.insertAddress = insertAddress == null ? DEFAULT_INSERT_ADDRESS : insertAddress;
        this.findByEmailAddress = findByEmailAddress == null ? DEFAULT_FIND_BY_EMAIL_ADDRESS : findByEmailAddress;
        this.countAllAddress = countAllAddress == null ? DEFAULT_COUNT_ALL_ADDRESS : countAllAddress;
        this.insertInstances = insertInstances > 0 ? insertInstances : DEFAULT_INSERT_INSTANCES;
        this.maxQueryTime = maxQueryTime > 0 ? maxQueryTime : DEFAULT_MAX_QUERY_TIME;
        this.insertFailureAttempts = insertFailureAttempts > 0 ? insertFailureAttempts : DEFAULT_INSERT_FAILURE_ATTEMPTS;
        this.queriesFailureAttempts = queriesFailureAttempts > 0 ? queriesFailureAttempts : DEFAULT_QUERIES_FAILURE_ATTEMPTS;
        this.insertRetryPolicy = insertRetryPolicy == null ? DEFAULT_INSERT_RETRY_POLICY : insertRetryPolicy;
        this.queryRetryPolicy = queryRetryPolicy == null ? DEFAULT_QUERY_RETRY_POLICY : queryRetryPolicy;
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
               new DeploymentOptions().setInstances(insertInstances));

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
                vertxRef.spawn(countAllAddress,
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
