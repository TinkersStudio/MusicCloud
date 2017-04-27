package com.tinkersstudio.musiccloud.model;

/**
 * Created by anhnguyen on 2/11/17.
 */

public class Info {
    private String attribute;
    private String value;

    public Info (String attribute, String value){
        this.attribute = attribute;
        this.value = value;
    }

    public String getAttribute() { return this.attribute;}
    public void setAttribute(String attribute) {this.attribute = attribute;}

    public String getValue() { return this.value;}
    public void setValue(String value) {this.value = value;}
}
