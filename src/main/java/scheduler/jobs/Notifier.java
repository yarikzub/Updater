package scheduler.jobs;

import javax.mail.Message;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;
import scheduler.Options;


public class Notifier {
    
    public static void SendPlayersChanged(PlayerInfo[] players)
    {
        Options options = Options.GetCurrent();
        
        String emailText = "Top players were changed:\n";
        for (PlayerInfo player : players) {
            emailText += String.format("%d %s %.1f %.1f\n",
                player.position,
                player.name,
                player.ranking1,
                player.ranking2);
        }
        
        Email email = new Email();
        email.setFromAddress(null, options.FromAddress);
        email.addRecipient(null, options.ToAddress, Message.RecipientType.TO);
        email.setSubject("Ranking was changed");
        email.setText(emailText);

        new Mailer(
            options.SmtpHost,
            options.SmtpPort,
            options.SmtpUser,
            options.SmtpPassword,
            TransportStrategy.SMTP_TLS)
        .sendMail(email);
    }
    
}
