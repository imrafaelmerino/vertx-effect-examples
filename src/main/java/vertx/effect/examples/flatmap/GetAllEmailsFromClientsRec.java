package vertx.effect.examples.flatmap;


import jsonvalues.JsArray;
import jsonvalues.JsObj;
import jsonvalues.Lens;
import vertx.effect.Val;
import vertx.effect.exp.Cons;
import vertx.effect.exp.JsObjExp;
import vertx.effect.λ;
import  io.vertx.core.Vertx;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;


public class GetAllEmailsFromClientsRec implements λ<JsArray, JsArray> {

    public final λ<String, JsArray> getClientEmails;
    private static final String IDS_FIELD = "ids";
    private static final String ACC_FIELD = "acc";


    public GetAllEmailsFromClientsRec(final λ<String, JsArray> getClientEmails) {
        this.getClientEmails = requireNonNull(getClientEmails);
    }

    @Override
    public Val<JsArray> apply(final JsArray clients) {
        return getCustomerEmailsRec().apply(JsObj.of(IDS_FIELD,
                                                     clients,
                                                     ACC_FIELD,
                                                     JsArray.empty()))
                                     .map(accLens.get);

    }

    private λ<JsObj, JsObj> getCustomerEmailsRec() {
        return input -> {
            JsArray ids = idsLens.get.apply(input);
            if (ids.isEmpty()) return Cons.success(input);
            String id = ids.head()
                           .toJsStr().value;
            return JsObjExp.sequential(IDS_FIELD,
                                       Cons.success(ids.tail()),
                                       ACC_FIELD,
                                       this.getClientEmails.apply(id)
                                                           .map(emails -> accEmails(emails).apply(input))
            );

        };
    }

    private static final Lens<JsObj, JsArray> idsLens = JsObj.lens.array(IDS_FIELD);

    private static final Lens<JsObj, JsArray> accLens = JsObj.lens.array(ACC_FIELD);

    private static Function<JsObj, JsObj> accEmails(JsArray emails) {
        return message -> accLens.modify.apply(acc -> acc.appendAll(emails))
                                        .apply(message);
    }


}
