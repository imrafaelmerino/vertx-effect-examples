package vertx.effect.examples.signup.email;

import com.sun.mail.util.MailConnectException;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import jsonvalues.JsObj;
import vertx.effect.Val;
import vertx.effect.exp.Cons;
import vertx.effect.λ;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Properties;

import static javax.mail.Message.RecipientType.TO;

class SendEmailLambda implements λ<JsObj, Void> {

    final String host;
    final Properties props;
    final String user;
    final byte[] password;
    final String from;
    final String fromName;
    final String configSet;
    private static final String CONF_SET_HEADER = "X-SES-CONFIGURATION-SET";

    public SendEmailLambda(final String host,
                           final Properties props,
                           final String user,
                           final byte[] password,
                           final String from,
                           final String fromName,
                           final String configSet) {
        this.host = host;
        this.props = props;
        this.user = user;
        this.password = password;
        this.from = from;
        this.fromName = fromName;
        this.configSet = configSet;
    }


    @Override
    public Val<Void> apply(final JsObj email) {
        // Send the message.
        Transport transport = null;
        try {
            Session session = Session.getDefaultInstance(props);

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from,
                                            fromName)
            );
            msg.setRecipient(TO,
                             new InternetAddress(Email.toLens.get.apply(email))
            );
            msg.setSubject(Email.subjectLens.get.apply(email));
            msg.setContent(Email.bodyLens.get.apply(email),
                           Email.contentTypeLens.get.apply(email)
            );

            if (configSet != null && !configSet.isBlank())
                msg.setHeader(CONF_SET_HEADER,
                              configSet
                );

            transport = session.getTransport();
            transport.connect(host,
                              user,
                              new String(password)
            );
            transport.sendMessage(msg,
                                  msg.getAllRecipients()
            );

            return Cons.NULL;

        } catch (MailConnectException e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof UnknownHostException) {
                    return Cons.failure(new ReplyException(ReplyFailure.RECIPIENT_FAILURE,
                                                           EmailFailures.UNKNOWN_HOST,
                                                           e.getMessage()));
                }
                if (e.getCause() instanceof SocketTimeoutException) {//connection timeout
                    return Cons.failure(new ReplyException(ReplyFailure.RECIPIENT_FAILURE,
                                                           EmailFailures.CONNECTION_TIMEOUT,
                                                           e.getMessage()));
                }

            }

            return Cons.failure(e);
        } catch (MessagingException e) {//
            if (e.getCause() instanceof SocketTimeoutException) {
                return Cons.failure(new ReplyException(ReplyFailure.RECIPIENT_FAILURE,
                                                       EmailFailures.REQ_TIMEOUT,
                                                       e.getMessage()
                ));
            }
            return Cons.failure(e);
        } catch (Exception e) {
            return Cons.failure(e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    return Cons.failure(e);
                }
            }
        }
    }
}
