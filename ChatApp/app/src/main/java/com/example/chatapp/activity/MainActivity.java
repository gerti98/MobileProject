package com.example.chatapp.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.connection.FirebaseDbManager;
import com.example.chatapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    public static FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        //Check if the user is already logged in, so that he can skip the login phase
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] permissionToRequest = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                Manifest.permission.INTERNET
        };


        // Check Permissions
       if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
           Log.w(TAG, " PERM> Audio permission granded");
            // You can use the API that requires the permission.
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(permissionToRequest);
        }

       if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.FOREGROUND_SERVICE) ==
                PackageManager.PERMISSION_GRANTED) {
           Log.w(TAG, " PERM> foregorund permission granded");
            // You can use the API that requires the permission.
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
           requestPermissionLauncher.launch(permissionToRequest);
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, " PERM> notification permission granded");
            // You can use the API that requires the permission.
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(permissionToRequest);
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, " PERM> internet permission granded");
            // You can use the API that requires the permission.
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(permissionToRequest);
        }

        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        createRequest();

        //Configure button to perform login if pressed
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), PermissionMap -> {
                for (Map.Entry<String, Boolean> entry : PermissionMap.entrySet()) {
                    Log.w(TAG, "AAAA" + entry.getKey()+ "  " + String.valueOf(entry.getValue()));
                }
            });

    private void createRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();
                            new FirebaseDbManager().addUserToDB(user);

                            //Start list of contacts activity
                            Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Sorry auth failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}