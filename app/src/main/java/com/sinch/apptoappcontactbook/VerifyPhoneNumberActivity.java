package com.sinch.apptoappcontactbook;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.parse.ParseUser;


public class VerifyPhoneNumberActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);
    }

    public void sendVerificationRequest(View v) {
        EditText phoneNumberField = (EditText) findViewById(R.id.verifyPhoneNumberInput);
        String phoneNumber = phoneNumberField.getText().toString();

        //TODO verify phone number with Sinch SDK

        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put("phoneNumber", phoneNumber);
        currentUser.saveInBackground();

        Intent intent = new Intent(this, ListContactsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            ParseUser.logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
