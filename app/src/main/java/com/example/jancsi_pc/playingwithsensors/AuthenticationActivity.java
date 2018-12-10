package com.example.jancsi_pc.playingwithsensors;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;

import java.io.File;
import java.util.Date;

public class AuthenticationActivity extends AppCompatActivity {

    private final long ONE_MEGABYTE = 1024 * 1024;

    private TextView appNameTextView;

    private TextView titleTextView;
    private TextView selectedEmailTextView;

    private EditText emailEditText;
    private ImageView deleteEmailImageView;

    private EditText passwordEditText;
    private ImageView deletePasswordImageView;

    private EditText passwordEditText2;
    private ImageView deletePasswordImageView2;

    private Button authButton;
    private TextView registerORloginTextView;
    private TextView forgotPassTextView;

    private Button backButton;
    private ImageView editEmailImageView;
    private TextView reportErrorTextView;
    private TextView infoTextView;

    private TextView auth_offlineValidationTextView;

    private ConstraintLayout.LayoutParams params;

    private String mEmail = "";
    private String mPassword = "";
    private String password2 = "";

    private final String TAG = "AuthenticationActivity";

    private int requestPasswordResetCount = 0;
    private boolean doubleBackToExitPressedOnce = false;

    private boolean emailToPass = false;
    private boolean passToEmail = false;
    private boolean emailToRegister = false;
    private boolean registerToEmail = false;
    private boolean userExists = false;

    // Progress Bar
    private ProgressBar progressBar;
    private TextView loadingTextView;
    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        Log.d(TAG, ">>>RUN>>>onCreate()");

        appNameTextView = findViewById(R.id.auth_appNameTextView);
        appNameTextView.setTextColor( R.string.app_name );

        titleTextView = findViewById(R.id.auth_titleTextView);
        selectedEmailTextView= findViewById(R.id.auth_selectedEmailTextView);

