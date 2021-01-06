package vertx.effect.examples.signup;

import jsonvalues.*;
import jsonvalues.gen.JsGens;
import jsonvalues.gen.JsObjGen;
import jsonvalues.spec.JsObjSpec;

import java.time.Instant;
import java.util.List;

import static jsonvalues.spec.JsSpecs.*;

public class Client {

    private static final String AGE_FIELD = "age";
    private static final String NAME_FIELD = "name";
    private static final String REGISTRATION_DATE_FIELD = "registration_date";
    private static final String IMAGE_FIELD = "image";
    private static final String EMAIL_FIELD = "email";
    private static final String GENDER_FIELD = "gender";
    private static final String ADDRESS_FIELD = "address";
    private static final String VALIDATED_FIELD = "validated";

    public static final JsPath EMAIL_ADDRESS_PATH = JsPath.fromKey(EMAIL_FIELD)
                                                          .key(ADDRESS_FIELD);
    public static final JsPath IS_EMAIL_VALIDATED_PATH = JsPath.fromKey(EMAIL_FIELD)
                                                               .key(VALIDATED_FIELD);


    public static final Lens<JsObj, String> EMAIL_LENS =
            JsObj.lens.str(EMAIL_ADDRESS_PATH);

    public static final Lens<JsObj, Boolean> IS_EMAIL_VALIDATED_LENS =
            JsObj.lens.bool(IS_EMAIL_VALIDATED_PATH);

    public static final Lens<JsObj, String> ADDRESS_LENS =
            JsObj.lens.str(ADDRESS_FIELD);

    public static final List<JsStr> GENDER_ENUM =
            List.of(JsStr.of("M"),
                    JsStr.of("F"));


    public static final JsObjSpec EMAIL_SPEC =
            JsObjSpec.lenient(ADDRESS_FIELD,
                              str,
                              VALIDATED_FIELD,
                              bool);


    public static final JsObjSpec CLIENT_SPEC =
            JsObjSpec.strict(AGE_FIELD,
                             integer.optional(),
                             NAME_FIELD,
                             str,
                             REGISTRATION_DATE_FIELD,
                             instant,
                             IMAGE_FIELD,
                             binary.optional(),
                             EMAIL_FIELD,
                             EMAIL_SPEC,
                             GENDER_FIELD,
                             oneOf(GENDER_ENUM).optional(),
                             ADDRESS_FIELD,
                             str);

    public static final JsObjGen CLIENT_GEN =
            JsObjGen.of(AGE_FIELD,
                        JsGens.integer.optional(),
                        NAME_FIELD,
                        JsGens.alphabetic(5),
                        REGISTRATION_DATE_FIELD,
                        JsGens.cons(JsInstant.of(Instant.now())),
                        EMAIL_FIELD,
                        JsObjGen.of(ADDRESS_FIELD,
                                    JsGens.cons(JsStr.of("rafamg13@gmail.com")),
                                    VALIDATED_FIELD,
                                    JsGens.bool
                        ),
                        GENDER_FIELD,
                        JsGens.oneOf(GENDER_ENUM),
                        ADDRESS_FIELD,
                        JsGens.cons(JsStr.of("Plaza de Colón, 1, 28001 Madrid"))
            );
}
