package com.kozikwlodarski.backendgymflex.service;

import com.kozikwlodarski.backendgymflex.event.ClassCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ClassCancellationNotificationService {
    private final JavaMailSender mailSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyParticipants(ClassCancelledEvent event) {
        for (String email : event.recipientEmails()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Odwołanie zajęć: " + event.classTitle());
            message.setText("Zajęcia „" + event.classTitle() + "” zostały odwołane. Rezerwacja została anulowana.");
            mailSender.send(message);
        }
    }
}
