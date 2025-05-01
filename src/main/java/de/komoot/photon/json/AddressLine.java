package de.komoot.photon.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddressLine {

    @JsonProperty("place_id")
    public long placeId;

    @JsonProperty("rank_address")
    public int rankAddress;

    @JsonProperty("fromarea")
    public boolean fromArea;

    @JsonProperty("isaddress")
    public boolean isAddress;
}
