package com.example.jancsi_pc.playingwithsensors;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

import static android.support.design.widget.Snackbar.LENGTH_LONG;

public class AuthenticationActivity extends AppCompatActivity {

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
    private TextView forgetPassTextView;

    private Button backButton;
    private ImageView editEmailImageView;
    private TextView reportErrorTextView;
    private TextView infoTextView;

    private ConstraintLayout.LayoutParams params;

    private String email = "";
    private String password = "";
    private String password2 = "";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final String TAG = "AuthenticationActivity";

    private CoordinatorLayout coordinatorLayoutForSnackbar;

    private int requestPasswordResetCount = 0;
    private boolean doubleBackToExitPressedOnce = false;

    private boolean emailToPass = false;
    private boolean passToEmail = false;
    private boolean emailToRegister = false;
    private boolean registerToEmail = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        Log.d(TAG, ">>>RUN>>>onCreate()");

        if( Util.isFinished ){
            Log.d(TAG," isFinished() = true");
            finish();
        }

        appNameTextView = findViewById(R.id.appNameTextView);
        appNameTextView.setTextColor( R.string.app_name );

        titleTextView = findViewById(R.id.titleTextView);
        selectedEmailTextView= findViewById(R.id.selectedEmailTextView);

        emailEditText = findViewById(R.id.emailEditText);
        deleteEmailImageView = findViewById(R.id.deleteEmailImageView);

        passwordEditText = findViewById(R.id.passwordEditText);
        deletePasswordImageView = findViewById(R.id.deletePasswordImageView);

        passwordEditText2 = findViewById(R.id.passwordEditText2);
        deletePasswordImageView2 = findViewById(R.id.deletePasswordImageView2);

        authButton = findViewById(R.id.button);
        registerORloginTextView = findViewById(R.id.registerORloginTextView);
        forgetPassTextView = findViewById(R.id.forgetPassTextView);
        infoTextView = findViewById(R.id.infoTextView);

        backButton = findViewById(R.id.backButton);
        editEmailImageView = findViewById(R.id.editEmailImageView); // ugyan azt csinalja mint a backButton csak felhasználóbarátabb

        forgetPassTextView.setText("Forget password.");

