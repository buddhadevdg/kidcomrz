package com.shareart.service.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.shareart.service.domain.Article;
import com.shareart.service.domain.User;

import io.vertx.core.Vertx;

public class EmailHelper {
	private static Properties props = new Properties();
	private static Vertx vertx;
	private static Logger log = Logger.getLogger(EmailHelper.class);
	
	private EmailHelper(){
		
	}

	public static void init(Vertx vtx) {
		vertx = vtx;
		props.put("mail.smtp.host", "smtp.mail.yahoo.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.debug", "true");
	}

	public static void sendRegristrationMail(String to, String activateUrl) {
		log.info("inside sendRegristrationMail------------");
		vertx.executeBlocking(future -> {
			Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("buddhadev.project@yahoo.com", "project@123");
				}
			});

			// compose message
			try {
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress("buddhadev.project@yahoo.com"));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
				//message.setHeader("content-Type", "text/html;charset=\"ISO-8859-1\"");
				
				StringBuilder body = new StringBuilder("<HTML><BODY>");
				body.append("<i>Greetings!</i><br><br>");
				body.append("Just one more step.<br><br>");
				body.append("Click <a href='" + activateUrl+ "'> this link</a> to activate your <b>kidscomrz</b> account.<br>");
				body.append("<br>");
				body.append("Cheers!");
				body.append("</BODY>");
				body.append("</HTML>");

				message.setSubject("kidscomrz acitivation");
				//message.setText(body.toString(),"ISO-8859-1");
				message.setContent(body.toString(), "text/html");

				// send message
				Transport.send(message);
				log.info("message sent successfully");
				future.complete();
			} catch (Exception e) {
				log.error(e);
				future.fail(e);
			}
		} , res -> {
			if (res.succeeded()) {
				log.info("Email trigger successfully! ");
			} else {
				log.info("Failed to trigger email ");
			}
		});	
	}
	
	public static void sendPurchaseArticleMail(User articleOwner, Article article, User purchaserUser) {
		log.info("inside sendPurchaseArticleMail------------");
		vertx.executeBlocking(future -> {
			Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("buddhadev.project@yahoo.com", "project@123");
				}
			});

			// compose message
			try {
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress("buddhadev.project@yahoo.com"));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(articleOwner.getEmail()));
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(purchaserUser.getEmail()));
				//message.setHeader("content-Type", "text/html;charset=\"ISO-8859-1\"");
				
				StringBuilder body = new StringBuilder("<HTML><BODY>");
				body.append("Hi ").append(articleOwner.getLastName()).append(" ").append(articleOwner.getFirstName()).append(",<br>");
				body.append("<p> You have a purchase request for the below article, please go through the purchase request and act accordingly.</p>");
				body.append("<p>");
				body.append("Article Information<br>");
				body.append("-------------------<br>");
				body.append("Name: ").append(article.getArticleName()).append("<br>");
				body.append("Description: ").append(article.getDescription()).append("<br>");
				body.append("Category: ").append(article.getCategory().getCategoryName()).append("<br>");
				body.append("Price: ").append(article.getPrice()).append("<br>");
				body.append("</p>");
				body.append("<p>");
				body.append("Purchase Requestor Information<br>");
				body.append("------------------------------<br>");
				body.append("Name: ").append(purchaserUser.getLastName()).append(" ").append(purchaserUser.getFirstName()).append("<br>");
				body.append("Email: ").append(purchaserUser.getEmail()).append(" (cced),<br>");
				body.append("Address: <br>");
				body.append("line1: ").append(purchaserUser.getAddress().getLine1()).append("<br>");
				body.append("line2: ").append(purchaserUser.getAddress().getLine2()).append("<br>");
				body.append("city: ").append(purchaserUser.getAddress().getCity()).append("<br>");
				body.append("state: ").append(purchaserUser.getAddress().getState()).append("<br>");
				body.append("zip: ").append(purchaserUser.getAddress().getZipCode()).append("<br>");
				body.append("Phone: ").append(purchaserUser.getPhone()).append("<br>");
				body.append("</p>");
				body.append("Regards,<br>");
				body.append("kidscomrz Team");
				body.append("</BODY>");
				body.append("</HTML>");

				message.setSubject("Purchase Request for '"+article.getArticleName()+"'");
				//message.setText(body.toString(),"ISO-8859-1");
				message.setContent(body.toString(), "text/html");

				// send message
				Transport.send(message);
				log.info("message sent successfully");
				future.complete();
			} catch (Exception e) {
				log.error(e);
				e.printStackTrace();
				future.fail(e);
			}
		} , res -> {
			if (res.succeeded()) {
				log.info("Email trigger successfully! ");
			} else {
				log.info("Failed to trigger email ");
			}
		});	
	}
	
	public static void main(String[] args) {
		EmailHelper.init(null);
		EmailHelper.sendRegristrationMail("buddhadev.dasgupta@gmail.com", "sdfsdf");
	}
}