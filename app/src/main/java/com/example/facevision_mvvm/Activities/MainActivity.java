package com.example.facevision_mvvm.Activities;

import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.facevision_mvvm.Fragments.DashboardFragment;
import com.example.facevision_mvvm.Fragments.FaceFragments.Utils.Communicator;
import com.example.facevision_mvvm.Fragments.FaceRecFragment;
import com.example.facevision_mvvm.Fragments.ProfileFragment;
import com.example.facevision_mvvm.Models.Location;
import com.example.facevision_mvvm.R;
import com.example.facevision_mvvm.Services.LocationService;
import com.example.facevision_mvvm.ViewModels.WantedViewModel;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity implements Communicator {


    WantedViewModel wantedViewModel;
    ObjectAnimator objectAnimator;

    private  static  final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    Fragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseMessaging.getInstance().setAutoInitEnabled(true);


        //getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.zoom_in,R.anim.zoom_out).replace(R.id.fragment_container, new DashboardFragment() ).commit();
//


        fragment = new DashboardFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.fade_in,R.animator.fade_out);
        ft.replace(R.id.fragment_container, fragment, "fragment");
        ft.addToBackStack(null);
        ft.commit();




        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
        } else {
            //startLocationService();
        }

    }



    FaceRecFragment faceRecFragment;
    @Override
    public void addFace() {

        faceRecFragment.myAddFace();
        Toast.makeText(this, "add face", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void openFaceRec() {

        faceRecFragment = new FaceRecFragment();
        FragmentManager fragmentManager =  this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, faceRecFragment);
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_left,R.animator.slide_in_left,R.animator.slide_in_left,R.animator.slide_in_left);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }


    ProfileFragment profileFragment;
    @Override
    public void openProfile() {

        profileFragment = new ProfileFragment();
        FragmentManager fragmentManager =  this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.fade_in,R.animator.fade_out);
        fragmentTransaction.replace(R.id.fragment_container, profileFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(manager != null)
        {
            for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
              if (LocationService.class.getName().equals(service.service.getClassName())) {
                  if (service.foreground) {
                    return true;
                  }
              }
            }
            return false;
        }
        return false;
    }

    private void startLocationService() {
        if(!isLocationServiceRunning())
        {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Location.ACTION_START_SERVICE_LOCATION);
            startService(intent);
            Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService()
    {
        if(isLocationServiceRunning())
        {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Location.ACTION_STOP_SERVICE_LOCATION);
            startService(intent);
            Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length >0)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startLocationService();
            }
            else{
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }


}