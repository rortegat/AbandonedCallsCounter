import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {

	private String USERNAME = "ccxalerts@domain.com";
	private String PASSWORD = "Contraseña";

	private String HOST = "d23hubm6.in.ibm.com";
	private int PORT = 25;

	private Properties props = System.getProperties();
	
	
	public void sendEmail() {
		
		props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", HOST);
		props.put("mail.smtp.port", PORT);
		props.put("mail.smtp.ssl.trust", HOST);

		Session session = Session.getInstance(props,
		          new javax.mail.Authenticator() {
					@Override
		            protected PasswordAuthentication getPasswordAuthentication() {
		                return new PasswordAuthentication(USERNAME, PASSWORD);
		            }
		          });
		 session.setDebug(true);

		

		try {
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("ccxalerts@domain.com"));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("ricardo.ortega@citel.com.mx"));
			msg.setSubject("NOTIFICACIÓN", "utf-8");
			msg.setContent("Se ha superado el límite de llamadas perdidas", "text/html; charset=utf-8");

			System.out.println("Sending...");
			Transport.send(msg);
			System.out.println("Email sent!");
		} catch (Exception ex) {
			System.out.println("The email was not sent.");
			System.out.println("Error message: " + ex.getMessage());
		} 
	}
}
