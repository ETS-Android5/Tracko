
package com.kaavya.hypertrack.json;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class DeviceInfo {

    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("os_name")
    @Expose
    private String osName;
    @SerializedName("device_brand")
    @Expose
    private String deviceBrand;
    @SerializedName("sdk_version")
    @Expose
    private String sdkVersion;
    @SerializedName("device_model")
    @Expose
    private String deviceModel;
    @SerializedName("network_operator")
    @Expose
    private String networkOperator;
    @SerializedName("name")
    @Expose
    private Object name;
    @SerializedName("os_version")
    @Expose
    private String osVersion;
    @SerializedName("os_hardware_identifier")
    @Expose
    private String osHardwareIdentifier;

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public void setDeviceBrand(String deviceBrand) {
        this.deviceBrand = deviceBrand;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getNetworkOperator() {
        return networkOperator;
    }

    public void setNetworkOperator(String networkOperator) {
        this.networkOperator = networkOperator;
    }

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsHardwareIdentifier() {
        return osHardwareIdentifier;
    }

    public void setOsHardwareIdentifier(String osHardwareIdentifier) {
        this.osHardwareIdentifier = osHardwareIdentifier;
    }

}