        emailEditText = findViewById(R.id.auth_emailEditText);
        deleteEmailImageView = findViewById(R.id.auth_deleteEmailImageView);
        emailEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEditText.setError(null);
            }
        });

        passwordEditText = findViewById(R.id.auth_passwordEditText);
        deletePasswordImageView = findViewById(R.id.auth_deletePasswordImageView);
        passwordEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEditText.setError(null);
            }
        });

        passwordEditText2 = findViewById(R.id.auth_passwordEditText2);
        deletePasswordImageView2 = findViewById(R.id.auth_deletePasswordImageView2);
        passwordEditText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEditText2.setError(null);
            }
        });

        authButton = findViewById(R.id.auth_button);
        registerORloginTextView = findViewById(R.id.auth_registerORloginTextView);
        forgotPassTextView = findViewById(R.id.auth_forgotPassTextView);
        infoTextView = findViewById(R.id.auth_infoTextView);

        backButton = findViewById(R.id.auth_backButton);
        editEmailImageView = findViewById(R.id.auth_editEmailImageView); // ugyan azt csinalja mint a backButton csak felhasználóbarátabb

        forgotPassTextView.setText(R.string.forgotPassword);

        reportErrorTextView = findViewById(R.id.auth_errorReportTextView);
        reportErrorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>reportErrorTextViewClickListener");
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","abc@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with authentication.");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
                startActivity(Intent.createChooser(emailIntent, "Send mEmail..."));
            }
        });

        emailEditText.setText("");
        passwordEditText.setText("");
        passwordEditText2.setText("");

        progressBar = findViewById(R.id.auth_progressBar);
        loadingTextView = findViewById(R.id.auth_loadingCompleteTextView);
        loadingTextView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        auth_offlineValidationTextView = findViewById(R.id.auth_offlineValidationTextView);
        auth_offlineValidationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(AuthenticationActivity.this, GaitValidationActivity.class) );
            }
        });

        /*
        *
        *  Setup the Interface
        *
         */
        
        switch( Util.screenMode ){

            case EMAIL_MODE:{
                prepareScreenUIFor_email();
                break;
            }
            case PASSWORD_MODE:{
                prepareScreenUIFor_password();
                break;
            }
            case REGISTER_MODE:{
                prepareScreenUIFor_register();
                break;
            }
        }

        emailEditText.setText(mEmail);//TODO REMOVE BEFORE RELEASE
    } // OnCreate

    private void Register() {
        Log.d(TAG, ">>>RUN>>>Register()");
        Util.hideKeyboard(AuthenticationActivity.this);

        if( ! RequireEnabledInternetAndInternetConnection() ){
            return;
        }

        mEmail = emailEditText.getText().toString();
        mPassword = passwordEditText.getText().toString().trim(); //TODO ENCODE PASSWORD
        password2 = passwordEditText2.getText().toString().trim();

        Log.d(TAG, "\nmEmail=\""+ mEmail +"\"" );

        if( mEmail.equals("") ){
            emailEditText.setError("Wrong mEmail");
            emailEditText.requestFocus();
            return;
        }

        if( mPassword.length() <= 6 ){
            passwordEditText.setError("At least 6 character!");
            passwordEditText.requestFocus();
            return;
        }

        if(  mPassword.equals("") ){
            passwordEditText.setError("Must be filled!");
            passwordEditText.requestFocus();
            return;
        }

        if( ! mPassword.equals(password2) ){
            passwordEditText2.setError("Passwords has to be the same!");
            passwordEditText2.requestFocus();
            return;
        }

        //if there are no errors so far => create user with credentials
        Util.mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            //FirebaseUser user = Util.mAuth.getCurrentUser();
                            //updateUI(user);
                            sendVerificationEmail();
                            //Toast.makeText(AuthenticationActivity.this, getString(R.string.verifyMailbox),Toast.LENGTH_LONG).show();
                            Snackbar.make(findViewById(R.id.auth_main_layout),getString(R.string.verifyMailbox),Snackbar.LENGTH_LONG).show();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            //updateUI(null);
                            //Toast.makeText(AuthenticationActivity.this, getString(R.string.registerFailed),Toast.LENGTH_LONG).show();
                            Snackbar.make(findViewById(R.id.auth_main_layout),getString(R.string.registerFailed),Snackbar.LENGTH_LONG).show();
                        }
                        // ...
                    }

                    private void sendVerificationEmail() {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        user.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // mEmail sent

                                            // after mEmail is sent just logout the user and finish this activity
                                            FirebaseAuth.getInstance().signOut();
                                            //startActivity(new Intent(AuthenticationActivity.this, DataCollectorActivity.class));
                                            finish();
                                        }
                                        else
                                        {
                                            // mEmail not sent, so display message and restart the activity or do whatever you wish to do

                                            //restart this activity
                                            overridePendingTransition(0, 0);
                                            finish();
                                            overridePendingTransition(0, 0);
                                            startActivity(getIntent());

                                        }
                                    }
                                });

                    }
                });
        Log.d(TAG, "<<<FINISH<<<Register()");
    }

    private void Login(){
        Log.d(TAG, ">>>RUN>>>Login()");

        authButton.setEnabled(false);

        if( ! RequireEnabledInternetAndInternetConnection() ){
            return;
        }

        mEmail = emailEditText.getText().toString();
        mPassword = passwordEditText.getText().toString(); //TODO ENCODE PASSWORD

        Log.d(TAG, "mEmail=\""+ mEmail +"\"");

        if( mEmail.equals("") ){
            emailEditText.setError("Wrong mEmail");
            emailEditText.requestFocus();
            authButton.setEnabled(true);
            return;
        }

        if( mPassword.length() < 6 ){
            passwordEditText.setError("At least 6 character!");
            passwordEditText.requestFocus();
            authButton.setEnabled(true);
            return;
        }

        if(  mPassword.equals("") ){
            passwordEditText2.setError("Wrong Password!");
            passwordEditText2.requestFocus();
            authButton.setEnabled(true);
            return;
        }

        Util.mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            //FirebaseUser user = Util.mAuth.getCurrentUser();
                            //updateUI(user);
                            Util.userEmail = mEmail;
                            Util.isSignedIn = true;
                            authButton.setEnabled(true);

                            CheckUserModel();// Wait to get the model or create new one, will do the finish() !

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            //Toast.makeText(AuthenticationActivity.this, "Login Failed!", Toast.LENGTH_LONG).show();
                            Snackbar.make(findViewById(R.id.auth_main_layout),"Email or Password is incorrect!",Snackbar.LENGTH_LONG).show();
                        }
                        // ...
                    }
                });
        authButton.setEnabled(true);
        Log.d(TAG, "<<<FINISHED<<<Login()");
    }

    /*
     *
     *  Preparing the Authentication View for different Login and Register:
     *
     */


    private void prepareScreenUIFor_email(){
        Log.d(TAG, ">>>RUN>>>prepareScreenUIFor_email()");
        titleTextView.setText(R.string.login);
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText("");
        selectedEmailTextView.setVisibility(View.INVISIBLE);

        emailEditText.setVisibility(View.VISIBLE);
        deleteEmailImageView.setVisibility(View.VISIBLE);
        deleteEmailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEditText.setText("");
            }
        });

        passwordEditText.setVisibility(View.INVISIBLE);
        deletePasswordImageView.setVisibility(View.INVISIBLE);

        passwordEditText2.setVisibility(View.INVISIBLE);
        deletePasswordImageView2.setVisibility(View.INVISIBLE);

        authButton.setText(R.string.login);
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>authButtonClickListener");
                authButton.setEnabled(false);

                if (RequireEnabledInternetAndInternetConnection()) {            // This method gives feedback using Snackbar
                    Log.d(TAG, " isNetworkEnabled = true");
                    Log.d(TAG, " isNetworkConnection = true");
                    mEmail = emailEditText.getText().toString();


                    Log.d(TAG, "Waiting for fetchProvidersForEmail() ...");
                    if (!mEmail.equals("")) {
                        // TODO: kicserelni a fetchProviderForEmail-t lecserelni: https://firebase.google.com/docs/auth/admin/manage-users#list_all_users
                        Util.mAuth.fetchProvidersForEmail(mEmail).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Checking to see if user exists in firebase or not");
                                    ProviderQueryResult result = task.getResult();

                                    if(result != null && result.getProviders()!= null && result.getProviders().size() > 0){
                                        Log.d(TAG, "User exists, trying to go further");
                                        Log.d(TAG, "Go To: PASSWORD_MODE");
                                        emailToPass = true;         //
                                        passToEmail = false;        //
                                        emailToRegister = false;    // because of animations
                                        registerToEmail = false;    //
                                        Util.screenMode = Util.ScreenModeEnum.PASSWORD_MODE;
                                        prepareScreenUIFor_password();
                                        authButton.setEnabled(true);
                                        authButton.setEnabled(true);
                                    } else {
                                        userExists=false;
                                        emailEditText.setError("Please fill the Email field with a registered email address!");
                                        emailEditText.requestFocus();
                                        authButton.setEnabled(true);
                                        Log.d(TAG, "Login user doesn't exist");
                                        //TODO Snackbar asking the user to register
                                        authButton.setEnabled(true);
                                    }
                                } else {
                                    Log.w(TAG, "User check failed", task.getException());
                                    Toast.makeText(AuthenticationActivity.this,
                                            "There is a problem, please try again later.",
                                            Toast.LENGTH_SHORT).show();
                                    userExists=false;
                                    authButton.setEnabled(true);
                                }
                                //hide progress dialog
                                //hideProgressDialog();
                                //enable and disable login, logout buttons depending on signin status
                                ///showAppropriateOptions();
                            }
                        });
                    }else{
                        //Toast.makeText(AuthenticationActivity.this, "Please fill the Email field!", Toast.LENGTH_LONG).show();
                        emailEditText.setError("Please fill the Email field with a valid mEmail address!");
                        emailEditText.requestFocus();
                    }

                }else{
                    authButton.setEnabled(true);
                }
            }
        });
        params = (ConstraintLayout.LayoutParams) authButton.getLayoutParams();
        params.topToBottom = emailEditText.getId();
        authButton.setLayoutParams(params);
        authButton.requestLayout();

        registerORloginTextView.setText(R.string.createNewAccount);
        registerORloginTextView.setVisibility(View.VISIBLE);
        registerORloginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>registerORloginTextViewClickListener");
                Log.d(TAG,"Go To: REGISTER_MODE");
                /*
                userExists=false;
                mEmail=emailEditText.getText().toString();
                if(!mEmail.equals("")) {
                    /Util.mAuth.fetchProvidersForEmail(mEmail).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "checking to see if user exists in firebase or not");
                                ProviderQueryResult result = task.getResult();

                                if (result != null && result.getProviders() != null && result.getProviders().size() > 0) {
                                    Log.d(TAG, "User exists, stopping");
                                    userExists = true;
                                } else {
                                    Log.d(TAG, "User doesn't exist");
                                    //Toast.makeText(AuthenticationActivity.this, "Please fill the Email field!", Toast.LENGTH_LONG).show();
                                    userExists=false;
                                }
                            } else {
                                Log.w(TAG, "User check failed", task.getException());
                                Toast.makeText(AuthenticationActivity.this,
                                        "There is a problem, please try again later.",
                                        Toast.LENGTH_SHORT).show();
                                userExists=false;
                            }
                        }
                    });
                }
                if(userExists){
                    Toast.makeText(AuthenticationActivity.this, "Email already registered!", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Stopped registration");
                    return;
                }
                */
                emailToPass = false;        //
                passToEmail = false;         //
                emailToRegister = true;    // because of animations
                registerToEmail = false;    //
                Util.screenMode = Util.ScreenModeEnum.REGISTER_MODE;
                prepareScreenUIFor_register();
            }
        });

        forgotPassTextView.setVisibility(View.VISIBLE);
        forgotPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>forgotPassTextViewClickListener");
                resetPassword();
            }
        });

        backButton.setVisibility(View.INVISIBLE);
        editEmailImageView.setVisibility(View.INVISIBLE);

        if( passToEmail ) {
            passwordEditText.setText("");
            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -180);
            translateAnimation.setDuration(300);
            TranslateAnimation translateAnimation2 = new TranslateAnimation(0, 0, 150, 0);
            translateAnimation2.setDuration(300);

            passwordEditText.setAnimation(translateAnimation);
            deletePasswordImageView.setAnimation(translateAnimation);
            authButton.setAnimation(translateAnimation2);
            registerORloginTextView.setAnimation(translateAnimation2);
            forgotPassTextView.setAnimation(translateAnimation2);
            //passwordEditText.startAnimation(alphaAnimation);
            //registerORloginTextView.startAnimation(alphaAnimation);
            //deletePasswordImageView.startAnimation(alphaAnimation);
        }

        if( registerToEmail ) {
            passwordEditText.setText("");
            TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, 0, -180);
            translateAnimation1.setDuration(300);
            TranslateAnimation translateAnimation2 = new TranslateAnimation(0, 0, 0, -360);
            translateAnimation2.setDuration(300);
            TranslateAnimation translateAnimation3 = new TranslateAnimation(0, 0, 360, 0);
            translateAnimation3.setDuration(300);

            passwordEditText.setAnimation(translateAnimation1);
            deletePasswordImageView.setAnimation(translateAnimation1);
            passwordEditText2.setAnimation(translateAnimation2);
            deletePasswordImageView2.setAnimation(translateAnimation2);
            authButton.setAnimation(translateAnimation3);
            registerORloginTextView.setAnimation(translateAnimation3);
            forgotPassTextView.setAnimation(translateAnimation3);
            //passwordEditText.startAnimation(alphaAnimation);
            //registerORloginTextView.startAnimation(alphaAnimation);
            //deletePasswordImageView.startAnimation(alphaAnimation);
        }
    }

    private void prepareScreenUIFor_password(){
        Log.d(TAG, ">>>RUN>>>prepareScreenUIFor_password()");
        titleTextView.setText(R.string.login);
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText(mEmail);
        selectedEmailTextView.setVisibility(View.VISIBLE);

        emailEditText.setVisibility(View.INVISIBLE);
        deleteEmailImageView.setVisibility(View.INVISIBLE);

        passwordEditText.setVisibility(View.VISIBLE);
        deletePasswordImageView.setVisibility(View.VISIBLE);
        deletePasswordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEditText.setText("");
            }
        });

        passwordEditText2.setVisibility(View.INVISIBLE);
        deletePasswordImageView2.setVisibility(View.INVISIBLE);

        if(mEmail.trim().equals("millejanos31@gmail.com") || mEmail.trim().equals("wolterwill31@gmail.com") ){  //TODO : DELETE THIS !
            mPassword = "01234567";
            passwordEditText.setText("01234567");
        }

        params = (ConstraintLayout.LayoutParams) authButton.getLayoutParams();
        params.topToBottom = passwordEditText.getId();
        //authButton.startAnimation(alphaAnimation);
        authButton.setLayoutParams( params );
        authButton.requestLayout();
        authButton.setText(R.string.login);
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Finishing Login
                Log.d(TAG, ">>>RUN>>>authButtonClickListener");
                if( RequireEnabledInternetAndInternetConnection() ) {            // This method gives feedback using Snackbar
                    mPassword = passwordEditText.getText().toString();

                    Login();

                }else{
                    authButton.setEnabled(true);
                }
            }
        });

        //registerORloginTextView.setText("Back."); //Van Back gomb bal oldalt fent
        registerORloginTextView.setVisibility(View.INVISIBLE);
        //registerORloginTextView.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Log.d(TAG,"Go To: EMAIL_MODE");
        //        Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
        //        prepareScreenUIFor_email();
        //    }
        //});

        forgotPassTextView.setText(R.string.forgotPassword);
        forgotPassTextView.setVisibility(View.VISIBLE);
        forgotPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>forgotPassTextViewClickListener");
                resetPassword();
            }
        });

        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>backButtonClickListener");
                Log.d(TAG,"Go To: EMAIL_MODE");
                emailToPass = false;        //
                passToEmail = true;         //
                emailToRegister = false;    // because of animations
                registerToEmail = false;    //
                Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
                prepareScreenUIFor_email();
            }
        });
        editEmailImageView.setVisibility(View.VISIBLE);
        editEmailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>editEmailImageViewClickListener");
                Log.d(TAG,"Go To: EMAIL_MODE");
                emailToPass = false;        //
                passToEmail = true;         //
                emailToRegister = false;    // because of animations
                registerToEmail = false;    //
                Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
                prepareScreenUIFor_email();
            }
        });
        //Animation:

        // AlphaAnimation alphaAnimation = new AlphaAnimation(0.2f, 1.0f);
        // alphaAnimation.setDuration(300);
        // alphaAnimation.setFillBefore(true);
        // alphaAnimation.setFillAfter(false);

        if( emailToPass ) {
            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -150, 0);
            translateAnimation.setDuration(300);

            authButton.setAnimation(translateAnimation);
            passwordEditText.setAnimation(translateAnimation);
            deletePasswordImageView.setAnimation(translateAnimation);
            registerORloginTextView.setAnimation(translateAnimation);
            forgotPassTextView.setAnimation(translateAnimation);
            //passwordEditText.startAnimation(alphaAnimation);
            //registerORloginTextView.startAnimation(alphaAnimation);
            //deletePasswordImageView.startAnimation(alphaAnimation);
        }
    }

    private void prepareScreenUIFor_register(){
        Log.d(TAG, ">>>RUN>>>prepareScreenUIFor_register()");
        titleTextView.setText(R.string.register);
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText("");
        selectedEmailTextView.setVisibility(View.INVISIBLE);

        emailEditText.setVisibility(View.VISIBLE);
        deleteEmailImageView.setVisibility(View.VISIBLE);
        deleteEmailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEditText.setText("");
            }
        });


        passwordEditText.setText("");
        passwordEditText.setVisibility(View.VISIBLE);
        deletePasswordImageView.setVisibility(View.VISIBLE);
        deletePasswordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEditText.setText("");
            }
        });

        passwordEditText2.setText("");
        passwordEditText2.setVisibility(View.VISIBLE);
        deletePasswordImageView2.setVisibility(View.VISIBLE);
        deletePasswordImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEditText2.setText("");
            }
        });

        params = (ConstraintLayout.LayoutParams) authButton.getLayoutParams();
        params.topToBottom = passwordEditText2.getId();
        authButton.setLayoutParams( params );
        authButton.requestLayout();

        authButton.setText(R.string.register);
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( emailEditText.getText().toString().trim().equals("") ){
                    emailEditText.setError("This field must be filled!");
                    return;
                }
                if( passwordEditText.getText().toString().trim().equals("") ){
                    passwordEditText.setError("This field must be filled!");
                    return;
                }
                if( passwordEditText.getText().toString().trim().length() < 6 ){
                    passwordEditText.setError("Passwords has to be at least 6 characters!");
                    return;
                }
                if( passwordEditText2.getText().toString().trim().equals("") ){
                    passwordEditText2.setError("This field must be filled!");
                    return;
                }
                if( ! passwordEditText.getText().toString().trim().equals( passwordEditText2.getText().toString().trim() ) ){
                    passwordEditText2.setError("The passwords must be the same!");
                    return;
                }
                authButton.setEnabled(false);

                mEmail =emailEditText.getText().toString();
                if(!mEmail.equals("")) {
                    // TODO: kicserelni a fetchProviderForEmail-t lecserelni: https://firebase.google.com/docs/auth/admin/manage-users#list_all_users
                    Util.mAuth.fetchProvidersForEmail(mEmail).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "checking to see if user exists in firebase or not");
                                ProviderQueryResult result = task.getResult();

                                if (result != null && result.getProviders() != null && result.getProviders().size() > 0) {
                                    Log.d(TAG, "User exists, stopping");
                                    userExists = true;
                                    Toast.makeText(AuthenticationActivity.this, "Email already registered!", Toast.LENGTH_LONG).show();
                                } else {
                                    Log.d(TAG, "User doesn't exist ==> Register");
                                    //Toast.makeText(AuthenticationActivity.this, "Please fill the Email field!", Toast.LENGTH_LONG).show();
                                    userExists=false;
                                    mPassword = passwordEditText.getText().toString().trim();
                                    Register();
                                }
                            } else {
                                Log.w(TAG, "User check failed", task.getException());
                                Toast.makeText(AuthenticationActivity.this,
                                        "There is a problem, please try again later.",
                                        Toast.LENGTH_SHORT).show();
                                userExists=false;
                                mPassword = passwordEditText.getText().toString().trim();
                                Register();
                            }
                            authButton.setEnabled(true);
                        }
                    });
                }
                /*if(userExists){
                    Toast.makeText(AuthenticationActivity.this, "Email already registered!", Toast.LENGTH_LONG).show();
                    return;
                }
                mPassword = passwordEditText.getText().toString().trim();
                Register();
                */
            }
        });

        registerORloginTextView.setText(R.string.alreadyHaveAccount);
        registerORloginTextView.setVisibility(View.VISIBLE);
        registerORloginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>registerORloginTextViewClickListener");
                Log.d(TAG,"Go To: EMAIL_MODE");
                emailToPass = false;        //
                passToEmail = false;         //
                emailToRegister = false;    // because of animations
                registerToEmail = true;    //
                Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
                prepareScreenUIFor_email();
            }
        });

        //forgotPassTextView.setText("forgot password.");
        forgotPassTextView.setVisibility(View.INVISIBLE);
        forgotPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>forgotPassTextViewClickListener");
                resetPassword();
            }
        });

        backButton.setVisibility(View.INVISIBLE);
        editEmailImageView.setVisibility(View.INVISIBLE);

        if( emailToRegister ) {
            passwordEditText.setText("");
            TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, -180, 0);
            translateAnimation1.setDuration(300);
            TranslateAnimation translateAnimation2 = new TranslateAnimation(0, 0, -360, 0);
            translateAnimation2.setDuration(300);


            passwordEditText.setAnimation(translateAnimation1);
            deletePasswordImageView.setAnimation(translateAnimation1);
            passwordEditText2.setAnimation(translateAnimation2);
            deletePasswordImageView2.setAnimation(translateAnimation2);
            authButton.setAnimation(translateAnimation2);
            registerORloginTextView.setAnimation(translateAnimation2);
            forgotPassTextView.setAnimation(translateAnimation2);
            //passwordEditText.startAnimation(alphaAnimation);
            //registerORloginTextView.startAnimation(alphaAnimation);
            //deletePasswordImageView.startAnimation(alphaAnimation);
        }
    }

    /*
     *
     *  Connection Testers:
     *
     */

    // A + B and feedback with Snackbar to the user
    private boolean RequireEnabledInternetAndInternetConnection() {          // TODO: altalanositas: RequireEnabledInternetAndInternetConnection(Activity activity) {...}
        Log.d(TAG, ">>>RUN>>>RequireEnabledInternetAndInternetConnection()");
        Util.hideKeyboard(AuthenticationActivity.this);

        //Asking the user to enable WiFi:
        boolean isNetworkEnabled = CheckWiFiNetwork();

        //Asking for connection:
        boolean isNetworkConnection = RequireInternetConnection();

        if (!isNetworkEnabled) {
            //authButton.setError("Please enable internet connection!");
            Log.d("TAG", " isNetworkEnabled = false");
            View mainLayoutView = findViewById(R.id.auth_main_layout);
            Snackbar.make(mainLayoutView, "Please enable internet connection!", Snackbar.LENGTH_SHORT).show();
        } else {
            if (!isNetworkConnection) {
                //authButton.setError("No internet connection detected!");
                Log.d(TAG, " isNetworkConnection = false");
                View view = findViewById(R.id.auth_main_layout);
                Snackbar.make(view, "No internet connection detected!", Snackbar.LENGTH_SHORT).show();
            } else {
                Log.d(TAG,"RequireEnabledInternetAndInternetConnection() --> true");
                return true;
            }
        }
        Log.d(TAG,"RequireEnabledInternetAndInternetConnection() --> false");
        return false;
    }
    // B
    private boolean RequireInternetConnection() {
        Log.d(TAG, ">>>RUN>>>RequireInternetConnection()");
        ConnectivityManager cm = (ConnectivityManager) AuthenticationActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        // While there is no connection, force the user to connect
        /*while( ! isConnected ){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AuthenticationActivity.this);

            // set title
            alertDialogBuilder.setTitle("No internet detected");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Make you shore you are connected to the internet")
                    .setCancelable(false)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Nothing (Retry)
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Util.isFinished = true;
                            finish(); //close the App
                        }
                    });
            isConnected = activeNetwork != null && activeNetwork.isConnected();

            return false;
        }*/
        if(isConnected){
            Log.d(TAG,"RequireInternetConnection() --> true");
            return true;
        }
        // else:
        Log.d(TAG,"RequireInternetConnection() --> false");
        return false;
    }
    // A
    private boolean CheckWiFiNetwork() {
        Log.d(TAG, ">>>RUN>>>CheckWiFiNetwork()");

        final WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if( ! mWifiManager.isWifiEnabled() ) {
            /*
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AuthenticationActivity.this);

            // set title
            alertDialogBuilder.setTitle("Wifi Settings");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Do you want to enable WIFI ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // enable wifi
                            mWifiManager.setWifiEnabled(true);

                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //disable wifi
                            //mWifiManager.setWifiEnabled(false);
                            Util.isFinished = true;
                            finish(); //close the App
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            */
            Log.d(TAG,"CheckWiFiNetwork() --> false");
            return false;
        }
        // else:
        Log.d(TAG,"CheckWiFiNetwork() --> true");
        return true;
    }

    /*
     *
     *  Common used methods:
     *
     */

    private void resetPassword() {
        Log.d(TAG,">>>RUN>>>resetPassword()");
        forgotPassTextView.setVisibility(View.INVISIBLE);
        mEmail =emailEditText.getText().toString().trim();
        if (mEmail.equals("")) {
            Log.d(TAG,">>>RUN>>>Email field is empty ==> \"please fill it\"");
            emailEditText.setError("Type your mEmail before mPassword request.");
            //emailEditText.requestFocus();
            forgotPassTextView.setVisibility(View.VISIBLE);
            return;
        }else{
            Log.d(TAG, "Waiting for fetchProvidersForEmail() ...");
            // TODO: kicserelni a fetchProviderForEmail-t lecserelni: https://firebase.google.com/docs/auth/admin/manage-users#list_all_users
            Util.mAuth.fetchProvidersForEmail(mEmail).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "checking to see if user exists in firebase or not");
                        ProviderQueryResult result = task.getResult();

                        if(result != null && result.getProviders()!= null && result.getProviders().size() > 0){
                            Log.d(TAG, "User exists, trying to go further");
                            userExists=true;
                        } else {
                            Log.d(TAG, "User doesn't exist");
                            //Toast.makeText(AuthenticationActivity.this, "Please fill the Email field!", Toast.LENGTH_LONG).show();
                            emailEditText.setError("Please fill the Email field with a registered email address!");
                            emailEditText.requestFocus();
                            userExists=false;
                        }
                    } else {
                        Log.w(TAG, "User check failed", task.getException());
                        Toast.makeText(AuthenticationActivity.this,
                                "There is a problem, please try again later.",
                                Toast.LENGTH_SHORT).show();
                        userExists=false;
                    }
                }
            });
        }

        if(!userExists){
            forgotPassTextView.setVisibility(View.VISIBLE);
            return;
        }

        Util.userEmail = emailEditText.getText().toString().trim();
        mEmail = Util.userEmail;

        if( requestPasswordResetCount == 0 ){
            // First mPassword reset request
            Log.d(TAG,"requestPasswordResetCount(" + requestPasswordResetCount + ") > 0 ==> AlertDialog");

            Util.mAuth.sendPasswordResetEmail(Util.userEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Reset mPassword request sent.");
                                View mainLayoutView = findViewById(R.id.auth_main_layout);
                                //Snackbar.make(mainLayoutView, "Reset mEmail is sent!", Snackbar.LENGTH_SHORT).show();
                                Toast.makeText(AuthenticationActivity.this, "Reset mPassword request was sent!", Toast.LENGTH_LONG ).show();
                            }
                        }
                    });
            requestPasswordResetCount++;

        }else{
            // If the user tries to send mPassword reset multiple times in a row
            Log.d(TAG,"requestPasswordResetCount(" + requestPasswordResetCount + ") > 0 ==> AlertDialog");

            AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you want to send a reset request?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // leave the method to run
                    //Toast.makeText(AuthenticationActivity.this, "SEND REQUEST NUMBER: " + requestPasswordResetCount, Toast.LENGTH_SHORT).show();
                    Util.mAuth.sendPasswordResetEmail(Util.userEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Reset mPassword request sent.");
                                        View mainLayoutView = findViewById(R.id.auth_main_layout);
                                        //Snackbar.make(mainLayoutView, "Reset mEmail is sent!", Snackbar.LENGTH_SHORT).show();
                                        Toast.makeText(AuthenticationActivity.this, "Reset mPassword request was sent!", Toast.LENGTH_LONG ).show();
                                    }
                                }
                            });
                    requestPasswordResetCount++;
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();


        }


        forgotPassTextView.setVisibility(View.VISIBLE);
    }

    private void CheckUserModel() {
        // Test user model existence in firebase
        // AFTER signInWithEmailAndPassword is succed !
        Log.d(TAG, ">>>RUN>>>CheckUserModel()");

        Util.mRef = Util.mStorage.getReference().child("models/model_" + Util.mAuth.getUid() + ".mdl" );

        Log.d(TAG, "Util.mRef= Util.mStorage.getReference().child(models/model_" + Util.mAuth.getUid() + ".mdl)" );
        Log.d(TAG, "Util.mRef= " + Util.mRef);
        Log.d(TAG, "Util.mRef.toString()= "  + Util.mRef.toString() );

        loadingTextView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        final File localFile;
        try {
            //localFile = new File("/storage/emulated/0/logmein","model.mdl");
            localFile = File.createTempFile("model_", ".mdl");
            Util.mRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    Util.hasUserModel = true;
                    Util.isSetUserModel = true;
                    Log.i(TAG,"### Util.hasUserModel = true;");
                    Log.i(TAG, "MODEL FOUND: Local File Path: " + localFile.getAbsolutePath());
                    Log.i(TAG,"<<<finish()<<<");
                    loadingTextView.setVisibility(View.VISIBLE);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Util.hasUserModel = false;
                    Util.isSetUserModel = true;
                    Log.i(TAG,"### Util.hasUserModel = false;");
                    Log.i(TAG, "MODEL NOT FOUND: ERROR: getFile()");
                    Log.i(TAG,"<<<finish()<<<");
                    //e.printStackTrace();
                    loadingTextView.setVisibility(View.VISIBLE);
                    finish();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    final double process = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    //progressDialog.setMessage("Downloaded: " + (int) process + "%");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress((int)process);
                        }
                    });
                }
            });
            /*mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"mHandler.post() --> Finish()");
                    loadingTextView.setVisibility(View.VISIBLE);
                }
            });*/
        } catch (Exception e) {
            Util.hasUserModel = false;
            Util.isSetUserModel = true;
            Log.i(TAG,"### Util.hasUserModel = false;");
            Log.e(TAG, "ERROR: MODEL: EXCEPTION !");
            e.printStackTrace();
            finish();
        }

        Log.d(TAG, "<<<FINISHED<<<CheckUserModel()");
    }

    @Override
    public void onBackPressed() {
        // back button pressed:
        //  if EMAIL_MODE ==> exit app
        //  if PASSWORD_MODE ==> EMAIL_MODE
        //  if REGISTER_MODE ==> EMAIL_MODE
        if( Util.screenMode == Util.ScreenModeEnum.EMAIL_MODE ) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                Util.isFinished = true;
                finish();
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
        if( Util.screenMode == Util.ScreenModeEnum.PASSWORD_MODE ) {
            Log.d(TAG,"Go To: EMAIL_MODE");
            emailToPass = false;        //
            passToEmail = true;         //
            emailToRegister = false;    // because of animations
            registerToEmail = false;    //
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            prepareScreenUIFor_email();
        }
        if( Util.screenMode == Util.ScreenModeEnum.REGISTER_MODE ) {
            Log.d(TAG,"Go To: EMAIL_MODE");
            emailToPass = false;        //
            passToEmail = false;        //
            emailToRegister = false;    // because of animations
            registerToEmail = true;    //
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            prepareScreenUIFor_email();
        }
    }

    @Override
    public void onResume(){

        Util.isSetUserModel = false;

        if( Util.isFinished ){
            Log.d(TAG," isFinished() = true");
            finish();
        }

        Date date = new Date();
        CharSequence s  = DateFormat.format("yyyyMMdd_HHmmss", date.getTime());
        Util.preferencesEditor.putString(Util.LAST_LOGGED_IN_DATE_KEY, s.toString() );
        Util.preferencesEditor.apply();


        super.onResume();
    }
}
