
package com.kaavya.hypertrack.json;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class DeviceList {

    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("pagination_token")
    @Expose
    private Object paginationToken;
    @SerializedName("links")
    @Expose
    private Links links;

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public Object getPaginationToken() {
        return paginationToken;
    }

    public void setPaginationToken(Object paginationToken) {
        this.paginationToken = paginationToken;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

}
