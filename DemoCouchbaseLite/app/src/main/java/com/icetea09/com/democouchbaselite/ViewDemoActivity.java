package com.icetea09.com.democouchbaselite;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ViewDemoActivity extends ActionBarActivity {

    private static final String VIEW_PHONE = "VIEW_PHONE";

    private ListView mLvContacts;

    private Database mDatabase;
    private Manager mManager;
    private View mPhoneView;
    private Query mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_demo);

        mLvContacts = (ListView)findViewById(R.id.lv_contacts);

        try {
            mManager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
            mDatabase = mManager.getDatabase("icetea09-database");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        //Create dummy data
        saveContactInfo("Trinh", "Le", "01656132966");
        saveContactInfo("Hermione", "Granger", "154879161");
        saveContactInfo("Ron", "Weasley", "02486232");
        saveContactInfo("Draco", "Malfoy", "0154876");

        mPhoneView = mDatabase.getView(VIEW_PHONE);
        mPhoneView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                String phone = (String)document.get(FirstDemoActivity.FIELD_PHONE_NUMBER);
                emitter.emit(phone, document.get(FirstDemoActivity.FIELD_FIRST_NAME) + " " + document.get(FirstDemoActivity.FIELD_LAST_NAME));
            }
        }, "1");

        /*mPhoneView.setMapReduce(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                List<String> phones = (List) document.get(FirstDemoActivity.FIELD_PHONE_NUMBER);
                for (String phone : phones) {
                    emitter.emit(phone, document.get(FirstDemoActivity.FIELD_FIRST_NAME) + " " + document.get(FirstDemoActivity.FIELD_LAST_NAME));
                }
            }
        }, new Reducer() {
            @Override
            public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
                return values.size();
            }
        }, "1");*/

        mQuery = mDatabase.getView(VIEW_PHONE).createQuery();
        mQuery.setDescending(true);
        mQuery.setLimit(20);

        List<String> values = new ArrayList<>();

        try{
            QueryEnumerator result = mQuery.run();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                values.add(row.getValue().toString() + " - " + row.getKey().toString());
            }
        }
        catch (CouchbaseLiteException e){
            e.printStackTrace();
        }

        // Define a new Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        mLvContacts.setAdapter(adapter);

    }

    private boolean saveContactInfo(String firstName, String lastName, String phoneNumber) {
        Document document = mDatabase.createDocument();

        Map<String, Object> properties = new HashMap();
        properties.put(FirstDemoActivity.FIELD_FIRST_NAME, firstName);
        properties.put(FirstDemoActivity.FIELD_LAST_NAME, lastName);
        properties.put(FirstDemoActivity.FIELD_PHONE_NUMBER, phoneNumber);
        try {
            document.putProperties(properties);
            return true;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return false;
        }

    }

}
