#App-to-App Calling Integrated With Your Contact Book

In this tutorial, I'll show you how to build an app-to-app calling app that only lets you call people in your contact book that have the app installed. This project uses Sinch for the phone number verification, and Parse for the user management.
     
Unlike some of my other tutorials that walk you through every bit of code you need to build the app, this tutorial will touch on the main concepts here, and let you dig through the code on our Github at your own pace. The finished app code can be found at [https://www.github.com/sinch/android-app-calling-contact-book](https://www.github.com/sinch/android-app-calling-contact-book)        
    
##Setup

Sign up for a Sinch account at [https://www.sinch.com/signup](https://www.sinch.com/signup) and download the verification SDK from [https://www.sinch.com/downloads](https://www.sinch.com/downloads)

Also sign up for a Parse account and follow the steps to add Parse to your Android app here [https://parse.com/apps/quickstart#parse_data](https://parse.com/apps/quickstart#parse_data)

##Signup and Login

When users sign up, you want to direct them to the activity to verify their phone number. When they log in, you want to see first see if they still need to verify their phone number, and then either prompt them to verify their phone number or send them to a list of their contacts that have the app installed (this is a huge block of code, but bear with me!):
    
    public class LoginActivity extends Activity {
    
        private Button signUpButton;
        private Button loginButton;
        private EditText usernameField;
        private EditText passwordField;
        private String username;
        private String password;
        private Intent notVerifiedIntent;
        private Intent verifiedIntent;
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
    
            notVerifiedIntent = new Intent(this, VerifyPhoneNumberActivity.class);
            verifiedIntent = new Intent(this, ListContactsActivity.class);
    
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.get("phoneNumber") != null) {
                    startActivity(verifiedIntent);
                } else {
                    startActivity(notVerifiedIntent);
                }
            }
    
            setContentView(R.layout.activity_login);
    
            loginButton = (Button) findViewById(R.id.loginButton);
            signUpButton = (Button) findViewById(R.id.signupButton);
            usernameField = (EditText) findViewById(R.id.loginUsername);
            passwordField = (EditText) findViewById(R.id.loginPassword);
    
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    username = usernameField.getText().toString();
                    password = passwordField.getText().toString();
    
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        public void done(ParseUser user, com.parse.ParseException e) {
                            if (user != null) {
                                if (user.get("phoneNumber") != null) {
                                    startActivity(verifiedIntent);
                                } else {
                                    startActivity(notVerifiedIntent);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                    "Wrong username/password combo",
                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
    
            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
    
                    username = usernameField.getText().toString();
                    password = passwordField.getText().toString();
    
                    ParseUser user = new ParseUser();
                    user.setUsername(username);
                    user.setPassword(password);
    
                    user.signUpInBackground(new SignUpCallback() {
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                ParseUser.becomeInBackground(ParseUser.getCurrentUser().getSessionToken());
                                startActivity(notVerifiedIntent);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                    "There was an error signing up."
                                    , Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
        }
    
    }


##Verify Phone Number



##List Contacts

This is the fun part! First, create a list that you can populate later:

    final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, android.R.id.text1);

    final ListView contactsListView = (ListView) findViewById(R.id.contactList);
    contactsListView.setAdapter(adapter);
    
Here is how you will get a list of all phone numbers in the contact book (It also gets rid of all spaces, parentheses, and plus signs in an attempt to normalize the numbers):

    final List<String> contactBookNumbers = new ArrayList<>();

    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
    while (phones.moveToNext()) {
        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        phoneNumber = phoneNumber.replaceAll("[\\s\\+\\(\\)-]", "");
        contactBookNumbers.add(phoneNumber);
    }
    phones.close();
    
Then, go through all of the ParseUsers with a verified phone number, and if a user's phone number is in the contact book list of numbers, add their username to the ListView:

    ParseQuery<ParseUser> query = ParseUser.getQuery();
    query.whereExists("phoneNumber");
    query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
    query.findInBackground(new FindCallback<ParseUser>() {
        public void done(List<ParseUser> objects, ParseException e) {
            if (e == null) {
                for(int i=0; i<objects.size(); i++) {
                    if (contactBookNumbers.contains(objects.get(i).get("phoneNumber").toString())) {
                        adapter.add(objects.get(i).getUsername());
                    }
                }
            } else {
                Log.d("ParseException", e.getMessage());
            }
        }
    });
    
**Note:** I noticed that in my contact book, not all phone numbers are prefixed with the country code. However, to verify a phone number with Sinch, you must include the country code. So, for example someone in your contact book with the phone number **(555) 555-5555** who has the app installed, won't show up in your list of contacts, because they are verified with the number **+1 (555) 555-5555**. Unless you're dealing with a specific country (like US only), I haven't found an easy way to tell which country a phone number belongs to in the contact book, mostly because it's unclear wether the phone number has a country code or not to start with. Any suggestions greatly appreciated! Comment below if you have a solution.
