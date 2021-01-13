package vertx.effect.examples;

import jsonvalues.JsObj;
import jsonvalues.Json;
import jsonvalues.MalformedJson;
import vertx.effect.VertxModule;
import vertx.effect.λ;

import java.time.Instant;

public class FunctionsModule extends VertxModule {
    private static final String GET_TIMESTAMP_ADDRESS = "get-timestamp";
    private static final String STR_TO_JSOBJ_ADDRESS = "string-to-json";
    private static final String JSON_TO_STR_ADDRESS = "json-to-string";

    public λ<Void, Instant> getTimestamp;

    public λ<String, JsObj> str2JsObj;

    public λ<JsObj, String> jsObj2Str;


    @Override
    protected void initialize() {

        getTimestamp = ask(GET_TIMESTAMP_ADDRESS);

        str2JsObj = ask(STR_TO_JSOBJ_ADDRESS);

        jsObj2Str = ask(JSON_TO_STR_ADDRESS);

    }

    @Override
    protected void deploy() {

        deploy(GET_TIMESTAMP_ADDRESS, Functions.getTimestamp);

        deploy(STR_TO_JSOBJ_ADDRESS, Functions.str2JsObj);

        deploy(JSON_TO_STR_ADDRESS, Functions.jsObj2Str);
    }
}
