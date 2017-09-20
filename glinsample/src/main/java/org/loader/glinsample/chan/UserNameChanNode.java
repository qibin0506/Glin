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

public class UserNameChanNode extends ChanNode {

    @Override
    public void run(final Context ctx) {

        Log.d("Glin", "UserNameChanNode");

        final SharedPreferences sp = App.get().getSharedPreferences("sp",
                android.content.Context.MODE_PRIVATE);

        if (!TextUtils.isEmpty(sp.getString("name", null))) {
            next();
            return;
        }

        Net.get().create(Api.class, getClass().getName()).uid("name").enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Result<UserInfo> result) {
                if (result.isOK()) {
                    // refresh name param
                    ctx.getCall().getParams().add("name", result.getResult().getName());
                    sp.edit().putString("name", result.getResult().getName()).apply();

                    next();
                } else {
                    cancel(5000, "no username");
                }
            }
        });
    }
}
