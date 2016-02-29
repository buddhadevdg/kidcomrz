package test.com.shareart.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailExampleYahoo {
    private static final String SMTP_HOST_NAME = "smtp.mail.yahoo.com";
    private static final int SMTP_HOST_PORT = 587;//465,587,25
    private static final String SMTP_AUTH_USER = "buddhadev.project@yahoo.com";
    private static final String SMTP_AUTH_PWD  = "project@123";

    public static void main(String[] args) throws Exception{
       new MailExampleYahoo().test();
    }

    public void test() throws Exception{
        Properties props = new Properties();

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.auth", "true");
        // props.put("mail.smtps.quitwait", "false");

        //Session mailSession = Session.getDefaultInstance(props);
        Session mailSession = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("buddhadev.project@yahoo.com", "project@123");
			}
		});
        mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject("Testing SMTP-SSL");
        message.setContent("This is a test", "text/plain");

        message.addRecipient(Message.RecipientType.TO,
             new InternetAddress("buddhadev.project@gmail.com"));

        transport.connect
          (SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);

        transport.sendMessage(message,
            message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }
}
