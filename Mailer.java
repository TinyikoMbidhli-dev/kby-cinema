import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class Mailer {

    public static String send(String toEmail, String subject, String body) {
        String user = System.getenv("CINEMA_MAIL_USER");
        String pass = System.getenv("CINEMA_MAIL_PASS");

        if (user == null || pass == null) {
            return "Email login not set up on this computer.";
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(user, "KBY Cinema"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject(subject);
            msg.setText(body);
            Transport.send(msg);
            return null; // null means success
        } catch (Exception e) {
            return "Could not send: " + e.getMessage();
        }
    }
}