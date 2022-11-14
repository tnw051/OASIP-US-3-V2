package int221.oasip.backendus3.services;

import int221.oasip.backendus3.entities.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

@Service
public class MailService {
    @Value("${mail.disable}")
    private boolean mailDisable;

    public void sendmail(Event event) throws MessagingException {
        if (mailDisable) {
            return;
        }
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("oasip.us3.noreply@gmail.com", "hyyvvoygfnytkmgt");
            }
        });
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E MMM dd, yyyy HH:mm").withZone(ZoneId.of("Asia/Bangkok"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm ").withZone(ZoneId.of("Asia/Bangkok"));
        Instant endTime = event.getEventStartTime().plusSeconds(event.getEventDuration() * 60);

        javax.mail.Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("oasip.us3.noreply@gmail.com", false));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(event.getBookingEmail()));
        msg.setSubject("Your booking is complete.");
        String eventCategory = event.getEventCategory().getEventCategoryName();
        String eventNotes = event.getEventNotes();
        msg.setContent("Subject: [OASIP] " + eventCategory + " @ " + dateTimeFormatter.format(event.getEventStartTime()) + " - " + timeFormatter.format(endTime) + " (ICT)" +
                        "<br>Reply-to: noreply@intproj21.sit.kmutt.ac.th" +
                        "<br>Booking Name: " + event.getBookingName() +
                        "<br>Event Category: " + eventCategory +
                        "<br>When: " + dateTimeFormatter.format(event.getEventStartTime()) + " - " + timeFormatter.format(endTime) + " (ICT)" +
                        "<br>Event Notes: " + (eventNotes == null ? "" : eventNotes)

                , "text/html; charset=utf-8");
        msg.setSentDate(new Date());

        Transport.send(msg);
    }
}
