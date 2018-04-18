package com.cyclon.com.med_manager;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cyclon.com.med_manager.Constants.DataConstants;
import com.cyclon.com.med_manager.Data.DatabaseHelper;
import com.cyclon.com.med_manager.Service.ScheduleJob;
import com.cyclon.com.med_manager.Service.StartMedicationJobScheduler;
import com.cyclon.com.med_manager.Service.StopMedicationJobScheduler;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;

public class AddMedication extends AppCompatActivity {

    /**
     * The add medication Activity
     * This activity add the medication entered by the user.
     */
    private DatabaseHelper dbHelper;
    private EditText med_name, med_desc;
    private Spinner med_interval;
    private static Button med_start_date, med_end_date;
    private String interval;
    public static int date_picker_identifier;

    static long current, start, end;
    private int interval_in_sec;
    static String date, current_date_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        med_name = findViewById(R.id.medication_name);
        med_desc = findViewById(R.id.medication_description);
        med_interval = findViewById(R.id.medication_interval);
        med_start_date = findViewById(R.id.medication_start_date);
        med_end_date = findViewById(R.id.medication_end_date);

        //Set the interval spinner
        final ArrayList<String> intervals = new ArrayList<>();
        intervals.add("30min");
        intervals.add("1hr");
        for(int i=2; i<=24; i++){
            intervals.add(i+"hrs");
        }

        //Sets on item selected on the spinner.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervals);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        med_interval.setAdapter(adapter);
        med_interval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                interval = (String)med_interval.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // This gets the current date and time and fills the views as appropriate.
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        sortCurrentDateAndTime(year, month, day, hour, minute);