        reportErrorTextView = findViewById(R.id.errorReportTextView);
        reportErrorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>reportErrorTextViewClickListener");
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","abc@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with authentication.");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
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
    }

    private void Register() {
        Log.d(TAG, ">>>RUN>>>Register()");

        if( RequireEnabledInternetAndIternetConnection() ){
            return;
        }

        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString().trim(); //TODO ENCODE PASSWORD
        password2 = passwordEditText2.getText().toString().trim();

        Log.d(TAG, "\nemail=\""+email+"\"" + "\npassword=\""+password+"\"\n" + "\npassword2=\""+password2+"\"\n" );

        if( email.equals("") ){
            emailEditText.setError("Wrong email");
            emailEditText.requestFocus();
            return;
        }

        if( password.length() <= 6 ){
            passwordEditText.setError("At least 6 character!");
            passwordEditText.requestFocus();
            return;
        }

        if(  password.equals("") ){
            passwordEditText.setError("Must to be filled!");
            passwordEditText.requestFocus();
            return;
        }

        if( ! password.equals(password2) ){
            passwordEditText2.setError("Passwords has to be the same!");
            passwordEditText2.requestFocus();
            return;
        }

        //Ha Nincs hiba:

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            sendVerificationEmail();
                            //Toast.makeText(AuthenticationActivity.this, getString(R.string.verifyMailbox),Toast.LENGTH_LONG).show();
                            Snackbar.make(findViewById(R.id.main_layout),getString(R.string.verifyMailbox),Snackbar.LENGTH_LONG).show();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            //updateUI(null);
                            //Toast.makeText(AuthenticationActivity.this, getString(R.string.registerFailed),Toast.LENGTH_LONG).show();
                            Snackbar.make(findViewById(R.id.main_layout),getString(R.string.registerFailed),Snackbar.LENGTH_LONG).show();
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
                                            // email sent

                                            // after email is sent just logout the user and finish this activity
                                            FirebaseAuth.getInstance().signOut();
                                            //startActivity(new Intent(AuthenticationActivity.this, DataCollectorActivity.class));
                                            finish();
                                        }
                                        else
                                        {
                                            // email not sent, so display message and restart the activity or do whatever you wish to do

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
    }

    private void Login(){
        Log.d(TAG, ">>>RUN>>>Login()");

        authButton.setEnabled(false);

        if( ! RequireEnabledInternetAndIternetConnection() ){
            return;
        }

        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString(); //TODO ENCODE PASSWORD

        Log.d(TAG, "\nemail=\""+email+"\"" + "\npassword=\""+password+"\"\n");

        if( email == "" ){
            emailEditText.setError("Wrong email");
            emailEditText.requestFocus();
            authButton.setEnabled(true);
            return;
        }

        if( password.length() < 6 ){
            passwordEditText.setError("At least 6 character!");
            passwordEditText.requestFocus();
            authButton.setEnabled(true);
            return;
        }

        if(  password == "" ){
            passwordEditText2.setError("Wrong Password!");
            passwordEditText2.requestFocus();
            authButton.setEnabled(true);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            Util.userEmail = email;
                            Util.isSignedIn = true;
                            authButton.setEnabled(true);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            //Toast.makeText(AuthenticationActivity.this, "Login Failed!", Toast.LENGTH_LONG).show();
                            Snackbar.make(findViewById(R.id.main_layout),"Email or Password is incorrect!",Snackbar.LENGTH_LONG).show();
                            //updateUI(null);
                        }
                        // ...
                    }
                });
        authButton.setEnabled(true);
    }

    /*
     *
     *  Preparing the Authentication View for different Login and Register:
     *
     */


    private void prepareScreenUIFor_email(){
        Log.d(TAG, ">>>RUN>>>prepareScreenUIFor_email()");
        titleTextView.setText("Login");
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

        authButton.setText("Login");
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>authButtonClickListener");
                if (RequireEnabledInternetAndIternetConnection()) {            // This method gives feedback using Snackbar
                    Log.d("TAG", " isNetworkEnabled = true");
                    Log.d("TAG", " isNetworkConnection = true");
                    email = emailEditText.getText().toString();
                    if (!email.equals("")) {
                        Log.d(TAG, "Go To: PASSWORD_MODE");
                        emailToPass = true;         //
                        passToEmail = false;        //
                        emailToRegister = false;    // because of animations
                        registerToEmail = false;    //
                        Util.screenMode = Util.ScreenModeEnum.PASSWORD_MODE;
                        prepareScreenUIFor_password();
                    } else {
                        //Toast.makeText(AuthenticationActivity.this, "Please fill the Email field!", Toast.LENGTH_LONG).show();
                        emailEditText.setError("Please fill the Email field!");
                        emailEditText.requestFocus();
                    }
                }

            }
        });
        params = (ConstraintLayout.LayoutParams) authButton.getLayoutParams();
        params.topToBottom = emailEditText.getId();
        authButton.setLayoutParams(params);
        authButton.requestLayout();

        registerORloginTextView.setText("Create new account.");
        registerORloginTextView.setVisibility(View.VISIBLE);
        registerORloginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>registerORloginTextViewClickListener");
                Log.d(TAG,"Go To: REGISTER_MODE");
                emailToPass = false;        //
                passToEmail = false;         //
                emailToRegister = true;    // because of animations
                registerToEmail = false;    //
                Util.screenMode = Util.ScreenModeEnum.REGISTER_MODE;
                prepareScreenUIFor_register();
            }
        });

        forgetPassTextView.setVisibility(View.VISIBLE);
        forgetPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>forgetPassTextViewClickListener");
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
            forgetPassTextView.setAnimation(translateAnimation2);
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
            forgetPassTextView.setAnimation(translateAnimation3);
            //passwordEditText.startAnimation(alphaAnimation);
            //registerORloginTextView.startAnimation(alphaAnimation);
            //deletePasswordImageView.startAnimation(alphaAnimation);
        }
    }

    private void prepareScreenUIFor_password(){
        Log.d(TAG, ">>>RUN>>>prepareScreenUIFor_password()");

        titleTextView.setText("Login");
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText( email );
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

        if(email.trim().equals("millejanos31@gmail.com") || email.trim().equals("wolterwill31@gmail.com") ){  //TODO : DELETE THIS !
            password = "01234567";
            passwordEditText.setText("01234567");
        }

        params = (ConstraintLayout.LayoutParams) authButton.getLayoutParams();
        params.topToBottom = passwordEditText.getId();
        //authButton.startAnimation(alphaAnimation);
        authButton.setLayoutParams( params );
        authButton.requestLayout();

        authButton.setText("Login");
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>authButtonClickListener");
                if( RequireEnabledInternetAndIternetConnection() ) {            // This method gives feedback using Snackbar
                    password = passwordEditText.getText().toString();
                    Login();
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
        forgetPassTextView.setText("Forget password.");
        forgetPassTextView.setVisibility(View.VISIBLE);
        forgetPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>forgetPassTextViewClickListener");
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
            forgetPassTextView.setAnimation(translateAnimation);
            //passwordEditText.startAnimation(alphaAnimation);
            //registerORloginTextView.startAnimation(alphaAnimation);
            //deletePasswordImageView.startAnimation(alphaAnimation);
        }
    }

    private void prepareScreenUIFor_register(){
        Log.d(TAG, ">>>RUN>>>prepareScreenUIFor_register()");
        titleTextView.setText("Register");
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


        passwordEditText.setVisibility(View.VISIBLE);
        deletePasswordImageView.setVisibility(View.VISIBLE);
        deletePasswordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEditText.setText("");
            }
        });

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

        authButton.setText("Register");
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });

        registerORloginTextView.setText("Already have account?");
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

        //forgetPassTextView.setText("Forget password.");
        forgetPassTextView.setVisibility(View.INVISIBLE);
        forgetPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>forgetPassTextViewClickListener");
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
            forgetPassTextView.setAnimation(translateAnimation2);
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
    private boolean RequireEnabledInternetAndIternetConnection() {          // TODO: altalanositas: RequireEnabledInternetAndIternetConnection(Activity activity) {...}
        Log.d(TAG, ">>>RUN>>>RequireEnabledInternetAndIternetConnection()");
        // If keyboard is shown then hide:
        Activity activity = AuthenticationActivity.this;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(AuthenticationActivity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View activityOnFocusView = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (activityOnFocusView == null) {
            activityOnFocusView = new View(activity);
        }
        imm.hideSoftInputFromWindow(activityOnFocusView.getWindowToken(), 0);

        //Asking the user to enable WiFi:
        boolean isNetworkEnabled = CheckWiFiNetwork();

        //Asking for connection:
        boolean isNetworkConnection = RequireInternetConnection();

        if (!isNetworkEnabled) {
            //authButton.setError("Please enable internet connection!");
            Log.d("TAG", " isNetworkEnabled = false");
            View mainLayoutView = findViewById(R.id.main_layout);
            Snackbar.make(mainLayoutView, "Please enable internet connection!", Snackbar.LENGTH_SHORT).show();
        } else {
            if (!isNetworkConnection) {
                //authButton.setError("No internet connection detected!");
                Log.d("TAG", " isNetworkConnection = false");
                View view = findViewById(R.id.main_layout);
                Snackbar.make(view, "No internet connection detected!", Snackbar.LENGTH_SHORT).show();
            } else {
                return true;
            }
        }
        return false;
    }
    // B
    private boolean RequireInternetConnection() {
        Log.d(TAG, ">>>RUN>>>RequireInternetConnection()");
        ConnectivityManager cm = (ConnectivityManager) AuthenticationActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        // While there is no connection, force the user to connect
        while( ! isConnected ){
            /*
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
            */
            return false;
        }
        // else:
        return true;
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
            return false;
        }
        // else:
        return true;
    }

    /*
     *
     *  Common used methods
     *
     */


    private void resetPassword() {
        Log.d(TAG,">>>RUN>>>resetPassword()");
        forgetPassTextView.setVisibility(View.INVISIBLE);
        if (emailEditText.getText().toString().trim().equals("")) {
            Log.d(TAG,">>>RUN>>>Email field is empty ==> \"please fill it\"");
            emailEditText.setError("Type your email before password request.");
            //emailEditText.requestFocus();
            forgetPassTextView.setVisibility(View.VISIBLE);
            return;
        }

        if( requestPasswordResetCount > 0 ){
            // If the user tries to send password reset multiple times in a row
            Log.d(TAG,"requestPasswordResetCount(" + requestPasswordResetCount + ") > 0 ==> AlertDialog");
            AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
            builder.setTitle("Confirm");
            builder.setMessage("Are you sure to send another email?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // leave the method to run
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    forgetPassTextView.setVisibility(View.VISIBLE);
                    return;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        requestPasswordResetCount++;
        Util.userEmail = emailEditText.getText().toString().trim();
        Log.d(TAG, ">>>RUN>>>forgetPassTextViewClickListener");
        Toast.makeText(AuthenticationActivity.this, "SEND REQUEST NUMBER: " + requestPasswordResetCount, Toast.LENGTH_SHORT).show();
         mAuth.sendPasswordResetEmail(Util.userEmail)
                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         if (task.isSuccessful()) {
                             Log.d(TAG, "Reset email sent.");
                             View mainLayoutView = findViewById(R.id.main_layout);
                             //Snackbar .make(mainLayoutView, "Reset email is sent!", Snackbar.LENGTH_SHORT).show();
                             Toast.makeText(AuthenticationActivity.this, "Reset email is sent!", Toast.LENGTH_LONG ).show();
                         }
                     }
                 });
        forgetPassTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Util.isFinished = true;
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public void onResume(){
        if( Util.isFinished ){
            Log.d(TAG," isFinished() = true");
            finish();
        }
        super.onResume();
    }
}
