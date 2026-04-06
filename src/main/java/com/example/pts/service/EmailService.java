package com.example.pts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import com.example.pts.model.Job;
import java.util.List;

import org.springframework.scheduling.annotation.Async;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final String fromEmail = "yaswanthadithyareddy11@gmail.com";

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending HTML email: " + e.getMessage());
        }
    }

    @Async
    public void sendLoginNotificationEmail(String to, String name, List<Job> jobs, boolean isNewUser) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>");
        
        // Header
        html.append("<div style='background-color: #0066cc; color: white; padding: 20px; border-radius: 10px 10px 0 0; text-align: center;'>");
        html.append("<h1 style='margin: 0;'>Placement Training System Portal</h1>");
        html.append("</div>");

        // Welcome Message
        html.append("<div style='padding: 20px; color: #333;'>");
        html.append("<h2>Hello, ").append(name).append("!</h2>");
        if (isNewUser) {
            html.append("<p style='font-size: 16px; line-height: 1.6;'>Welcome to the **Placement Training System**! Your account has been successfully created.</p>");
            html.append("<p style='font-size: 16px; line-height: 1.6;'>You can now explore job opportunities, track your applications, and prepare for your career.</p>");
        } else {
            html.append("<p style='font-size: 16px; line-height: 1.6;'>We noticed a new login to your Placement Training System account. If this was you, you can safely ignore this email.</p>");
        }

        // Job Vacancies (Placement Related Content)
        if (jobs != null && !jobs.isEmpty()) {
            html.append("<hr style='border: 0; border-top: 1px solid #eee; margin: 25px 0;'>");
            html.append("<h3 style='color: #0066cc;'>Latest Placement Opportunities</h3>");
            html.append("<ul style='list-style: none; padding: 0;'>");
            for (Job job : jobs) {
                html.append("<li style='margin-bottom: 20px; padding: 15px; background: #f9f9f9; border-left: 4px solid #0066cc; border-radius: 4px;'>");
                html.append("<strong style='font-size: 18px;'>").append(job.getTitle()).append("</strong><br>");
                html.append("<span style='color: #666;'>").append(job.getCompany()).append("</span> &bull; ");
                html.append("<span style='color: #0066cc; font-weight: bold;'>").append(job.getSalary()).append("</span><br>");
                html.append("<small style='color: #999;'>Deadline: ").append(job.getDeadline()).append("</small>");
                html.append("</li>");
            }
            html.append("</ul>");
        }

        // Footer
        html.append("<div style='margin-top: 30px; text-align: center; color: #888; font-size: 12px;'>");
        html.append("<p>&copy; 2024 Placement Training System &bull; University Official Portal</p>");
        if (isNewUser) {
            html.append("<p>Please complete your profile details on the dashboard to start applying.</p>");
        }
        html.append("</div>");
        html.append("</div></div>");

        String subject = isNewUser ? "Welcome to Placement Training System Portal!" : "New Login Notification - Placement Training System Portal";
        sendHtmlEmail(to, subject, html.toString());
    }

    @Async
    public void sendPasswordResetEmail(String to, String otp) {
        String body = "You requested a password reset for your Placement Training System Portal account.\n\n" +
                      "Your One-Time Password (OTP) is: " + otp + "\n\n" +
                      "Please enter this OTP to reset your password. This OTP will expire in 15 minutes.";
        sendEmail(to, "Your Password Reset OTP - Placement Training System Portal", body);
    }
    
    @Async
    public void sendNotificationEmail(String to, String message) {
        String body = "New Notification from Placement Training System Portal:\n\n" + message;
        sendEmail(to, "New Notification - Placement Training System Portal", body);
    }
}
