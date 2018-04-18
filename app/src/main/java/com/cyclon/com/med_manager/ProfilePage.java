package com.cyclon.com.med_manager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cyclon.com.med_manager.Data.DatabaseHelper;
import com.cyclon.com.med_manager.Data.ProfileData;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The Profile activity where user info is seen and editing can be done to it
 */
public class ProfilePage extends AppCompatActivity implements View.OnClickListener{

    private FloatingActionButton edit, edit_dp;
    private Button ok, cancel;
    private CircleImageView profile_image;
    private TextView name, profile_email;
    private EditText input_name;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECT_PHOTO = 2;
    private String old_name, new_name;
    private Bitmap mResultsBitmap;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        edit = findViewById(R.id.edit_btn);
        edit_dp = findViewById(R.id.edit_dp);
        ok = findViewById(R.id.edit_ok);
        profile_email = findViewById(R.id.profile_email);
        cancel = findViewById(R.id.edit_cancel);
        profile_image = findViewById(R.id.profile_image);
        name = findViewById(R.id.profile_name);
        input_name = findViewById(R.id.input_name);

        edit_dp.setOnClickListener(this);
        edit.setOnClickListener(this);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

        input_name.setVisibility(View.GONE);
        ok.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        edit_dp.setVisibility(View.GONE);
        name.setEnabled(false);

        dbHelper = new DatabaseHelper(this);
        loadData();
    }

    //Loads data from data base
    private void loadData(){
        Cursor cursor = dbHelper.getProfile();
        ProfileData data = null;
        while(cursor.moveToNext()) {
            data = new ProfileData(cursor.getString(1),
                    cursor.getString(2),
                    cursor.getBlob(3));
        }

        if(data != null) {
            name.setText(data.getName());
            profile_email.setText(data.getEmail());
            byte[] image = data.getImage();

            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            profile_image.setImageBitmap(bmp);
        }else{
            Toast.makeText(this, "Data not found!", Toast.LENGTH_SHORT).show();
        }
    }

    //Edit floating button toggles visibilities
    public void edit() {
        ok.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        input_name.setVisibility(View.VISIBLE);
        edit_dp.setVisibility(View.VISIBLE);
        edit.setVisibility(View.GONE);
        name.setVisibility(View.GONE);
        old_name = name.getText().toString();
        input_name.setText(old_name);
        input_name.setEnabled(true);
        profile_image.setEnabled(true);

        BitmapDrawable drawable = (BitmapDrawable) profile_image.getDrawable();
        mResultsBitmap = drawable.getBitmap();
    }

    //Ok Stores changes
    public void ok() {
        new_name = input_name.getText().toString();
        name.setText(new_name);
        ok.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        input_name.setVisibility(View.GONE);
        edit_dp.setVisibility(View.GONE);
        edit.setVisibility(View.VISIBLE);
        name.setVisibility(View.VISIBLE);
        profile_image.setEnabled(false);

        BitmapDrawable drawable = (BitmapDrawable) profile_image.getDrawable();
        mResultsBitmap = drawable.getBitmap();
        updateDatabase();
    }

    //Discards changes
    private void cancel() {
        name.setText(old_name);
        ok.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        input_name.setVisibility(View.GONE);
        edit_dp.setVisibility(View.GONE);
        edit.setVisibility(View.VISIBLE);
        name.setVisibility(View.VISIBLE);
        profile_image.setEnabled(false);

        profile_image.setImageBitmap(mResultsBitmap);
    }

    //Updates database
    private void updateDatabase() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mResultsBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] image_in_byte = bos.toByteArray();
        String email = profile_email.getText().toString();
        dbHelper.updateProfile(old_name, new_name, email, image_in_byte);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.edit_btn:
                edit();
                break;
            case R.id.edit_ok:
                ok();
                break;
            case R.id.edit_cancel:
                cancel();
                break;
            case R.id.edit_dp:
                startDialog();
                break;
        }
    }

    private void startDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton("Take a Photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       launchCamera();
                    }
                })
                .setPositiveButton("Choose from Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        chooseFromGallery();
                    }
                })
                .setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Launches camera for user to taka photo by calling an explicit intent and catching the result on the onActivity result.
    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //Prompts user to choose an already existing image from the gallery and catching the result on the onActivity result.
    private void chooseFromGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profile_image.setImageBitmap(imageBitmap);
        } else if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String photoUrl = cursor.getString(columnIndex);
                cursor.close();

                Bitmap bitmap = BitmapFactory.decodeFile(photoUrl);
                profile_image.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MedicationView.class));
        this.finish();
    }
}
