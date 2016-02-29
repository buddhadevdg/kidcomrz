package test.com.shareart.service;

import java.util.Properties;
//import javax.mail.*;
//import javax.mail.internet.*;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMailSSL {
	public static void main(String[] args) {

		String to = "buddhadev.dasgupta@gmail.com";// change accordingly

		// Get the session object
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.mail.yahoo.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		/*props.put("mail.smtp.user", "buddhadev.project@gmail.com");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.debug", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.socketFactory.port", "25");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");*/
		long count = System.currentTimeMillis();
		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("buddhadev.project@yahoo.com", "project@123");
			}
		});
		System.out.println("time to create session :: "+(System.currentTimeMillis()-count));

		// compose message
		try {

			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress("buddhadev.project@yahoo.com"));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject("Hello");
			message.setText("Testing.......");
			count = System.currentTimeMillis();
			// send message
			Transport.send(message);
			
			/*Transport transport = session.getTransport("smtps");
			transport.connect("smtp.gmail.com", 587, "buddhadev.project@gmail.com", "project@123");
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();  */
			System.out.println("time to send :: "+(System.currentTimeMillis()-count));
			System.out.println("message sent successfully");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}

	}
}