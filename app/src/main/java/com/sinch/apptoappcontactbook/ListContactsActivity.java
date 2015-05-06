package com.sinch.apptoappcontactbook;

import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ListContactsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contacts);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, android.R.id.text1);

        final ListView contactsListView = (ListView) findViewById(R.id.contactList);
        contactsListView.setAdapter(adapter);

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String  itemValue = (String) contactsListView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), itemValue, Toast.LENGTH_LONG).show();
            }
        });

        final List<String> contactBookNumbers = new ArrayList<>();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext()) {
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumber = phoneNumber.replaceAll("[\\s\\+\\(\\)-]", "");
            contactBookNumbers.add(phoneNumber);
        }
        phones.close();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereExists("phoneNumber");
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    for(int i=0; i<objects.size(); i++) {
                        if (contactBookNumbers.contains(objects.get(i).get("phoneNumber").toString())) {
                            adapter.add(objects.get(i).getUsername());
                            System.out.println("Here: " + new Date());
                        }
                    }
                } else {
                    Log.d("ParseException", e.getMessage());
                }
            }
        });
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
