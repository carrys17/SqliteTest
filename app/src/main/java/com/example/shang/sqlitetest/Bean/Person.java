package com.example.shang.sqlitetest.Bean;

/**
 * Created by shang on 2017/6/29.
 */

public class Person {
    private int _id;
    private String name;
    private int age;

    public Person(int _id, String name, int age) {
        this._id = _id;
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "_id="+this._id+",name="+this.name+",age="+this.age;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
