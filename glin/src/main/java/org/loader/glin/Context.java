/**
 * Copyright 2017,Smart Haier.All rights reserved
 */
package org.loader.glin;

import org.loader.glin.call.Call;

/**
 * <p class="note">File Note</p>
 * created by qibin at 2017/7/1 
 */
public class Context {
    private Call<?> call;
    private Result<?> result;
    private RawResult rawResult;

    public Context() {}

    public Context(Call<?> call) {
        this.call = call;
    }

    public Call<?> getCall() {
        return call;
    }

    public void setCall(Call<?> call) {
        this.call = call;
    }

    public Result<?> getResult() {
        return result;
    }

    public void setResult(Result<?> result) {
        this.result = result;
    }

    public RawResult getRawResult() {
        return rawResult;
    }

    public void setRawResult(RawResult rawResult) {
        this.rawResult = rawResult;
    }
}
