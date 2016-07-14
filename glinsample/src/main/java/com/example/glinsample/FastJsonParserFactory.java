package com.example.glinsample;

import org.loader.glin.parser.*;
import org.loader.glin.parser.Parser;

/**
 * Created by qibin on 2016/7/13.
 */

public class FastJsonParserFactory implements ParserFactory {

    @Override
    public Parser getParser() {
        return new com.example.glinsample.Parser("data");
    }

    @Override
    public Parser getListParser() {
        return new ListParser("data");
    }
}
