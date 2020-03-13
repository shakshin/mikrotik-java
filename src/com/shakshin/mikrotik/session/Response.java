package com.shakshin.mikrotik.session;

import com.shakshin.mikrotik.client.ApiSentence;

import java.util.HashMap;
import java.util.LinkedList;

public class Response {
    public boolean successful = true;
    public boolean systemError = false;
    public String errorMessage = null;

    public LinkedList<ApiSentence> rawResponse = null;

    public LinkedList<HashMap<String, String>> items = new LinkedList<HashMap<String, String>>();

}
