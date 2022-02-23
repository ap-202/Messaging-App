package com.example.messages;

import android.graphics.Color;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class Contact {

    private String firstName;
    private String lastName;
    private String name;
    private Message lastmsg;
    private Timestamp lasttime;
    private int color;

    public Contact(String n){
        name=n;
    }
    public Contact(String n, Message l, Timestamp t){
        name=n;
        lastmsg=l;
        lasttime=t;
    }

    public int getColor() {
        return color;
    }

    public Contact(String n, Message l, Timestamp t, int c){
        name=n;
        lastmsg=l;
        lasttime=t;
        FirebaseFirestore.getInstance().collection(n).document("picture").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.get("option")!=null)
                    color=Math.toIntExact((long)documentSnapshot.get("option"));
            }
        });


    }
    public Contact(String n, Message l){
        name=n;
        lastmsg=l;
    }
    public void setName(String f, String l){
        firstName=f;
        lastName=l;
    }
public Contact(){}
    public Message getLastmsg() {
        return lastmsg;
    }

    public String getName() {
        return name;
    }

    public Timestamp getLasttime() {
        return lasttime;
    }
}
