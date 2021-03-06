package com.example.nilss.mqttlabb3;


public class Lampobject {
    private String sat;
    private String bri;
    private String hue;
    private String onoff;
    private String id;
    private Boolean inRange;

    public Lampobject(String id, String sat, String bri, String hue, String onoff){
        this.id = id;
        this.sat = sat;
        this.bri = bri;
        this.hue = hue;
        this.onoff = onoff;
        setInRange(onoff);
    }


    public void setInRange(String onoff){
        if(onoff.equals("true")){
            inRange = true;
        }else{
            inRange = false;
        }
    }
    public String getSat() {
        return sat;
    }

    public void setInRange(Boolean inRange) {
        this.inRange = inRange;
    }

    public Boolean getInRange() {
        return inRange;
    }

    public void setSat(String sat) {
        this.sat = sat;
    }

    public String getBri() {
        return bri;
    }

    public void setBri(String bri) {
        this.bri = bri;
    }

    public String getHue() {
        return hue;
    }

    public void setHue(String hue) {
        this.hue = hue;
    }

    public String getOnoff() {
        return onoff;
    }

    public void setOnoff(String onoff) {
        this.onoff = onoff;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "{\"on\":"+onoff+", \"sat\":"+sat+", \"bri\":"+bri+",\"hue\":"+hue+"}*";
    }
}
