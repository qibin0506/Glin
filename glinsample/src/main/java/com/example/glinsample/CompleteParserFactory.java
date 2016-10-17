package com.haiersmart.commonbizlib.parser;

import org.loader.glin.factory.ParserFactory;
import org.loader.glin.parser.Parser;

/**
 * Created by qibin on 2016/10/17.
 */

public class CompleteParserFactory implements ParserFactory {
    @Override
    public Parser getParser() {
        return new CompleteObjectParser();
    }

    @Override
    public Parser getListParser() {
        return new CompleteListParser();
    }
}