        //On click on the start and end date picker buttons.
        date_picker_identifier = 0;
        med_start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date_picker_identifier = 1;
                showDatePicker();
            }
        });

        med_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date_picker_identifier = 2;
                showDatePicker();
            }
        });

        dbHelper = new DatabaseHelper(this);
    }

    /**
     * Metthod for sorting the current time
     */
    private void sortCurrentDateAndTime(int ... values){
        String day, month, hour, minute;
        if(values[2]<10) day = "0"+values[2];
        else day = ""+values[2];

        if(values[1]<10) month = "0"+values[1];
        else month = ""+values[1];

        if(values[3]<10) hour = "0"+values[3];
        else hour = ""+values[3];

        if(values[4]<10) minute = "0"+values[4];
        else minute = ""+values[4];

        current_date_time = day+"-"+month+"-"+values[0] +" "+hour+":"+minute;

        med_start_date.setText(current_date_time);
        med_end_date.setText(current_date_time);

        current = convertTime(current_date_time);
    }

    /**
     * Converts date formats to get the time.
     */
    private long convertTime(String format_to_convert){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        try {
            Date date = simpleDateFormat.parse(format_to_convert);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * The add button that add medication if the filled are well filled.
     */
    public void add(View view) {
        String name, desc, start_date, end_date;
        name = med_name.getText().toString().trim();
        desc = med_desc.getText().toString().trim();
        start_date = med_start_date.getText().toString().trim();
        end_date = med_end_date.getText().toString().trim();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String job_tag = sp.getString(DataConstants.MEDICATION_TABLE_NAME, null);

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(desc)) {
            handleJobActivities(name, job_tag, start_date, end_date);
            if(dbHelper.addMedication(name, desc, interval, start_date, end_date)) {
                Toast.makeText(this, "Medication added!!", Toast.LENGTH_SHORT).show();
                handleJobActivities(name, job_tag, start_date, end_date);
            }
        }else Toast.makeText(this, "Please fill all field!!!", Toast.LENGTH_LONG).show();
    }

    /**
     * This schedules 3 jobs. On the start the main medication jon on its start date
     * The second the main medication that send notification that the user should take there medication.
     * And the last to end the main medication job on its end date
     */
    private void handleJobActivities(String name, String job_tag, String start_date, String end_date) {
        start = convertTime(start_date);
        end = convertTime(end_date);

        if(start >= current){
            if (end > start){
                if(job_tag != null){
                    String start_job_tag = job_tag+"_start_"+name;
                    String main_job_tag = job_tag+"_"+name;
                    String stop_job_tag = job_tag+"_stop_"+name;

                    start = start - current;
                    end = end - current;

                    int start_in_sec = (int)(start / 1000);
                    int end_in_sec = (int)(end / 1000);

                    String job_interval;
                    if (interval.contains("min")) {
                        job_interval = interval.substring(0, interval.indexOf('m'));
                        interval_in_sec = Integer.parseInt(job_interval) * 60;
                    } else {
                        job_interval = interval.substring(0, interval.indexOf('h'));
                        interval_in_sec = Integer.parseInt(job_interval) * 3600;
                    }

                    interval_in_sec = 10;
                    if(start_in_sec <= interval_in_sec){
                        ScheduleJob.scheduleMedicationreminder(this, interval_in_sec, main_job_tag);
                    }else {
                        start_in_sec = start_in_sec - interval_in_sec;
                        scheduleJobStarter(start_job_tag, main_job_tag, start_in_sec);
                    }
                    scheduleJobStopper(stop_job_tag, main_job_tag, end_in_sec);
                }

            }else Toast.makeText(this, "Invalid dates : End date should be grater than start date", Toast.LENGTH_LONG).show();

        }else Toast.makeText(this, "Invalid dates : Start date should be grater than or equal to the current date",
                Toast.LENGTH_LONG).show();
    }

    private void showDatePicker(){
        DialogFragment dateFragment = new MyDatePicker();
        dateFragment.show(getFragmentManager(), "Date");
    }

    /**
     * Schedules the start job service
     */
    private void scheduleJobStarter(String start_job_tag, String main_job_tag, int start_job_date){
        Bundle bundle = new Bundle();
        bundle.putString(DataConstants.MAIN_JOB_TAG, main_job_tag);
        bundle.putLong(DataConstants.MAIN_JOB_INTERVAL, interval_in_sec);

        Driver driver = new GooglePlayDriver(this);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job reminderJob = dispatcher.newJobBuilder()
                .setExtras(bundle)
                .setService(StartMedicationJobScheduler.class)
                .setTag(start_job_tag)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(start_job_date, start_job_date+5))
                .setReplaceCurrent(true)
                .build();
        dispatcher.schedule(reminderJob);
    }

    /**
     * Schedules the stop job service
     */
    private void scheduleJobStopper(String stop_job_tag, String main_job_tag, int end_job_date){
        Bundle bundle = new Bundle();
        bundle.putString(DataConstants.MAIN_JOB_TAG, main_job_tag);

        Driver driver = new GooglePlayDriver(this);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job reminderJob = dispatcher.newJobBuilder()
                .setExtras(bundle)
                .setService(StopMedicationJobScheduler.class)
                .setTag(stop_job_tag)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(false)
                .setTrigger(Trigger.executionWindow(end_job_date, end_job_date+5))
                .setReplaceCurrent(true)
                .build();
        dispatcher.schedule(reminderJob);
    }

    public static class MyDatePicker extends DialogFragment
            implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle saveInstanceState){

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            setDate(year, month+1, day);
            DialogFragment timePicker = new MyTimePicker();
            timePicker.show(getFragmentManager(), "Time");
        }

        public static String setDate(int year, int mon, int dy) {
            String day, month;

            if(dy<10) day = "0"+dy;
            else day = ""+dy;

            if(mon<10) month = "0"+mon;
            else month = ""+mon;

            date = day+"-"+month+"-"+year;

            return date;
        }
    }

    public static class MyTimePicker extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            setTime(hourOfDay, minute);
        }

        public static String setTime(int hrs, int min){
            String hour, minute;

            if(hrs<10) hour = "0"+hrs;
            else hour = ""+hrs;

            if(min<10) minute = "0"+min;
            else minute = ""+min;

            String time = hour+":"+minute;
            String set_date_time = date+" "+time;

            switch (date_picker_identifier){
                case 1:
                    med_start_date.setText(set_date_time);
                    break;
                case 2:
                    med_end_date.setText(set_date_time);
                    break;
            }

            return time;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddMedication.this, MedicationView.class));
        this.finish();
    }
}
