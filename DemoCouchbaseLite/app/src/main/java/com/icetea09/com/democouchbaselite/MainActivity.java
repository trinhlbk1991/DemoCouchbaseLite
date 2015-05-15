package com.icetea09.com.democouchbaselite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private Database mDatabase;
    private Manager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_document_demo).setOnClickListener(this);
        findViewById(R.id.btn_view_demo).setOnClickListener(this);

        try {
            mManager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
            mDatabase = mManager.getDatabase("icetea09-database");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_document_demo:
                navigateTo(FirstDemoActivity.class);
                break;
            case R.id.btn_view_demo:
                navigateTo(ViewDemoActivity.class);
                break;
        }
    }

    private void navigateTo(Class<?> dest){
        Intent intent = new Intent(this, dest);
        startActivity(intent);
    }

    public static void alert(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
