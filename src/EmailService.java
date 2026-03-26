import java.io.*;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class EmailService {
    
    public static boolean sendRealEmail(String toEmail, String code, String senderEmail, String appPassword) {
        try {
            // Connect securely to Gmail's SMTP server
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket("smtp.gmail.com", 465);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            readResponse(in);
            sendCmd(out, "HELO smtp.gmail.com"); readResponse(in);
            sendCmd(out, "AUTH LOGIN"); readResponse(in);
            sendCmd(out, Base64.getEncoder().encodeToString(senderEmail.getBytes())); readResponse(in);
            sendCmd(out, Base64.getEncoder().encodeToString(appPassword.getBytes())); readResponse(in);
            sendCmd(out, "MAIL FROM:<" + senderEmail + ">"); readResponse(in);
            sendCmd(out, "RCPT TO:<" + toEmail + ">"); readResponse(in);
            sendCmd(out, "DATA"); readResponse(in);

            String message = "Subject: Your Security Code - Smart Inventory System\r\n" +
                             "From: " + senderEmail + "\r\n" +
                             "To: " + toEmail + "\r\n\r\n" +
                             "Hello,\r\n\r\n" +
                             "A password reset request was made for your account.\r\n" +
                             "Your 6-digit reset code is: " + code + "\r\n\r\n" +
                             "If you did not request this, please ignore this email.\r\n" +
                             ".\r\n";
                             
            sendCmd(out, message); readResponse(in);
            sendCmd(out, "QUIT"); readResponse(in);

            socket.close();
            return true;
        } catch (Exception e) {
            System.out.println("Email Error: " + e.getMessage());
            return false;
        }
    }

    // Sends user feedback directly to the developer's email
    public static boolean sendFeedbackEmail(String toEmail, String userName, String userEmail, String feedbackText, String senderEmail, String appPassword) {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket("smtp.gmail.com", 465);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            readResponse(in);
            sendCmd(out, "HELO smtp.gmail.com"); readResponse(in);
            sendCmd(out, "AUTH LOGIN"); readResponse(in);
            sendCmd(out, Base64.getEncoder().encodeToString(senderEmail.getBytes())); readResponse(in);
            sendCmd(out, Base64.getEncoder().encodeToString(appPassword.getBytes())); readResponse(in);
            sendCmd(out, "MAIL FROM:<" + senderEmail + ">"); readResponse(in);
            sendCmd(out, "RCPT TO:<" + toEmail + ">"); readResponse(in);
            sendCmd(out, "DATA"); readResponse(in);

            String message = "Subject: App Feedback from " + userName + " - Smart Inventory\r\n" +
                             "From: " + senderEmail + "\r\n" +
                             "To: " + toEmail + "\r\n\r\n" +
                             "You have received new feedback from the Smart Inventory System:\r\n\r\n" +
                             "User Name: " + userName + "\r\n" +
                             "User Email: " + userEmail + "\r\n\r\n" +
                             "Message:\r\n" + feedbackText + "\r\n" +
                             ".\r\n";
                             
            sendCmd(out, message); readResponse(in);
            sendCmd(out, "QUIT"); readResponse(in);

            socket.close();
            return true;
        } catch (Exception e) {
            System.out.println("Feedback Email Error: " + e.getMessage());
            return false;
        }
    }

    private static void sendCmd(BufferedWriter out, String cmd) throws IOException {
        out.write(cmd + "\r\n"); out.flush();
    }

    private static void readResponse(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (line.length() >= 4 && line.charAt(3) == ' ') break;
        }
    }
    // Sends an automated low-stock report to the manager
    public static boolean sendLowStockAlert(String toEmail, String reportData, String senderEmail, String appPassword) {
        try {
            javax.net.ssl.SSLSocketFactory factory = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
            javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) factory.createSocket("smtp.gmail.com", 465);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            readResponse(in);
            sendCmd(out, "HELO smtp.gmail.com"); readResponse(in);
            sendCmd(out, "AUTH LOGIN"); readResponse(in);
            sendCmd(out, Base64.getEncoder().encodeToString(senderEmail.getBytes())); readResponse(in);
            sendCmd(out, Base64.getEncoder().encodeToString(appPassword.getBytes())); readResponse(in);
            sendCmd(out, "MAIL FROM:<" + senderEmail + ">"); readResponse(in);
            sendCmd(out, "RCPT TO:<" + toEmail + ">"); readResponse(in);
            sendCmd(out, "DATA"); readResponse(in);

            String message = "Subject: URGENT: Low Stock Alert - Smart Inventory\r\n" +
                             "From: " + senderEmail + "\r\n" +
                             "To: " + toEmail + "\r\n\r\n" +
                             "System Alert: The following items are critically low on stock.\r\n" +
                             "Please review and dispatch orders to suppliers immediately:\r\n\r\n" +
                             reportData + "\r\n\r\n" +
                             "End of Report.\r\n.\r\n";
                             
            sendCmd(out, message); readResponse(in);
            sendCmd(out, "QUIT"); readResponse(in);

            socket.close();
            return true;
        } catch (Exception e) {
            System.out.println("Alert Email Error: " + e.getMessage());
            return false;
        }
    }
}