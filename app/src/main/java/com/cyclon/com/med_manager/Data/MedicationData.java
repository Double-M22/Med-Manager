package com.cyclon.com.med_manager.Data;

public class MedicationData {
    private int id;
    private String name, description, intervals, start_date, end_date;

    public MedicationData(int id, String name, String description, String intervals, String start_date, String end_date) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.intervals = intervals;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIntervals() {
        return intervals;
    }

    public String getStart_date() {
        return start_date;
    }

    public String getEnd_date() {
        return end_date;
    }
}
