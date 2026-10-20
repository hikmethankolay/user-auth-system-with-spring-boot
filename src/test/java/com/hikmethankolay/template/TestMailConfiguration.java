package com.hikmethankolay.template;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/** Replaces the SMTP sender with one that keeps every message in memory. */
@TestConfiguration(proxyBeanMethods = false)
public class TestMailConfiguration {

    @Bean
    @Primary
    CapturingMailSender capturingMailSender() {
        return new CapturingMailSender();
    }

    /** Stores sent messages so tests can read the links they contain. */
    public static class CapturingMailSender implements MailSender {

        private static final Pattern TOKEN = Pattern.compile("[?&]token=([A-Za-z0-9_-]+)");

        private final List<SimpleMailMessage> messages = new CopyOnWriteArrayList<>();

        private final AtomicInteger failuresLeft = new AtomicInteger();

        private final AtomicReference<CountDownLatch[]> hold = new AtomicReference<>();

        @Override
        public void send(SimpleMailMessage message) {
            CountDownLatch[] latches = hold.getAndSet(null);
            if (latches != null) {
                latches[0].countDown();
                try {
                    latches[1].await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            if (failuresLeft.getAndUpdate(left -> Math.max(0, left - 1)) > 0) {
                throw new MailSendException("Simulated SMTP outage");
            }
            messages.add(message);
        }

        /** Makes the next send count down {@code entered} and then wait for {@code release}, like a slow SMTP server. */
        public void holdNextSend(CountDownLatch entered, CountDownLatch release) {
            hold.set(new CountDownLatch[] {entered, release});
        }

        /** Makes the next {@code count} sends fail, like an SMTP outage. */
        public void failNext(int count) {
            failuresLeft.set(count);
        }

        @Override
        public void send(SimpleMailMessage... messages) {
            for (SimpleMailMessage message : messages) {
                send(message);
            }
        }

        /** Returns the newest message sent to an address with a subject containing the given text. */
        public Optional<SimpleMailMessage> latest(String to, String subjectPart) {
            return messages.reversed().stream()
                    .filter(message ->
                            message.getTo() != null && List.of(message.getTo()).contains(to))
                    .filter(message ->
                            message.getSubject() != null && message.getSubject().contains(subjectPart))
                    .findFirst();
        }

        /** Extracts the token from the link in a message body. */
        public static String token(SimpleMailMessage message) {
            Matcher matcher = TOKEN.matcher(message.getText());
            if (!matcher.find()) {
                throw new AssertionError("No token link in: " + message.getText());
            }
            return matcher.group(1);
        }
    }
}
