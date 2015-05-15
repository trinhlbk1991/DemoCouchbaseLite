package com.icetea09.com.democouchbaselite.models;

/**
 * Created by Trinh Le on 5/15/2015.
 */
public class ContactInfo {
    private String mFirstName;
    private String mLastName;
    private String mPhoneNumber;

    public ContactInfo(String mFirstName, String mLastName, String mPhoneNumber) {
        this.mFirstName = mFirstName;
        this.mLastName = mLastName;
        this.mPhoneNumber = mPhoneNumber;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }
}
