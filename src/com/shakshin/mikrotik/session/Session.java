package com.shakshin.mikrotik.session;

import com.shakshin.mikrotik.client.Connection;
import com.shakshin.mikrotik.util.Logger;

import java.io.IOException;

/*
    Class representing routerboard session
 */

public class Session {
    private class SessionException extends Exception { // exception for internal use
        private String message;
        public SessionException(String m) {
            message = m;
        }

        @Override
        public String getMessage() {
            return  message;
        }
    }

    private boolean useSsl = false;
    private String host = null;
    private Integer port = 8728;
    private String user = null;
    private String password = null;

    private boolean reConnectAllowed = false;

    private Connection connection = null;

    public void setUseSsl() {
        useSsl = true;
    }

    public void setReConnectAllowed(boolean reConnectAllowed) {
        this.reConnectAllowed = reConnectAllowed;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isReConnectAllowed() {
        return reConnectAllowed;
    }

    public boolean reconnect() {
        if (!reConnectAllowed) return false;

        connection.close();
        connection = null;

        try {
            open();
        } catch (Exception e) {}
        return true;
    }

    // standard log in implementation
    private void login() throws IOException {
        Logger.trace("Performing log in request");
        Request rq = newRequest();
        rq.setNoLog(true);
        rq.setCommand("/login");
        rq.addParam("name", user);
        rq.addParam("password", password);
        Response rs = rq.execute();
        if (!rs.successful) {
            Logger.trace("Not logged in");
            throw new IOException("Not logged in");
        }
    }

    private void openInternal() { // internal open() implementation to support reconnection
        boolean connected = false;
        do {
            try {
                Logger.trace("Opening connection");
                connection = new Connection(host, port, useSsl);
                login();
                connected = true;
            } catch (IOException e) {
                if (!reConnectAllowed) {
                    connected = true; // just to exit the loop
                } else {
                    try {
                        Logger.trace("Will reconnect after a period");
                        Thread.sleep(1000); // wait before reconnect
                        Logger.trace("Reconnecting");
                    } catch (InterruptedException ei) {
                        return;
                    }
                }
            }

        } while (!connected);
    }

    public void open() throws SessionException, IOException {
        if (host == null) {
            throw new SessionException("No host specified");
        }
        if (port == null) {
            throw new SessionException("No port specified");
        }
        if (user == null) {
            throw new SessionException("No user specified");
        }
        if (password == null) {
            throw new SessionException("No password specified");
        }
        connection = null;
        openInternal();
    }

    public void close() {
        Logger.trace("Closing connection");
        connection.close();
    }

    // prepare new request instance within current session
    public Request newRequest() {
        Request rq = new Request(this);

        return rq;
    }
}
