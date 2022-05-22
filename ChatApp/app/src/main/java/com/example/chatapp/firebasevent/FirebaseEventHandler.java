package com.example.chatapp.firebasevent;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseEventHandler {
    private static List<FirebaseEventCouple> valueEventsList = new ArrayList<>();
    private static List<FirebaseEventCouple> childEventsList = new ArrayList<>();

    //add a new child event to the handler's list
    public static void addChildEvent(DatabaseReference dbRef, ChildEventListener ev){
        childEventsList.add(new FirebaseEventCouple(dbRef).setEvChild(ev));
        dbRef.addChildEventListener(ev);
    }

    //add a new value event to the handler's list
    public static void addValueEvent(DatabaseReference dbRef, ValueEventListener ev){
        childEventsList.add(new FirebaseEventCouple(dbRef).setEvValue(ev));
        dbRef.addValueEventListener(ev);
    }

    //detach all the active asynchronous firebase events
    public static void detachAll(){
        for (FirebaseEventCouple firebaseEventCouple: childEventsList){
            firebaseEventCouple.detachEvent();
        }
        for(FirebaseEventCouple firebaseEventCouple: valueEventsList){
            firebaseEventCouple.detachEvent();
        }
        valueEventsList.clear();
        childEventsList.clear();
    }

}
