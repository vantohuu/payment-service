package com.springboot.architectural.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;


@Component
public class EmailUtil {

    @Autowired
    private JavaMailSender javaMailSender;


    public void sendEmail(String email, Integer amount, String name, String responseCode) throws MessagingException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Verify payment");
        if (!responseCode.equals("00"))
        {
            mimeMessageHelper.setText("""
        <div>
          <h3>Thanh toan that bai</h3>
          <h4>XEMPHIM-SOA</h4>
          <br/>
          <p>So tien : %s </p>
          <p> Phim : %s </p>
        </div>
        """.formatted(amount, name), true);
        }
        else
        {
            mimeMessageHelper.setText("""
        <div>
          <h3>Thanh toan thanh cong</h3>
          <h4>XEMPHIM-SOA</h4>
          <br/>
          <p>So tien: %s </p>
          <p> Phim : %s </p>
        </div>
        """.formatted(amount, name), true);
        }

        javaMailSender.send(mimeMessage);
    }
}