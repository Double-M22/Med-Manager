package com.cyclon.com.med_manager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cyclon.com.med_manager.Constants.DataConstants;
import com.cyclon.com.med_manager.Data.DatabaseHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * The main activity. This activity only shows up when user has not signed in using the google sign option.
 *
 * When the user is signed in the activity is not called.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private GoogleSignInClient mGoogleSignInClient;
    private static final int REQUEST_CODE_SIGN_IN = 0;

    private SharedPreferences sp;
    private DatabaseHelper dbHelper;

    private String name, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        dbHelper = new DatabaseHelper(this);

        TextView  welcome = findViewById(R.id.welcome_text);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);


        //  Creating a sign_in option. This one request for the user email.
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestProfile()
                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            startActivity(new Intent(this, MedicationView.class));
            this.finish();
        }else{
            signInButton.setVisibility(View.VISIBLE);
            welcome.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        //Request user to choose an account.
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            //Google sign in complete and request for email and profile can be gotten.
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            email = account.getEmail();
            name = account.getDisplayName();

            // Getting user profile image from the google account.
            Uri photoUrl = account.getPhotoUrl();
            String url = ""+photoUrl;
            new DownloadImageTask().execute(url);

            //Store email in sharedPreference for later use.
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(DataConstants.PROFILE_EMAIL, email);
            editor.apply();

            startActivity(new Intent(this, MedicationView.class));
            this.finish();

            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Profile", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        /**
         * Async task that downloads image from passed in uri.
         * The doInBackground downloads the image file and returns a bitmap of the downloaded image.
         * The onPost execute sorts this bitmap and stores it in the profile_table in the Medication Database.
         * Username and email are also stored.
         */
        @Override
        protected Bitmap doInBackground(String... uri) {
            String photoUrl = uri[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(photoUrl).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            byte[] image_in_byte;
            Bitmap image;
            if(result != null)
                image = result;
            else image = BitmapFactory.decodeResource(MainActivity.this.getResources(),
                    R.drawable.no_profile_picture);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, bos);
            image_in_byte = bos.toByteArray();

            dbHelper.addProfile(name, email, image_in_byte);
        }
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
