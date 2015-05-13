package com.sinch.apptoappcontactbook;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseUser;
import com.sinch.verification.Config;
import com.sinch.verification.SinchVerification;
import com.sinch.verification.Verification;
import com.sinch.verification.VerificationListener;


public class VerifyPhoneNumberActivity extends ActionBarActivity {

    private final String APPLICATION_KEY = "YOUR_APPLICATION_KEY";
    private String phoneNumber;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);
    }

    public void sendVerificationRequest(View v) {
        EditText phoneNumberField = (EditText) findViewById(R.id.verifyPhoneNumberInput);
        phoneNumber = phoneNumberField.getText().toString();
        createVerification(phoneNumber);
        Toast.makeText(context, "Verifying phone number, please wait!", Toast.LENGTH_SHORT).show();
    }

    void createVerification(String phoneNumber) {
        Config config = SinchVerification
            .config()
            .applicationKey(APPLICATION_KEY)
            .context(context)
            .build();
        VerificationListener listener = new MyVerificationListener();
        Verification verification = SinchVerification.createSmsVerification(config, phoneNumber, listener);
        verification.initiate();
    }

    class MyVerificationListener implements VerificationListener {
        @Override
        public void onInitiated() {}

        @Override
        public void onInitiationFailed(Exception exception) {}

        @Override
        public void onVerified() {
            Toast.makeText(context, "Your phone number has been verified.", Toast.LENGTH_LONG).show();

            ParseUser currentUser = ParseUser.getCurrentUser();
            currentUser.put("phoneNumber", phoneNumber);
            currentUser.saveInBackground();

            Intent intent = new Intent(context, ListContactsActivity.class);
            startActivity(intent);
        }

        @Override
        public void onVerificationFailed(Exception exception) {
            Toast.makeText(context, "Verification failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
