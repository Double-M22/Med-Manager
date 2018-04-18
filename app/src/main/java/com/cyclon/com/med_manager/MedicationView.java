package com.cyclon.com.med_manager;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyclon.com.med_manager.Adapters.MedicationViewAdapter;
import com.cyclon.com.med_manager.Data.DatabaseHelper;
import com.cyclon.com.med_manager.Data.MedicationData;
import com.cyclon.com.med_manager.Data.ProfileData;
import com.cyclon.com.med_manager.Service.ScheduleJob;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The Medication View activity displays the medications in the database to the user.
 * From her the user is able to swipe and delete after the end date of the medication.
 */
public class MedicationView extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        MedicationViewAdapter.MedicationItemClickListener{

    private DatabaseHelper dbHelper;

    private RecyclerView med_recycler_view;

    private ArrayList<MedicationData> medData = new ArrayList<>();

    private ActionBarDrawerToggle toggle;

    private GoogleSignInClient mGoogleSignInClient;

    private View nav_header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_view);

        dbHelper = new DatabaseHelper(this);

        //Declarations and initializations.

        FloatingActionButton add_medication = findViewById(R.id.add_medication);
        add_medication.setOnClickListener(this);

        //Sets the recyclerView, with a linear layout, such the recycler items are displayed in linear form.
        med_recycler_view = findViewById(R.id.medication_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        med_recycler_view.setLayoutManager(linearLayoutManager);
        med_recycler_view.setHasFixedSize(true);

        //Drawable layout for navigation view.
        DrawerLayout drawer_layout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Navigation View that contains the profile image and name as it header, and has the logout and profile item.
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        nav_header = navigationView.getHeaderView(0);

        //  Creating a sign_in option. This one request for the user email.
        //  This is used to logout the user.
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestProfile()
                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        //The swipe handler, tha handles swipe events when user wants to delete an item.
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete

                //[Hint] Use getTag (from the adapter code) to get the id of the swiped item
                // Retrieve the id of the task to delete
                int id = (int) viewHolder.itemView.getTag();
                dbHelper.removeMedication(Integer.toString(id));

                // COMPLETED (3) Restart the loader to re-query for all tasks after a deletion
                fetchMedicationData();

            }
        }).attachToRecyclerView(med_recycler_view);

        //This loads the user profile to the navigation header.
        loadNavigationItem();
    }

    private void loadNavigationItem() {
        Cursor cursor = dbHelper.getProfile();
        ProfileData data = null;
        while(cursor.moveToNext()) {
            data = new ProfileData(cursor.getString(1),
                    cursor.getString(2),
                    cursor.getBlob(3));
        }

        if(data != null) {
            TextView name, email;
            name = nav_header.findViewById(R.id.drawer_name);
            email = nav_header.findViewById(R.id.drawer_email);

            CircleImageView profile_image;
            profile_image = nav_header.findViewById(R.id.drawer_dpImage);

            name.setText(data.getName());
            email.setText(data.getEmail());
            byte[] image = data.getImage();

            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            Bitmap profile_dp = Bitmap.createScaledBitmap(bmp, 200, 200, false);
            profile_image.setImageBitmap(profile_dp);
        }else{
            Toast.makeText(this, "Data not found!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Signs the user out
     */
    private void signOut(){
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MedicationView.this, "Sign out complete!!", Toast.LENGTH_SHORT).show();
                        revokeAccess();
                    }
                });
    }

    /**
     * Revokes access to account.
     */
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MedicationView.this, "Account revoked!!", Toast.LENGTH_SHORT).show();
                        ScheduleJob.cancelAllJob(MedicationView.this);
                        startActivity(new Intent(MedicationView.this, MainActivity.class));
                        MedicationView.this.finish();
                    }
                });
    }

     /**
     * Handles togvgling of menu item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Handles navigation item click
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();
        switch (item_id){
            case R.id.log_out:
                signOut();
                break;
            case R.id.profile:
                startActivity(new Intent(MedicationView.this, ProfilePage.class));
                this.finish();
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //Tranfers user to the add medication activity
            case R.id.add_medication:
                startActivity(new Intent(this, AddMedication.class));
                this.finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMedicationData();
    }

    /**
     * This fetch medication data from database
     */
    private void fetchMedicationData(){
        Cursor cursor = dbHelper.getMedications();
        medData.clear();

        while(cursor.moveToNext()){
            MedicationData data = new MedicationData(cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5));
            medData.add(data);
        }
        notifyAdapterOfChange();
    }

    /**
     * Custom notifyOnSetDataChange the adapter that data has changed.
     */
    private void notifyAdapterOfChange(){
        MedicationViewAdapter medAdapter = new MedicationViewAdapter(medData, this);
        med_recycler_view.setAdapter(medAdapter);
    }

    @Override
    public void onMedicationItemClicked(int clickedIndex) {
        Toast.makeText(this, ""+clickedIndex, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
