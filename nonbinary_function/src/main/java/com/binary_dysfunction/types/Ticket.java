package com.binary_dysfunction.types;

public class Ticket {

    public String id;
    public String owner;
    public String eventName;
    public int count;
    public String location;
    public double price;
    public long date;
    public boolean available;
    public boolean registered;

    public Ticket(String id, String owner, String eventName, int count, String location, double price, long date, boolean available, boolean registered) {
        this.id = id;
        this.owner = owner;
        this.eventName = eventName;
        this.count = count;
        this.location = location;
        this.price = price;
        this.date = date;
        this.available = available;
        this.registered = registered;
    }
}
