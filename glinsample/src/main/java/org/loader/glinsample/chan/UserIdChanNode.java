package org.loader.glinsample.chan;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.loader.glin.Callback;
import org.loader.glin.Context;
import org.loader.glin.Result;
import org.loader.glin.chan.ChanNode;
import org.loader.glinsample.App;
import org.loader.glinsample.api.Api;
import org.loader.glinsample.bean.UserInfo;
import org.loader.glinsample.utils.Net;

public class UserIdChanNode extends ChanNode {

    @Override
    public void run(final Context ctx) {
        final SharedPreferences sp = App.get().getSharedPreferences("sp",
                android.content.Context.MODE_PRIVATE);

        Log.d("Glin", "UserIdChanNode");

        if (!TextUtils.isEmpty(sp.getString("uid", null))) {
            next();
            return;
        }

        Net.get().create(Api.class, getClass().getName()).uid("id").enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Result<UserInfo> result) {
                if (result.isOK()) {
                    // refresh uid param
                    ctx.getCall().getParams().add("uid", result.getResult().getId());
                    sp.edit().putString("uid", result.getResult().getId()).apply();

                    next();
                } else {
                    cancel(4000, "no uid");
                }
            }
        });
    }
}
