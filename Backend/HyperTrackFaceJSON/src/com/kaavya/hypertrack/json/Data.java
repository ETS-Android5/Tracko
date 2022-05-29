
package com.kaavya.hypertrack.json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Data {

    @SerializedName("recorded_at")
    @Expose
    private String recordedAt;
    @SerializedName("activity")
    @Expose
    private String activity;
    @SerializedName("reason")
    @Expose
    private String reason;
    @SerializedName("inactive_reason")
    @Expose
    private InactiveReason inactiveReason;

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public InactiveReason getInactiveReason() {
        return inactiveReason;
    }

    public void setInactiveReason(InactiveReason inactiveReason) {
        this.inactiveReason = inactiveReason;
    }

}
