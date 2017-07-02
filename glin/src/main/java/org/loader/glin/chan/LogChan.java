package org.loader.glin.chan;

import org.loader.glin.Context;
import org.loader.glin.helper.LogHelper;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qibin on 2016/7/13.
 */

public class LogChan extends Chan implements Cloneable {

    private boolean isDebug;
    private LogHelper.LogPrinter mLogPrinter;

    public LogChan(boolean debug, LogHelper.LogPrinter printer) {
        isDebug = debug;
        mLogPrinter = printer;
    }

    @Override
    public void run(Context ctx) {
        if (isBeforeCall()) { prntRequestLog(ctx);}
        else { prntResponseLog(ctx);}
        next();
    }

    private void prntResponseLog(Context ctx) {
        if (!isDebug) { return;}

        StringBuilder builder = new StringBuilder();
        builder.append("statusCode: ").append(ctx.getRawResult().getStatusCode()).append("\n");
        builder.append("message: ").append(replaceBlank(ctx.getRawResult().getMessage())).append("\n");
        builder.append("response: ").append(replaceBlank(ctx.getRawResult().getResponse()));

        mLogPrinter.print("Glin", builder.toString());
    }

    private void prntRequestLog(Context ctx) {
        if (!isDebug) { return;}

        StringBuilder builder = new StringBuilder();
        builder.append("URL->").append(ctx.getCall().getUrl()).append("\n");

        builder.append("Method->").append(ctx.getCall().getClass().getSimpleName()).append("\n");

        builder.append("Params->");
        LinkedHashMap<String, String> map = ctx.getCall().getParams().get();
        for(Map.Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if(value == null) { continue;}
            builder.append(entry.getKey()).append(":").append(value).append(";");
        }

        builder.append("\nFiles->");
        LinkedHashMap<String, File> files = ctx.getCall().getParams().files();
        for(Map.Entry<String, File> entry : files.entrySet()) {
            File file = entry.getValue();
            if(file == null) { continue;}
            builder.append(entry.getKey()).append(":").append(file.getName());
        }
        builder.append("\n");

        if (ctx.getCall().getHeaders() != null && !ctx.getCall().getHeaders().isEmpty()) {
            for(Map.Entry<String, String> entry : ctx.getCall().getHeaders().entrySet()) {
                builder.append("Header->").append(entry.getKey()).append(":")
                        .append(entry.getValue()).append("\n");
            }
        }

        mLogPrinter.print("Glin", builder.toString());
    }

    private static String replaceBlank(String str) {
        String dest = str;
        if (dest != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }

        return dest;
    }

    @Override
    public LogChan clone() throws CloneNotSupportedException {
        return (LogChan) super.clone();
    }
}
