package msa.emaia.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${app.email.from}") // Inject the "from" address from properties
    private String fromAddress;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        this.sendEmail(message);
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // Set to true to send HTML

        javaMailSender.send(message);
    }
    // NEW: HTML with inline image via CID
    public void sendHtmlEmailWithInlineLogo(String to, String subject, String htmlContent,
                                            Resource logoResource, String contentId)
            throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        // multipart = true to allow inline resources
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        // "contentId" is what you reference in <img src="cid:contentId">
        helper.addInline(contentId, logoResource);

        javaMailSender.send(message);
    }
    public void sendEmail(SimpleMailMessage message) {
        javaMailSender.send(message);
    }


}