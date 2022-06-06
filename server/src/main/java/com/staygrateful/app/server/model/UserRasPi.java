package com.staygrateful.app.server.model;


import java.io.Serializable;

public class UserRasPi implements Serializable {

    public String name;

    public int age;

    public String gender;

    public int tinggi;

    public UserRasPi() {
        super();
    }

    public UserRasPi(String name, int age, String gender, int tinggi) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.tinggi = tinggi;
    }

    //"{ 'name':'Aldi Nugroho', 'age':30, 'gender':'wanita', 'tinggi':170}"
}
