package com.example.chatapp.FirebaseEvent;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

class FirebaseEventCouple{
    private DatabaseReference dbRef;
    private ValueEventListener evValue = null;
    private ChildEventListener evChild = null;

    FirebaseEventCouple(DatabaseReference dbRef){
        this.dbRef = dbRef;
    }

    public FirebaseEventCouple setEvValue(ValueEventListener evValue){
        this.evValue = evValue;
        return this;
    }

    public FirebaseEventCouple setEvChild(ChildEventListener evChild){
        this.evChild = evChild;
        return this;
    }

    public void detachEvent(){
        if (evValue == null){
            dbRef.removeEventListener(evChild);
        }
        else{
            dbRef.removeEventListener(evValue);
        }
    }
}
