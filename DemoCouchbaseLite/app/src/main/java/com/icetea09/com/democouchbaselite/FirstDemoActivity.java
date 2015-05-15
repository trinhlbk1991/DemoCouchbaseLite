package com.icetea09.com.democouchbaselite;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Revision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.icetea09.com.democouchbaselite.models.ContactInfo;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class FirstDemoActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = FirstDemoActivity.class.getSimpleName();
    private static final int REQUEST_CODE_GALLERY = 1;
    public static final String CONTACT_INFO_DOCUMENT_ID = "6ecbeaf0-fae5-11e4-b939-0800200c9a66";
    public static final String FIELD_FIRST_NAME = "firstName";
    public static final String FIELD_LAST_NAME = "lastName";
    public static final String FIELD_PHONE_NUMBER = "phoneNumber";

    private EditText mEtFirstName;
    private EditText mEtLastName;
    private EditText mEtPhoneNumber;
    private ImageView mImgAvatar;

    private Database mDatabase;
    private Manager mManager;
    private ContactInfo mContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_demo);

        mEtFirstName = (EditText) findViewById(R.id.et_first_name);
        mEtLastName = (EditText) findViewById(R.id.et_last_name);
        mEtPhoneNumber = (EditText) findViewById(R.id.et_phone_number);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
        mImgAvatar = (ImageView) findViewById(R.id.img_avatar);
        mImgAvatar.setOnClickListener(this);

        try {
            mManager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
            mDatabase = mManager.getDatabase("icetea09-database");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        mContact = getContactInfo();
        if (mContact != null) {
            mEtFirstName.setText(mContact.getFirstName());
            mEtLastName.setText(mContact.getLastName());
            mEtPhoneNumber.setText(mContact.getPhoneNumber());
        }

        Bitmap bitmap = getAvatar();
        if(bitmap != null){
            mImgAvatar.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imgDecodableString = cursor.getString(columnIndex);
            cursor.close();
            mImgAvatar.setImageBitmap(BitmapFactory
                    .decodeFile(imgDecodableString));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                if (mContact == null) {
                    if (saveContactInfo(mEtFirstName.getText().toString(), mEtLastName.getText().toString(), mEtPhoneNumber.getText().toString())) {
                        MainActivity.alert(getApplicationContext(), "Saved");
                    } else {
                        MainActivity.alert(getApplicationContext(), "Cannot save contact info!");
                    }
                } else {
                    if (updateContactInfo(mEtFirstName.getText().toString(), mEtLastName.getText().toString(), mEtPhoneNumber.getText().toString())) {
                        MainActivity.alert(getApplicationContext(), "Updated");
                    } else {
                        MainActivity.alert(getApplicationContext(), "Cannot update contact info!");
                    }
                }
                saveAvatar();
                break;
            case R.id.btn_delete:
                if (deleteContactInfo()) {
                    MainActivity.alert(getApplicationContext(), "Deleted");
                } else {
                    MainActivity.alert(getApplicationContext(), "Cannot delete contact info!");
                }
                break;
            case R.id.img_avatar:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
                break;
        }
    }

    private ContactInfo getContactInfo() {
        try {
            Document doc = mDatabase.getDocument(CONTACT_INFO_DOCUMENT_ID);
            String firstName = doc.getProperty(FIELD_FIRST_NAME).toString();
            String lastName = doc.getProperty(FIELD_LAST_NAME).toString();
            String phoneNumber = doc.getProperty(FIELD_PHONE_NUMBER).toString();
            return new ContactInfo(firstName, lastName, phoneNumber);
        } catch (Exception ex) {
            Log.e(TAG, "Cannot get contact info", ex);
        }
        return null;
    }

    private boolean saveContactInfo(String firstName, String lastName, String phoneNumber) {
        Document document = mDatabase.getDocument(CONTACT_INFO_DOCUMENT_ID);

        Map<String, Object> properties = new HashMap();
        properties.put(FIELD_FIRST_NAME, firstName);
        properties.put(FIELD_LAST_NAME, lastName);
        properties.put(FIELD_PHONE_NUMBER, phoneNumber);
        try {
            document.putProperties(properties);
            return true;
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot save document", e);
            return false;
        }

    }

    private boolean updateContactInfo(String firstName, String lastName, String phoneNumber) {
        Document document = mDatabase.getDocument(CONTACT_INFO_DOCUMENT_ID);

        Map<String, Object> properties = new HashMap();
        properties.putAll(document.getProperties());
        properties.put(FIELD_FIRST_NAME, firstName);
        properties.put(FIELD_LAST_NAME, lastName);
        properties.put(FIELD_PHONE_NUMBER, phoneNumber);
        try {
            document.putProperties(properties);
            return true;
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot save document", e);
            return false;
        }

    }

    private boolean deleteContactInfo() {
        Document document = mDatabase.getDocument(CONTACT_INFO_DOCUMENT_ID);
        try {
            document.delete();
            return true;
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot delete document", e);
            return false;
        }
    }

    private void saveAvatar() {
        Document doc = mDatabase.getDocument(CONTACT_INFO_DOCUMENT_ID);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Bitmap bitmap = ((BitmapDrawable) mImgAvatar.getDrawable()).getBitmap();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

        try {
            UnsavedRevision newRev = doc.getCurrentRevision().createRevision();
            newRev.setAttachment("avatar.jpg", "image/jpeg", bs);
            newRev.save();
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot save attachment", e);
        }
    }

    private Bitmap getAvatar() {
        Document doc = mDatabase.getDocument(CONTACT_INFO_DOCUMENT_ID);
        Revision rev = doc.getCurrentRevision();
        Attachment att = rev.getAttachment("avatar.jpg");
        if (att != null) {
            try {
                InputStream is = att.getContent();
                BufferedInputStream bif = new BufferedInputStream(is);
                return BitmapFactory.decodeStream(bif);
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "Cannot load attachment", e);
            }
            return null;
        }
        return null;
    }

}
