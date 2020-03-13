package com.shakshin.mikrotik.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/*
    Class representing elementary API communication unit, which could be sent to/from API endpoint
 */

public class ApiSentence {

    public LinkedList<String> words = new LinkedList<>();

    public ApiSentence(String... word) {
        for (int i =0; i < word.length; i ++) words.add(word[i]);
    }

    public static ApiSentence read(InputStream in) throws IOException {
        ApiSentence res= new ApiSentence();
        while (true) { // read words of sentence

            // have to read word length first
            byte[] bbuf = {0x00, 0x00, 0x00, 0x00};

            byte b = (byte)in.read();
            if ((b & 0x80) == 0x00) {
                bbuf[3] = b;
            } else if ((b & 0xC0) == 0x80) {
                bbuf[2] = (byte)(b & ~0xC0);
                bbuf[3] = (byte)in.read();
            } else if ((b & 0xE0) == 0xC0) {
                bbuf[1] = (byte)(b & ~0xE0);
                bbuf[2] = (byte)in.read();
                bbuf[3] = (byte)in.read();
            } else if ((b & 0xF0) == 0xE0) {
                bbuf[0] = (byte)(b & ~0xF0);
                bbuf[1] = (byte)in.read();
                bbuf[2] = (byte)in.read();
                bbuf[3] = (byte)in.read();
            } else if ((b & 0xF8) == 0xF0) {
                bbuf[0] = (byte)in.read();
                bbuf[1] = (byte)in.read();
                bbuf[2] = (byte)in.read();
                bbuf[3] = (byte)in.read();
            }
            ByteBuffer bb = ByteBuffer.wrap(bbuf);
            int len = bb.getInt();

            if (len == 0) { // zero-length word means end of sentence
                return res;
            }

            // then read word itself
            byte[] buff = new byte[len];
            int br = in.read(buff);
            if (br < len) {
                throw new IOException("No enough bytes in buffer");
            }
            String word = new String(buff);
            res.words.add(word); // and add word to list
        }
    }

    public void send(OutputStream out) throws IOException {
        for (String w : words) { // loop through words of sentence

            // format and send word length
            int l = w.length();
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(l);
            if (l <= 0x7F) {
                out.write(bb.get(3));
            } else if (l <= 0x3FFF) {
                out.write(bb.get(2) | 0x80);
                out.write(bb.get(3));
            } else if (l <= 0x1FFFFF) {
                out.write(bb.get(1) | 0xC0);
                out.write(bb.get(2));
                out.write(bb.get(3));
            } else if (l <= 0xFFFFFFF) {
                out.write(bb.get(0) | 0xE0);
                out.write(bb.get(1));
                out.write(bb.get(2));
                out.write(bb.get(3));
            } else if (l >= 0x10000000) {
                out.write(0xF0);
                out.write(bb.get(0));
                out.write(bb.get(1));
                out.write(bb.get(2));
                out.write(bb.get(3));
            }
            out.write(w.getBytes()); // send word itself
        }
        out.write(0x00); // empty word to end the sentence
    }

    @Override
    public String toString() { // string representation of sentence and it's content
        String res = "ApiSentence [ ";
        boolean first = true;
        for (String word : words) {
            if (first) {
                first = false;
            } else {
                res += ", ";
            }
            res += word;
        }
        res += " ];";
        return res;
    }
}
