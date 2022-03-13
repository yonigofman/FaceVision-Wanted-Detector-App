package com.example.facevision_mvvm.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.facevision_mvvm.Models.Wanted;
import com.example.facevision_mvvm.R;

import java.util.List;

public class WantedAdapter extends ArrayAdapter<Wanted> {

    Context context;
    List<Wanted> objects;


    public WantedAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Wanted> objects) {
        super(context, resource, textViewResourceId, objects);

        this.context = context;
        this.objects = objects;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.wanted_item, parent, false);

        TextView firstName = view.findViewById(R.id.txt_first_name);
        TextView lastName = view.findViewById(R.id.txt_last_mame);
        Wanted temp = objects.get(position);

        firstName.setText(temp.getFirstName());
        lastName.setText(temp.getLastName());


        return view;
    }



}
