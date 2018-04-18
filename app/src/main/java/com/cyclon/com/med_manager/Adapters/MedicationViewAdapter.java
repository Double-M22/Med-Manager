package com.cyclon.com.med_manager.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cyclon.com.med_manager.Data.MedicationData;
import com.cyclon.com.med_manager.R;

import java.util.ArrayList;

public class MedicationViewAdapter extends RecyclerView.Adapter<MedicationViewAdapter.MedicationViewHolder>{

    private int back_index, item_count;
    private int back_colors[] = new int[]{R.drawable.blue_back, R.drawable.red_back, R.drawable.purple_back,
                                            R.drawable.yellow_back, R.drawable.green_back};

    private ArrayList<MedicationData> medData = new ArrayList<>();

    //Handles adapter onClick events.
    private final MedicationItemClickListener medicationItemClickListener;
    public interface MedicationItemClickListener{
        void onMedicationItemClicked(int clickedIndex);
    }

    //Constructor for medication the takes in click context and the data to be loaded.
    public MedicationViewAdapter(ArrayList<MedicationData> medData, MedicationItemClickListener medicationItemClickListener) {
        this.medData = medData;
        this.medicationItemClickListener = medicationItemClickListener;
        this.back_index = 0;
        item_count = medData.size();
    }

    //Creates and inflates the medication View.
    @Override
    public MedicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.medication_view;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        MedicationViewHolder viewHolder = new MedicationViewHolder(view);

        viewHolder.itemView.setBackgroundResource(back_colors[back_index]);

        if(back_index < 4)
            back_index++;
        else back_index = 0;

        return viewHolder;
    }

    //Binds view holder to view
    @Override
    public void onBindViewHolder(MedicationViewHolder holder, int position) {
        String name, start, end, interval;
        MedicationData data = medData.get(position);
        name = data.getName();
        interval = "Interval: "+data.getIntervals();
        start = "Start: "+data.getStart_date();
        end = "End: "+data.getEnd_date();

        int id = data.getId();
        holder.itemView.setTag(id);

        holder.bind(name, interval, start, end);
    }

    //Returns the number of items in the recycler view
    @Override
    public int getItemCount() {
        return item_count;
    }

    //The view holder class that holds View of the recycler view.
    class MedicationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView med_name, med_interval, med_start, med_end;

        MedicationViewHolder(View itemView) {
            super(itemView);

            med_name = itemView.findViewById(R.id.view_medication_name);
            med_interval = itemView.findViewById(R.id.view_intervals);
            med_start = itemView.findViewById(R.id.view_start);
            med_end = itemView.findViewById(R.id.view_end);

            itemView.setOnClickListener(this);
        }

        void bind(String name, String intervals, String start, String end){
            med_name.setText(name);
            med_interval.setText(intervals);
            med_start.setText(start);
            med_end.setText(end);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = (int)itemView.getTag();
            medicationItemClickListener.onMedicationItemClicked(clickedPosition);
        }
    }
}
