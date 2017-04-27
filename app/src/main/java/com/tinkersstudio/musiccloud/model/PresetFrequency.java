package com.tinkersstudio.musiccloud.model;

/**
 * Created by anhnguyen on 4/26/17.
 */

public class PresetFrequency {
    private String name;
    private int band1, band2, band3, band4, band5;

    public PresetFrequency(String name, int band1, int band2, int band3, int band4, int band5 ) {
        this.name = name;
        this.band1 = band1;
        this.band2 = band2;
        this.band3 = band3;
        this.band4 = band4;
        this.band5 = band5;
    }

    public String getName(){ return name; }

    public int getBand1() {
        return band1;
    }

    public int getBand2() {
        return band2;
    }

    public int getBand3() {
        return band3;
    }

    public int getBand4() {
        return band4;
    }

    public int getBand5() {
        return band5;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBand1(int band1) {
        this.band1 = band1;
    }

    public void setBand2(int band2) {
        this.band2 = band2;
    }

    public void setBand3(int band3) {
        this.band3 = band3;
    }

    public void setBand4(int band4) {
        this.band4 = band4;
    }

    public void setBand5(int band5) {
        this.band5 = band5;
    }
}
