package vertx.effect.examples.flatmap;


import jsonvalues.JsArray;
import vertx.effect.Val;
import vertx.effect.exp.Cons;
import vertx.effect.λ;

import static java.util.Objects.requireNonNull;


public class GetAllEmailsFromClients implements λ<JsArray, JsArray> {

    public final λ<String, JsArray> getClientEmails;

    public GetAllEmailsFromClients(final λ<String, JsArray> getClientEmails) {
        this.getClientEmails = requireNonNull(getClientEmails);
    }

    @Override
    public Val<JsArray> apply(final JsArray clientsIds) {
        return clientsIds.streamValues()
                         .map(val -> val.toJsStr().value)
                         .reduce(Cons.success(JsArray.empty()),
                                 (acc, clientId) ->
                                         acc.flatMap(emails -> getClientEmails.apply(clientId)
                                                                              .map(emails::appendAll)
                                         ),
                                 (arr1, arr2) ->
                                         arr1.flatMap(a -> arr2.map(a::appendAll))
                         );
    }
}
