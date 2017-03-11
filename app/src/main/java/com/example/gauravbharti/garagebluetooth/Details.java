package com.example.gauravbharti.garagebluetooth;

/**
 * Created by gauravbharti on 11/03/17.
 */

public class Details
{   String name;
    String address;
    String type;
    String password;
    public Details(String name,String address,String type,String password)
    {   this.name=name;
        this.address=address;
        this.type=type;
        this.password=password;
    }
    public String getName()
    {   return name;

    }
    public String getAddress()
    {
        return address;
    }
    public String getType()
    {
        return type;
    }
    public String getPassword()
    {
        return password;
    }
    public void setName(String name)
    {
        this.name=name;
    }
    public void setAddress(String address)
    {
        this.address=address;
    }
    public void setType(String type)
    {   this.type=type;
    }
    public void setPassword(String password)
    {
        this.password=password;
    }
}
