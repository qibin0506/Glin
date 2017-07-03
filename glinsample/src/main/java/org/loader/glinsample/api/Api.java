/**
 * Copyright 2017,Smart Haier.All rights reserved
 */
package org.loader.glinsample.api;

import org.loader.glin.annotation.Arg;
import org.loader.glin.annotation.GET;
import org.loader.glin.annotation.POST;
import org.loader.glin.annotation.Path;
import org.loader.glin.call.Call;
import org.loader.glinsample.bean.UserInfo;

/**
 * <p class="note">File Note</p>
 * created by qibin at 2017/7/3 
 */
public interface Api {

    @GET("/user/{:path}/")
    Call<UserInfo> uid(@Path("path") String path);

    @POST("/user/info/")
    Call<UserInfo> info(@Arg("uid") String id, @Arg("name") String name);
}
