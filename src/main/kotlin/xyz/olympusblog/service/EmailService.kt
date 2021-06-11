package xyz.olympusblog.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import xyz.olympusblog.models.Email
import org.springframework.mail.MailException

import org.springframework.mail.javamail.MimeMessageHelper

import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.scheduling.annotation.Async
import xyz.olympusblog.exception.SpringRestException
import javax.mail.internet.MimeMessage


@Service
class EmailService(private val mailSender: JavaMailSender, private val mailContentBuilder: MailContentBuilder) {

    @Async
    fun sendEmail(email: Email) {
        val messagePreparator = MimeMessagePreparator { mimeMessage: MimeMessage? ->
            val messageHelper = MimeMessageHelper(mimeMessage!!)
            messageHelper.setFrom("staff@olympusblog.com")
            messageHelper.setTo(email.recipient)
            messageHelper.setSubject(email.subject)
            messageHelper.setText(mailContentBuilder.build(email.body))
        }
        try {
            mailSender.send(messagePreparator)
        } catch (e: MailException) {
            throw SpringRestException("Exception occurred when sending mail to ${email.recipient}")
        }
    }
}