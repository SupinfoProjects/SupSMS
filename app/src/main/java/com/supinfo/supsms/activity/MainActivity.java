package com.supinfo.supsms.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.supinfo.supsms.R;
import com.supinfo.supsms.entity.Contact;
import com.supinfo.supsms.response.Response;
import com.supinfo.supsms.tools.Properties;
import com.supinfo.supsms.tools.RequestSender;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    private BackupContactsTask mBackupContactsTask = null;
    private BackupMessagesTask mBackupMessagesTask = null;

    // UI references.
    private View mProgressView;
    private View mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkUser();

        mProgressView = findViewById(R.id.menu_progress);
        mMainView = findViewById(R.id.menu);

        // About
        Button aboutButton = (Button) findViewById(R.id.about);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAboutActivity();
            }
        });

        // Contacts
        Button contactsButton = (Button) findViewById(R.id.contacts);
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupContacts();
            }
        });

        // Messages
        Button messagesButton = (Button) findViewById(R.id.messages);
        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupMessages();
            }
        });

        // Logout
        Button logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void openAboutActivity() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    private void backupContacts() {
        showProgress(true);
        mBackupContactsTask = new BackupContactsTask();
        mBackupContactsTask.execute((Void) null);
    }

    private void backupMessages() {
        showProgress(true);
        mBackupMessagesTask = new BackupMessagesTask();
        mBackupMessagesTask.execute((Void) null);
    }

    private void logout() {
        Properties.getInstance().setUser(null); // remove the user
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public class BackupContactsTask extends AsyncTask<Void, Void, Boolean> {

        private static final String ACTION = "backupcontacts";
        private int savedContacts = 0;

        @Override
        protected Boolean doInBackground(Void... params) {

            Cursor contactsCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            if (contactsCursor.getCount() > 0) {
                List<Contact> contacts = new ArrayList<>();

                int idIndex         = contactsCursor.getColumnIndex(ContactsContract.Contacts._ID);
                int nameIndex       = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int hasNumberIndex  = contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

                // for each contact
                while (contactsCursor.moveToNext()) {
                    Contact contact = new Contact();
                    contact.setName(contactsCursor.getString(nameIndex));

                    // if contact has phone numbers
                    if (Integer.parseInt(contactsCursor.getString(hasNumberIndex)) > 0) {
                        List<String> numbers = new ArrayList<>();

                        Cursor numbersCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[] { contactsCursor.getString(idIndex) },
                            null
                        );

                        int numberIndex = numbersCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                        // for each phone number
                        while (numbersCursor.moveToNext()) {
                            numbers.add(numbersCursor.getString(numberIndex));
                        }

                        numbersCursor.close();
                        contact.setNumbers(numbers);
                    }

                    // for each email
                    List<String> emails = new ArrayList<>();

                    Cursor emailCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[] { contactsCursor.getString(idIndex) },
                        null
                    );

                    int emailIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

                    while (emailCursor.moveToNext()) {
                        emails.add(emailCursor.getString(emailIndex));
                    }

                    emailCursor.close();
                    contact.setEmails(emails);

                    // add the contact
                    contacts.add(contact);
                }

                contactsCursor.close();

                // call the API
                List<NameValuePair> parameters = new ArrayList<>(4);
                Gson gson = new Gson();

                parameters.add(new BasicNameValuePair("action", ACTION));
                parameters.add(new BasicNameValuePair("login", Properties.getInstance().getUser().getUsername()));
                parameters.add(new BasicNameValuePair("password", Properties.getInstance().getUser().getPassword()));
                parameters.add(new BasicNameValuePair("contacts", gson.toJson(contacts)));

                Log.d("debug", gson.toJson(contacts));
                Response result = RequestSender.sendRequest(parameters, Response.class);

                if (result.isSuccess()) {
                    savedContacts = contacts.size();
                }

                return result.isSuccess();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mBackupContactsTask = null;
            showProgress(false);

            if (success) {
                String message;

                switch (savedContacts) {
                    case 0:
                        message = getString(R.string.backup_contacts_empty);
                        break;

                    case 1:
                        message = getString(R.string.backup_contacts_one);
                        break;

                    default:
                        message = savedContacts + getString(R.string.backup_contacts_many);
                }

                Toast.makeText(
                    getApplicationContext(),
                    message,
                    Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.backup_error),
                    Toast.LENGTH_LONG
                ).show();
            }
        }

        @Override
        protected void onCancelled() {
            mBackupContactsTask = null;
            showProgress(false);
        }
    }

    public class BackupMessagesTask extends AsyncTask<Void, Void, Boolean> {

        private static final String ACTION = "backupsms";
        private int savedMessages = 0;

        @Override
        protected Boolean doInBackground(Void... params) {
            List<String> boxes = new ArrayList<>();
            boxes.add("inbox");
            boxes.add("sent");

            boolean success = true;

            for (String box : boxes) {
                List<Map<String, String>> messages = new ArrayList<>();

                Cursor cursor = getContentResolver().query(
                    Uri.parse("content://sms/" + box), null, null, null, null
                );

                while (cursor.moveToNext()) {
                    Map<String, String> message = new HashMap<>();

                    for (int id = 0; id < cursor.getColumnCount(); id++) {
                        message.put(cursor.getColumnName(id), cursor.getString(id));
                    }

                    messages.add(message);
                }

                if (!messages.isEmpty()) {
                    // call the API
                    List<NameValuePair> parameters = new ArrayList<>(5);
                    Gson gson = new Gson();

                    parameters.add(new BasicNameValuePair("action", ACTION));
                    parameters.add(new BasicNameValuePair("login", Properties.getInstance().getUser().getUsername()));
                    parameters.add(new BasicNameValuePair("password", Properties.getInstance().getUser().getPassword()));
                    parameters.add(new BasicNameValuePair("box", box));
                    parameters.add(new BasicNameValuePair("sms", gson.toJson(messages)));

                    Log.d("debug", gson.toJson(messages));
                    Response result = RequestSender.sendRequest(parameters, Response.class);

                    if (result.isSuccess()) {
                        savedMessages += messages.size();
                    } else {
                        success = false;
                    }
                }
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mBackupMessagesTask = null;
            showProgress(false);

            if (success) {
                String message;

                switch (savedMessages) {
                    case 0:
                        message = getString(R.string.backup_messages_empty);
                        break;

                    case 1:
                        message = getString(R.string.backup_messages_one);
                        break;

                    default:
                        message = savedMessages + getString(R.string.backup_messages_many);
                }

                Toast.makeText(
                    getApplicationContext(),
                    message,
                    Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.backup_error),
                    Toast.LENGTH_LONG
                ).show();
            }
        }

        @Override
        protected void onCancelled() {
            mBackupMessagesTask = null;
            showProgress(false);
        }
    }
}
