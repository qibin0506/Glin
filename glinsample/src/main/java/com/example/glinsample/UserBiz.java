package com.example.glinsample;

import org.loader.glin.annotation.Arg;
import org.loader.glin.annotation.GET;
import org.loader.glin.annotation.POST;
import org.loader.glin.call.Call;

/**
 * Created by qibin on 2016/7/14.
 */

public interface UserBiz {
	@ShouldCache
    @POST("/users/list")
    Call<User> list(@Arg("name") String userName);
}
