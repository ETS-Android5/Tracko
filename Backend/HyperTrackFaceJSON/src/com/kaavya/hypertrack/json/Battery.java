
package com.kaavya.hypertrack.json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Battery {

    @SerializedName("battery_percent")
    @Expose
    private long batteryPercent;
    @SerializedName("recorded_at")
    @Expose
    private String recordedAt;

    public long getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(long batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }

}
