package com.shakshin.mikrotik.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/*
    Utility class to send email via linux mail command
 */
public class Mail {
    public static void send(String subject, String body) {
        Logger.trace("Sending email with subject " + subject);
        Runtime run = Runtime.getRuntime();

        Process p = null;
        String cmd = "/usr/bin/mail root";

        try {
            p = run.exec(new String[] {"/usr/bin/mail", "root", "-s", subject});
            OutputStream out =  p.getOutputStream();
            out.write(body.getBytes());
            out.flush();
            out.close();
            p.waitFor();
            String resp = "";
            InputStream in = p.getErrorStream();
            while (true) {
                int i = in.read();
                if (i == -1) break;
                byte b = (byte) i;
                byte[] bb = new byte[1];
                bb[0] = b;
                resp += new String(bb);
            }
            if (!resp.equals("")) {
                Logger.trace("Mail subsystem response: " +resp);
            } else {
                Logger.trace("Mail sent");
            }
        } catch (IOException e) {
            Logger.trace("Error sending email: " + e.getMessage());
        } catch (InterruptedException ie) {

        } finally {
            p.destroy();
        }

    }
}
