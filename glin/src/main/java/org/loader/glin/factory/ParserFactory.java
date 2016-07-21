package org.loader.glin.factory;

import org.loader.glin.parser.Parser;

/**
 * Created by qibin on 2016/7/13.
 */

public interface ParserFactory {
    Parser getParser();
    Parser getListParser();
}
