package vertx.effect.examples.signup;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import jsonvalues.JsObj;
import jsonvalues.JsStr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import vertx.effect.Failures;
import vertx.effect.RegisterJsValuesCodecs;
import vertx.effect.VertxRef;
import vertx.effect.examples.signup.email.SendEmailModule;
import vertx.effect.examples.signup.email.SendEmailModuleBuilder;
import vertx.effect.exp.Pair;

import java.util.Properties;

@ExtendWith(VertxExtension.class)
public class SendEmailModuleTest {
    static SendEmailModule emailModule;

    @BeforeAll
    public static void prepare(VertxTestContext context,
                               Vertx vertx) {

        VertxRef vertxRef = new VertxRef(vertx);
        vertxRef.registerConsumer(VertxRef.EVENTS_ADDRESS,
                                  System.out::println
        );

        Properties props = new Properties();
        props.put("mail.transport.protocol",
                  "smtp"
        );
        props.put("mail.smtp.port",
                  587
        );
        props.put("mail.smtp.starttls.enable",
                  "true"
        );
        props.put("mail.smtp.auth",
                  "true"
        );
        props.put("mail.smtp.connectiontimeout",
                  1000
        );
        props.put("mail.smtp.timeout",
                  5000
        );

        emailModule = new SendEmailModuleBuilder()
                .setFrom("imrafael.merino@gmail.com")
                .setFromName("Rafael Merino García")
                .setHost("email-smtp.us-east-2.amazonaws.com")
                .setUser(System.getProperty("EMAIL_API_USER",""))
                .setPassword(System.getProperty("EMAIL_API_PASSWORD","").getBytes())
                .setInstances(2)
                .setSendEmailAddress("email-send")
                .setValidateEmailAddress("email-validate")
                .setProps(props)
                .createModule();


        Pair.sequential(vertxRef.deployVerticle(new RegisterJsValuesCodecs()),
                        vertxRef.deployVerticle(emailModule)
        )
            .onComplete(it -> {
                if (it.succeeded()) context.completeNow();
                else context.failNow(it.cause());
            })
            .get();

    }


    @Test
    public void test_Validate_Message_Invalid_Message(VertxTestContext context) {

        emailModule.validateEmail.apply(JsObj.empty())
                             .onComplete(result -> context.verify(() -> {
                                 Assertions.assertTrue(Failures.anyOf(Failures.BAD_MESSAGE_CODE)
                                                               .test(result.cause()));
                                 context.completeNow();
                             }))
                             .get();

    }

    @Test
    public void test_SendEmail_Valid_Message(VertxTestContext context) {

        JsObj email = JsObj.of("to",
                               JsStr.of("rafamg13@gmail.com"),
                               "subject",
                               JsStr.of("vertx-effect rocks!"),
                               "body",
                               JsStr.of("This is just a test"),
                               "content_type",
                               JsStr.of("text/plain")
        );

        emailModule.validateEmail.andThen(emailModule.sendEmail)
                                 .apply(email)
                                 .onComplete(result -> context.verify(() -> {
                                     Assertions.assertTrue(result.succeeded());
                                     context.completeNow();
                                 }))
                                 .get();

    }
}
