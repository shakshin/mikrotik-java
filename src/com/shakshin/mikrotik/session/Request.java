package com.shakshin.mikrotik.session;

import com.shakshin.mikrotik.client.ApiSentence;
import com.shakshin.mikrotik.util.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/*
    Class representing API request
 */
public class Request {
    private class RequestParam {
        public String name = null;
        public String value = null;
        public RequestParam(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
    private class RequestQuery {
        public String name = null;
        public String value = null;
        public String sign = null;
        public RequestQuery(String name, String value, String sign) {
            this.name = name;
            this.value = value;
            this.sign = sign;
        }
    }
    private Session session = null;

    private String command = null;

    private boolean noLog = false;

    public void setNoLog(boolean noLog) {
        this.noLog = noLog;
    }

    private List<RequestParam> params = new LinkedList<>();
    private List<RequestQuery> queries = new LinkedList<>();

    public Request(Session session) {
        this.session = session;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void addParam(String name, String value) {
        params.add(new RequestParam(name, value));
    }

    public void addQuery(String name, String sign, String value) {
        queries.add(new RequestQuery(name, value, sign));
    }

    public Response execute() {
        Logger.trace("Executing API request");
        Response resp = new Response();
        if (command == null) {
            resp.successful = false;
            resp.systemError = true;
            resp.errorMessage = "No command specified";
            return resp;
        }

        ApiSentence sent = new ApiSentence(command);

        // preparing words for regular parameters
        for (RequestParam p : params) {
            if (p.name == null) {
                resp.successful = false;
                resp.systemError = true;
                resp.errorMessage = "No parameter name specified";
                return resp;
            }
            String src = "=" + p.name + "=" + (p.value == null ? "" : p.value );
            sent.words.add(src);
        }

        // preparing words for query parameters
        for (RequestQuery q : queries) {
            if (q.name == null) {
                resp.successful = false;
                resp.systemError = true;
                resp.errorMessage = "No query parameter name specified";
                return resp;
            }
            String src = "?" + (q.sign != null ? q.sign : "") + q.name + (q.value == null ? "" : "=" + q.value);
            sent.words.add(src);
        }

        // executing request and preparing response
        try {
            LinkedList<ApiSentence> rs = session.getConnection().send(sent, noLog);
            resp.rawResponse = rs;
            // successful response should contain !re or !done sentence
            if (!rs.isEmpty() && !rs.get(0).words.get(0).equals("!re") && !rs.get(0).words.get(0).equals("!done")) {
                resp.successful = false;
            }
            Logger.trace("Parsing response sentences");
            for (ApiSentence s : rs) { // loop through response sentences to fill response items
                HashMap<String, String> item = new HashMap<>();
                for (String w : s.words) {
                    if (w.length() > 0 && w.charAt(0) == '=') {
                        String[] parts = w.split("=");
                        String name = "";
                        String val = "";
                        if (parts.length < 2) continue;
                        name = parts[1];
                        if (parts.length > 2)
                            val = parts[2];

                        item.put(name, val);
                    }
                }
                if (item.keySet().size() > 0) {
                    resp.items.add(item);
                }
            }
        } catch (IOException e) {
            // IO exception appears when connection is broken
            // we can try to resconnect and re-execute the same request if required
            Logger.trace("IOException during request execution. Probably connection issues.");
            if (!session.isReConnectAllowed()) {
                Logger.trace("Reconnect is not enabled");
                resp.successful = false;
                resp.systemError = true;
                resp.errorMessage = e.getMessage();
            } else {
                Logger.trace("Will try tp reconnect");
                session.reconnect();
                Logger.trace("Reconnected. Executing request again.");
                resp = execute();
            }
        }

        return resp;
    }
}
