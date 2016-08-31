package org.loader.glin.client;

import org.loader.glin.factory.ParserFactory;
import org.loader.glin.interceptor.IResultInterceptor;

import java.util.LinkedHashMap;

/**
 * Created by qibin on 2016/7/13.
 */

public interface IClient extends IRequest {
    void cancel(final Object tag);
    void parserFactory(ParserFactory factory);
    void timeout(long ms);
    void debugMode(boolean debug);
    LinkedHashMap<String, String> headers();
    void resultInterceptor(IResultInterceptor interceptor);
}
