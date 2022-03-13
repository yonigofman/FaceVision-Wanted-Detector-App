package com.example.facevision_mvvm.Repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.example.facevision_mvvm.Models.Wanted;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class Repo {

    static Repo instance;
    private final ArrayList<Wanted> wanteds = new ArrayList<>();
    private final MutableLiveData<ArrayList<Wanted>> wanted = new MutableLiveData<>();

    static final DatabaseReference wantedRef = FirebaseDatabase.getInstance().getReference("wanted");


    public static Repo getInstance() {
        if (instance == null) {
            instance = new Repo();
        }

        return instance;
    }


    public MutableLiveData<ArrayList<Wanted>> getWanteds() {

        if (wanteds.size() == 0) {
            retriveWanted();
        }

        wanted.setValue(wanteds);

        return wanted;

    }


    public void retriveWanted() {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Wanted");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wanted.getValue().clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    wanteds.add(ds.getValue(Wanted.class));
                }

                wanted.setValue(null);
                wanted.postValue(wanteds);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}


