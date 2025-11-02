package com.example.childcare.models;

import java.util.List;

public class StaffSchedule {
    private int staffId;
    private String date;
    private List<String> busyTimeSlots;

    public StaffSchedule(int staffId, String date, List<String> busyTimeSlots) {
        this.staffId = staffId;
        this.date = date;
        this.busyTimeSlots = busyTimeSlots;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getDate() {
        return date;
    }

    public List<String> getBusyTimeSlots() {
        return busyTimeSlots;
    }

    public boolean isTimeSlotBusy(String time) {
        if (busyTimeSlots == null) return false;

        for (String busySlot : busyTimeSlots) {
            if (isTimeInRange(time, busySlot)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTimeInRange(String checkTime, String busySlot) {
        // busySlot format: "09:00-10:30"
        String[] parts = busySlot.split("-");
        if (parts.length != 2) return false;

        String startTime = parts[0].trim();
        String endTime = parts[1].trim();

        return checkTime.compareTo(startTime) >= 0 && checkTime.compareTo(endTime) < 0;
    }
}