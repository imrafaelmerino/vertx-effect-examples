package vertx.effect.examples;

import jsonvalues.JsObj;
import jsonvalues.MalformedJson;
import vertx.effect.Val;
import vertx.effect.λ;

import java.time.Instant;

public class Functions {

    public static λ<String, JsObj> str2JsObj = str -> {
        try {
            return Val.succeed(JsObj.parse(str));
        } catch (MalformedJson malformedJson) {
            return Val.fail(malformedJson);
        }
    };

    public static final λ<JsObj, String> jsObj2Str = json -> Val.succeed(json.toString());

    public static final λ<Void, Instant> timeStamp = $ -> Val.succeed(Instant.now());




}
