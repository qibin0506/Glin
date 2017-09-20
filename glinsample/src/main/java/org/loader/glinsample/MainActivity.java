package org.loader.glinsample;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.loader.glin.Callback;
import org.loader.glin.Result;
import org.loader.glin.call.Call;
import org.loader.glinsample.api.Api;
import org.loader.glinsample.bean.UserInfo;
import org.loader.glinsample.chan.CheckEnvChanNode;
import org.loader.glinsample.chan.EndChanNode;
import org.loader.glinsample.chan.UserIdChanNode;
import org.loader.glinsample.chan.UserNameChanNode;
import org.loader.glinsample.utils.Net;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoTextView = (TextView) findViewById(R.id.info);
        mInfoTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final SharedPreferences sp = App.get().getSharedPreferences("sp",
                android.content.Context.MODE_PRIVATE);

        String uid = sp.getString("uid", "");
        String name = sp.getString("name", "");

        Call<UserInfo> call = Net.get().create(Api.class, getClass().getName()).info(uid, name);

        call.before(new CheckEnvChanNode())
                .next(new UserIdChanNode())
                .next(new UserNameChanNode())
                .after(new EndChanNode())
                .enqueue(new Callback<UserInfo>() {
                    @Override
                    public void onResponse(Result<UserInfo> result) {
                        if (result.isOK()) {
                            String uid = result.getResult().getId();
                            String name = result.getResult().getName();
                            int age = result.getResult().getAge();
                            Toast.makeText(MainActivity.this, uid + ";" + name + ";" + age, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "code:" + result.getCode() + ",msg:" + result.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
