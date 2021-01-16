package vertx.effect.examples.signup.mongodb;

import com.mongodb.client.MongoCollection;
import jsonvalues.JsObj;
import vertx.effect.RetryPolicy;
import vertx.effect.exp.Cons;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class ClientDAOModuleBuilder {
    private String insertAddress = "insert_client";
    private String findByEmailAddress = "find_one_client_by_email";
    private String countAllAddress = "count_all_clients";

    private final Supplier<MongoCollection<JsObj>> collection;

    private int insertInstances = 1;
    private int maxQueryTime = 4000;
    private int insertFailureAttempts = 3;
    private int queriesFailureAttempts = 3;

    private Function<Integer, RetryPolicy<Throwable>> insertRetryPolicy =
            attempts -> (error, remaining) -> Cons.NULL;
    private Function<Integer, RetryPolicy<Throwable>> queryRetryPolicy =
            attempts -> (error, remaining) -> Cons.NULL;

    public ClientDAOModuleBuilder(final Supplier<MongoCollection<JsObj>> collection) {
        this.collection = requireNonNull(collection);
    }

    public ClientDAOModuleBuilder setInsertInstances(final int insertInstances) {
        this.insertInstances = insertInstances;
        return this;
    }

    public ClientDAOModuleBuilder setMaxQueryTime(final int maxQueryTime) {
        this.maxQueryTime = maxQueryTime;
        return this;
    }

    public ClientDAOModuleBuilder setInsertFailureAttempts(final int insertFailureAttempts) {
        this.insertFailureAttempts = insertFailureAttempts;
        return this;
    }

    public ClientDAOModuleBuilder setInsertAddress(final String insertAddress) {
        this.insertAddress = insertAddress;
        return this;
    }

    public ClientDAOModuleBuilder setFindOneByEmailAddress(final String findByEmailAddress) {
        this.findByEmailAddress = findByEmailAddress;
        return this;
    }

    public ClientDAOModuleBuilder setCountAllAddress(final String countAllAddress) {
        this.countAllAddress = countAllAddress;
        return this;
    }

    public ClientDAOModuleBuilder setQueriesFailureAttempts(final int queriesFailureAttempts) {
        this.queriesFailureAttempts = queriesFailureAttempts;
        return this;
    }

    public ClientDAOModuleBuilder setInsertRetryPolicy(final Function<Integer, RetryPolicy<Throwable>> insertRetryPolicy) {
        this.insertRetryPolicy = requireNonNull(insertRetryPolicy);
        return this;
    }

    public ClientDAOModuleBuilder setQueryRetryPolicy(final Function<Integer, RetryPolicy<Throwable>> queryRetryPolicy) {
        this.queryRetryPolicy = requireNonNull(queryRetryPolicy);
        return this;
    }

    public ClientDAOModule createModule() {
        return new ClientDAOModule(collection,
                                   insertInstances,
                                   maxQueryTime,
                                   insertFailureAttempts,
                                   queriesFailureAttempts,
                                   insertRetryPolicy,
                                   queryRetryPolicy,
                                   insertAddress,
                                   findByEmailAddress,
                                   countAllAddress);
    }

    public String getInsertAddress() {
        return insertAddress;
    }

}