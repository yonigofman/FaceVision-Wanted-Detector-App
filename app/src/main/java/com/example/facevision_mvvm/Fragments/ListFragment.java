package com.example.facevision_mvvm.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.facevision_mvvm.Models.Wanted;
import com.example.facevision_mvvm.R;
import com.example.facevision_mvvm.Utils.WantedAdapter;
import com.example.facevision_mvvm.ViewModels.WantedViewModel;
import com.google.firebase.database.*;

import java.util.ArrayList;


public class ListFragment extends Fragment{


    WantedViewModel  wantedViewModel;
    DatabaseReference wantedRef;
    ListView listView;
    WantedAdapter allWantedAdapter;





    Button button;



    public ListFragment() {
        // Required empty public constructor
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        wantedViewModel = new ViewModelProvider(this).get(WantedViewModel.class);
       // wantedViewModel.init();

        wantedViewModel.getWantedList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Wanted>>() {
            @Override
            public void onChanged(ArrayList<Wanted> wanteds) {

                allWantedAdapter.notifyDataSetChanged();
            }
        });


        allWantedAdapter = new WantedAdapter(getContext(),0,0, wantedViewModel.getWantedList().getValue());
        listView.setAdapter(allWantedAdapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view =  inflater.inflate(R.layout.fragment_wanted_list, container, false);

        wantedRef = FirebaseDatabase.getInstance().getReference("Wanted/");
        listView = view.findViewById(R.id.lv);



        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                Wanted wanted = (Wanted) adapterView.getItemAtPosition(i);

                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getContext());// מופע של בילדר

                builder.setTitle("Delete Wanted");

                builder.setMessage("This will delete the wanted from the list and from the database");

                builder.setCancelable(true);
                builder.setIcon(R.drawable.ic_person_remove);

                builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        wantedRef = FirebaseDatabase.getInstance().getReference("Wanted/" + wanted.getKey());
                        wantedRef.removeValue();

                    }
                });

                builder.setNegativeButton("No", null);

                AlertDialog dialog = builder.create();// נפעיל את הבילדר ונחזיר רפרנס ל דיאלוג

                dialog.show();






                return true;
            }
        });








        return view;
    }






}