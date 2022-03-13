package com.example.facevision_mvvm.Fragments.FaceFragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.facevision_mvvm.Fragments.FaceFragments.Utils.Communicator;
import com.example.facevision_mvvm.Models.Location;
import com.example.facevision_mvvm.Models.Wanted;
import com.example.facevision_mvvm.R;
import com.example.facevision_mvvm.ViewModels.FaceRecViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import org.json.JSONObject;


public class AddWantedFragment extends Fragment {





    FirebaseDatabase firebaseDatabase;
    DatabaseReference wantedRef;


//    EditText etFirstName;
//    EditText etLastName;


    ImageView imageView;
    Button SaveWantedBtn;

    Wanted wanted;

    TextInputEditText etFirstName , etLastName;


    FaceRecViewModel similarityClassifierViewModel;

    String reco = "";

    public AddWantedFragment() {
        // Required empty public constructor
    }


    JSONObject jsonObject = new JSONObject();
    Object object;




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        similarityClassifierViewModel = new ViewModelProvider(getActivity()).get(FaceRecViewModel.class);
        similarityClassifierViewModel.getEmbeddings().observe(getActivity(), s -> {
            reco = new Gson().toJson(s);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       // ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout, container, false);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_add_wanted, container, false);

        imageView = rootView.findViewById(R.id.image_wanted);
        SaveWantedBtn = rootView.findViewById(R.id.btn_save_wanted);
        etFirstName = rootView.findViewById(R.id.etFirstName);
        etLastName = rootView.findViewById(R.id.etLastName);

        firebaseDatabase = FirebaseDatabase.getInstance();




        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                captureNewFace();
            }
        });






        SaveWantedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                wantedRef = FirebaseDatabase.getInstance().getReference("Wanted").push();
                 wanted = new Wanted();

                wanted.setFirstName(etFirstName.getText().toString());
                wanted.setLastName(etLastName.getText().toString());
                wanted.setKey(wantedRef.getKey());
                wanted.setLocation(new Location(0,0));
                wanted.setId(reco);
                wantedRef.setValue(wanted);











                //TODO change the first name and last name to the name (initialize the etFirstName and etLastName). //done

                //TODO send image to firebase storage


            }
        });

        return rootView;

    }



    Communicator c;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof Communicator) {
            c = (Communicator) context;
        }


    }


    public void captureNewFace()
    {

        imageView.setImageBitmap(FaceRecViewModel.bitmap);

        similarityClassifierViewModel.setFirst_mame(etFirstName.getText().toString());
        similarityClassifierViewModel.setLast_name(etLastName.getText().toString());

        c.addFace();



    }





}