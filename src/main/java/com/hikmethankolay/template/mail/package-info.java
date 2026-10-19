/**
 * Outgoing email through a transactional outbox: messages are stored with the change that
 * triggers them and sent with retries, so an SMTP outage or a restart never loses an email.
 */
package com.hikmethankolay.template.mail;
