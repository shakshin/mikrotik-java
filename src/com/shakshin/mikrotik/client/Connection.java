package com.shakshin.mikrotik.client;

import com.shakshin.mikrotik.util.Logger;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.LinkedList;

/*
    Class representing API connection
 */

public class Connection {

    private Socket socket = null; // socket to handle connection

    private void connect(String host, Integer port) throws IOException {
        try {
            socket = new Socket(host, port);
        } catch (IOException ioe) {
            throw new IOException("Can not open connection to the routerboard", ioe);
        }
    }

    private void connectSsl(String host, Integer port) throws IOException {
        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = ssf.createSocket(host, port);

        } catch (Exception e) {
            throw new IOException("Can not open SSL connection to the routerboard", e);
        }
    }

    public Connection(String host, Integer port) throws IOException {
        connect(host, port);
    }

    public Connection(String host) throws IOException {
        connect(host, 8728);
    }

    public Connection(String host, Integer port, boolean ssl) throws IOException {
        if (!ssl) {
            connect(host, port);
        } else {
            connectSsl(host, port);
        }
    }

    // send sentence with request and read response sentences
    public LinkedList<ApiSentence> send(ApiSentence sntc, boolean noLog) throws IOException {
        // noLog is used to hide login request with password from trace log
        if(!noLog) Logger.trace("ApiSentence to be sent to the router: " + sntc.toString());

        sntc.send(socket.getOutputStream()); // send request

        InputStream in = socket.getInputStream();
        ApiSentence resp = ApiSentence.read(socket.getInputStream()); //read the first sentence of response
        LinkedList<ApiSentence> res = new LinkedList<>();
        while (true) { // loop through subsequent sentences of response
            if(!noLog) Logger.trace("ApiSentence received from router: " + resp.toString());
            if (resp.words.size() == 0) break; // empty sentence means end of response
            res.add(resp);
            if (resp.words.get(0).equals("!done")) break; // !done sentence means end of response as well
            resp = ApiSentence.read(socket.getInputStream());
        }
        return res;
    }

    public void close() {
        try {
            socket.close();
        } catch (Exception e) {

        }
    }

}
