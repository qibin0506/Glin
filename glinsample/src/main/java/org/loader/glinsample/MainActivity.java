package org.loader.glinsample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
    private boolean canClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoTextView = (TextView) findViewById(R.id.info);
        mInfoTextView.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            canClick = true;
        }
    }

    @Override
    public void onClick(View v) {
        if (!canClick) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
            return;
        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 1000) { return;}

        if (grantResults.length <= 0) { return;}

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            canClick = true;
            onClick(null);
            return;
        }

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("NOTICE")
                    .setMessage("PLZ Grant me permission!!")
                    .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri packageURI = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    }).setNegativeButton("Cancel", null).show();
        }
    }
}
