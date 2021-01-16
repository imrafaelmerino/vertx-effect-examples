package vertx.effect.examples;

import jsonvalues.JsObj;
import jsonvalues.Json;
import jsonvalues.MalformedJson;
import vertx.effect.exp.Cons;
import vertx.effect.λ;

import java.time.Instant;

public class Functions {

    public static λ<String, JsObj> str2JsObj = str -> {
        try {
            return Cons.success(JsObj.parse(str));
        } catch (MalformedJson malformedJson) {
            return Cons.failure(malformedJson);
        }
    };

    public static final λ<JsObj, String> jsObj2Str = json -> Cons.success(json.toString());

    public static final λ<Void, Instant> getTimestamp = $ -> Cons.success(Instant.now());


}
