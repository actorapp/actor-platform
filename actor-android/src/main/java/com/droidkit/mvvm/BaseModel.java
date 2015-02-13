package com.droidkit.mvvm;

/**
 * Created by ex3ndr on 19.09.14.
 */
public abstract class BaseModel {

    private String name;

    public BaseModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
