package org.loader.glin;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by qibin on 2016/7/13.
 */

public class Params {

    public static final String DEFAULT_JSON_KEY = "json";

    private LinkedHashMap<String, String> mParams;
    private LinkedHashMap<String, File> mFiles;

    public Params() {
        mParams = new LinkedHashMap<>();
        mFiles = new LinkedHashMap<>();
    }

    public Params(String key, Object value) {
        this();
        add(key, value);
    }

    public Params add(String key, Object value) {
        if (key == null) { return this;}
        if (value == null) { mParams.put(key, null);}
        else if (value instanceof File) { mFiles.put(key, (File) value);}
        else { mParams.put(key, value.toString());}
        return this;
    }

    public LinkedHashMap<String, String> get() {
        return mParams;
    }

    public String getParams(String key) {
        return mParams.get(key);
    }

    public File getFile(String key) {
        return mFiles.get(key);
    }

    public LinkedHashMap<String, File> files() {
        return mFiles;
    }

    public void remove(String key) {
        if (mParams.containsKey(key)) { mParams.remove(key);}
        if (mFiles.containsKey(key)) { mFiles.remove(key);}
    }

    public String encode() {
        String query = null;
        for (Map.Entry<String, String> entry : mParams.entrySet()) {
            if (query == null) { query = entry.getKey() + "=" + entry.getValue();}
            else { query += "&" + entry.getKey() + "=" + entry.getValue();}
        }

        return query;
    }

    public Iterator<String> iterator() {
        return mParams.keySet().iterator();
    }

    public Iterator<String> fileIterator() {
        return mFiles.keySet().iterator();
    }

    public boolean isEmpty() {
        return mParams.isEmpty() && mFiles.isEmpty();
    }
}
