package vertx.effect.examples.signup.mongodb;

import com.mongodb.client.MongoCollection;
import io.vertx.core.DeploymentOptions;
import jsonvalues.JsObj;
import vertx.effect.λ;
import vertx.effect.λc;
import vertx.mongodb.effect.Converters;
import vertx.mongodb.effect.FindMessage;
import vertx.mongodb.effect.FindMessageBuilder;
import vertx.mongodb.effect.MongoModule;
import vertx.mongodb.effect.functions.Count;
import vertx.mongodb.effect.functions.FindOne;
import vertx.mongodb.effect.functions.InsertOne;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static vertx.effect.examples.signup.Client.EMAIL_LENS;


public class ClientDAOModule extends MongoModule {


    private static final int DEFAULT_INSERT_INSTANCES = 1;
    private static final int DEFAULT_MAX_QUERY_TIME = 1000;
    private static final String COUNT_ALL_CLIENTS_ADDRESS = "count_clients";
    private static final String INSERT_CLIENT_ADDRESS = "insert_client";
    private static final String FIND_ONE_ADDRESS = "find_one";

    public λ<JsObj, String> insert;
    public λ<String, Optional<JsObj>> findByEmail;
    public λ<Void, Long> countAll;

    private final int insertClientInstances;
    private final int maxQueryTime;


    public ClientDAOModule(final Supplier<MongoCollection<JsObj>> collection,
                           final int insertInstances,
                           final int maxQueryTime) {
        super(collection);
        if (insertInstances < 1) throw new IllegalArgumentException("insertInstances < 1");
        if (maxQueryTime < 0) throw new IllegalArgumentException("maxQueryTime < 0 ");

        this.insertClientInstances = insertInstances;
        this.maxQueryTime = maxQueryTime;
    }

    public ClientDAOModule(final Supplier<MongoCollection<JsObj>> collection) {
        this(collection,
             DEFAULT_INSERT_INSTANCES,
             DEFAULT_MAX_QUERY_TIME);
    }

    @Override
    protected void deploy() {

        λc<JsObj, String> insertClient = new InsertOne<>(collectionSupplier,
                                                         Converters.insertOneResult2HexId);
        deploy(INSERT_CLIENT_ADDRESS,
               insertClient,
               new DeploymentOptions().setInstances(insertClientInstances));

    }


    @Override
    protected void initialize() {

        this.insert = this.ask(INSERT_CLIENT_ADDRESS);

        λc<FindMessage, JsObj> findOne = new FindOne(collectionSupplier);

        λ<FindMessage, Optional<JsObj>> findOneOpt =
                it -> findOne.apply(it)
                             .map(Optional::ofNullable);

        λ<String, Optional<JsObj>> findOneByEmail =
                email -> findOneOpt.apply(new FindMessageBuilder().filter(EMAIL_LENS.set.apply(email)
                                                                                        .apply(JsObj.empty())
                                          )
                                                                  .maxTime(maxQueryTime,
                                                                           MILLISECONDS)
                                                                  .create()
                );


        this.findByEmail = vertxRef.spawn(FIND_ONE_ADDRESS,
                                          findOneByEmail);


        λc<JsObj, Long> count = vertxRef.spawn(COUNT_ALL_CLIENTS_ADDRESS,
                                               new Count(collectionSupplier));

        this.countAll = message -> count.apply(JsObj.empty());

    }


}
