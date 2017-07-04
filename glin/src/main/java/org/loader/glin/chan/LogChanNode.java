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

public class LogChanNode extends ChanNode implements Cloneable {

    private boolean isDebug;
    private LogHelper.LogPrinter mLogPrinter;

    public LogChanNode(boolean debug, LogHelper.LogPrinter printer) {
        isDebug = debug;
        mLogPrinter = printer;
    }

    @Override
    public void run(Context ctx) {
        if (isBeforeCall()) {
            prntRequestLog(ctx);
        }else {
            prntResponseLog(ctx);
            // do not call next(), when result is cache
            if (ctx.getResult().isCache()) { return;}
        }
        next();
    }

    private void prntResponseLog(Context ctx) {
        if (!isDebug) { return;}

        StringBuilder builder = new StringBuilder();
        builder.append("================== Glin Start Response ==================\n\n");

        if (ctx.getResult().isCache()) {
            builder.append(" is cache-> true \n")
                    .append(" use cache-> ")
                    .append(ctx.getCall().getUrl())
                    .append("\n");
        } else {
            builder.append(" is cache-> false \n");
            builder.append(" status code-> ").append(ctx.getRawResult().getStatusCode()).append("\n");
            builder.append(" message-> ").append(replaceBlank(ctx.getRawResult().getMessage())).append("\n");
            builder.append(" response-> ").append(replaceBlank(ctx.getRawResult().getResponse())).append("\n");

            if (ctx.getCall().shouldCache()) {
                builder.append(" cache result-> ").append(ctx.getCall().getUrl()).append("\n");
            }
        }

        builder.append("\n================== Glin  End  Response ==================");

        mLogPrinter.print("Glin", builder.toString());

    }

    private void prntRequestLog(Context ctx) {
        if (!isDebug) { return;}

        StringBuilder builder = new StringBuilder();
        builder.append("================== Glin Start Request ==================\n\n");

        builder.append(" url-> ").append(ctx.getCall().getUrl()).append("\n");
        builder.append(" method-> ").append(ctx.getCall().getClass().getSimpleName()).append("\n");

        if (!ctx.getCall().getParams().isEmpty()) {
            builder.append(" params-> \n");
            LinkedHashMap<String, String> map = ctx.getCall().getParams().get();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String value = entry.getValue();
                if (value == null) { continue;}
                builder.append(" \t").append(entry.getKey()).append(":").append(value).append("\n");
            }
        }

        if (!ctx.getCall().getParams().files().isEmpty()) {
            builder.append(" files-> \n");
            LinkedHashMap<String, File> files = ctx.getCall().getParams().files();
            for (Map.Entry<String, File> entry : files.entrySet()) {
                File file = entry.getValue();
                if (file == null) { continue;}
                builder.append(" \t").append(entry.getKey()).append(":")
                        .append(file.getName()).append("\n");
            }
        }

        if (ctx.getCall().getHeaders() != null && !ctx.getCall().getHeaders().isEmpty()) {
            builder.append(" header-> \n");
            for(Map.Entry<String, String> entry : ctx.getCall().getHeaders().entrySet()) {
                builder.append(" \t").append(entry.getKey()).append(":")
                        .append(entry.getValue()).append("\n");
            }
        }

        builder.append("\n================== Glin  End  Request ==================");
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
    public LogChanNode clone() throws CloneNotSupportedException {
        return (LogChanNode) super.clone();
    }
}
