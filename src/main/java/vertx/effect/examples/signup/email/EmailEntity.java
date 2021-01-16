package vertx.effect.examples.signup.email;

import jsonvalues.JsObj;
import jsonvalues.Lens;
import jsonvalues.spec.JsObjSpec;

import static jsonvalues.spec.JsSpecs.str;

public class EmailEntity {

    public static final String TO_FIELD = "to";
    public static final String SUBJECT_FIELD = "subject";
    public static final String BODY_FIELD = "body";
    public static final String CONTENT_TYPE_FIELD = "content_type";

    public static final Lens<JsObj,String> toLens =
            JsObj.lens.str(TO_FIELD);

    public static final Lens<JsObj,String> subjectLens =
            JsObj.lens.str(SUBJECT_FIELD);

    public static final Lens<JsObj,String> bodyLens =
            JsObj.lens.str(BODY_FIELD);

    public static final Lens<JsObj,String> contentTypeLens =
            JsObj.lens.str(CONTENT_TYPE_FIELD);


    public static final JsObjSpec spec =
            JsObjSpec.strict(TO_FIELD, str,
                             SUBJECT_FIELD, str,
                             BODY_FIELD, str,
                             CONTENT_TYPE_FIELD, str
            );
}
