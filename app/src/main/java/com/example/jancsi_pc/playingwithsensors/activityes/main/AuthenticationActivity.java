package com.example.jancsi_pc.playingwithsensors.activityes.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.R;
import com.example.jancsi_pc.playingwithsensors.utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

import java.io.File;
import java.util.Date;

/**
 * Activity that handle the user authentication: log-in, register, forgot password.
 *
 * @author MilleJanos
 */
public class AuthenticationActivity extends AppCompatActivity {

    private final String TAG = "AuthenticationActivity";
    private final long ONE_MEGABYTE = 1024 * 1024;
    private final long ANIMATION_DURATION = 300; //miliseconds
    private TextView appNameTextView;
    private TextView titleTextView;
    private TextView selectedEmailTextView;
    private EditText emailEditText;
    private ImageView deleteOrEditEmailImageView; // edit - same as back button, but more user friendly
    private Drawable DeleteImageDrawable;  // Used for changing the deleteOrEditEmailImageView
    private Drawable EditImageDrawable;    // ImageView content
    private EditText passwordEditText;
    private ImageView deletePasswordImageView;
    private EditText passwordEditText2;
    private ImageView deletePasswordImageView2;
    private Button authButton;
    private TextView registerORloginTextView;
    private TextView forgotPassTextView;
    private Button backButton;
    private TextView reportErrorTextView;
    private TextView infoTextView;
    private TextView auth_offlineValidationTextView;
    private ImageView appLogoImageView;
    private ConstraintLayout.LayoutParams params;
    private String mEmail = "";
    private String mPassword = "";
    private String password2 = "";
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
    // Progress Dialog
    //ProgressDialog Util.progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        Log.d(TAG, ">>>RUN>>>onCreate()");

        // Create the shared progress dialog:
        Util.progressDialog = new ProgressDialog(AuthenticationActivity.this);

        // Set the views of the activity:
        findViewsById();

        // Hide error click listeners (hides the error of the view when is clicked):
        emailEditText.setOnClickListener(v -> emailEditText.setError(null));
        passwordEditText.setOnClickListener(v -> passwordEditText.setError(null));
        passwordEditText2.setOnClickListener(v -> passwordEditText2.setError(null));

        // Error report click listener:
        reportErrorTextView.setOnClickListener(v -> reportError());

        // Small UI settings:
        appNameTextView.setTextColor(R.string.app_name);
        forgotPassTextView.setText(R.string.forgotPassword);
        emailEditText.setText("");
        passwordEditText.setText("");
        passwordEditText2.setText("");
        loadingTextView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        // Offline validation button(TextView):
        auth_offlineValidationTextView = findViewById(R.id.auth_offlineValidationTextView);
        auth_offlineValidationTextView.setOnClickListener(v -> startActivity(new Intent(AuthenticationActivity.this, GaitValidationActivity.class)));

        // Setup/change the Interface:
        switch (Util.screenMode) {

            case EMAIL_MODE: {
                prepareScreenUIForEmail();
                break;
            }
            case PASSWORD_MODE: {
                prepareScreenUIForPassword();
                break;
            }
            case REGISTER_MODE: {
                prepareScreenUIForRegister();
                break;
            }
        }

        // Setting the resource id for the delete/edit imageView
        DeleteImageDrawable = getResources().getDrawable(R.drawable.ic_clear_black);
        EditImageDrawable = getResources().getDrawable(R.drawable.ic_edit_black);
        // usage: imageview.setImageDrawable(<drawable>);

    } // OnCreate

    /**
     * This method makes the registration using the email and
     * password local variables
     */
    private void register() {
        Log.d(TAG, ">>>RUN>>>register()");
        Util.hideKeyboard(AuthenticationActivity.this);

        Util.validatedOnce = false;

        if (!Util.requireInternetConnection(this) /*requireEnabledInternetAndInternetConnection()*/) {
            Util.progressDialog.dismiss();
            return;
        } else {
            Util.hideKeyboard(AuthenticationActivity.this);
            Snackbar.make(findViewById(R.id.auth_main_layout), "No internet connection!", Snackbar.LENGTH_LONG).show();
        }

        mEmail = emailEditText.getText().toString();
        mPassword = passwordEditText.getText().toString().trim();
        password2 = passwordEditText2.getText().toString().trim();

        Log.d(TAG, "\nmEmail=\"" + mEmail + "\"");

        // Catch input errors:
        if (mEmail.equals("")) {
            emailEditText.setError("Wrong mEmail");
            emailEditText.requestFocus();
            Util.progressDialog.dismiss();
            return;
        }

        if (mPassword.equals("")) {
            passwordEditText.setError("Must be filled!");
            passwordEditText.requestFocus();
            Util.progressDialog.dismiss();
            return;
        }

        if (mPassword.length() <= 6) {
            passwordEditText.setError("At least 6 character!");
            passwordEditText.requestFocus();
            Util.progressDialog.dismiss();
            return;
        }

        if (!mPassword.equals(password2)) {
            passwordEditText2.setError("Passwords has to be the same!");
            passwordEditText2.requestFocus();
            Util.progressDialog.dismiss();
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
                            sendVerificationEmail();
                            //Toast.makeText(AuthenticationActivity.this, getString(R.string.verifyMailbox),Toast.LENGTH_LONG).show();
                            Snackbar.make(findViewById(R.id.auth_main_layout), getString(R.string.verifyMailbox), Snackbar.LENGTH_LONG).show();
                            Util.progressDialog.dismiss();
                            finish();
                        } else {
                            Util.progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            //Toast.makeText(AuthenticationActivity.this, getString(R.string.registerFailed),Toast.LENGTH_LONG).show();
                            Snackbar.make(findViewById(R.id.auth_main_layout), getString(R.string.registerFailed), Snackbar.LENGTH_LONG).show();
                        }
                        // ...
                    }

                    private void sendVerificationEmail() {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        user.sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // mEmail sent

                                        // after mEmail is sent just logout the user and finish this activity
                                        FirebaseAuth.getInstance().signOut();
                                        //startActivity(new Intent(AuthenticationActivity.this, DataCollectorActivity.class));
                                        Util.progressDialog.dismiss();
                                        finish();
                                    } else {
                                        // mEmail not sent, so display message and restart the activity or do whatever you wish to do

                                        //restart this activity
                                        overridePendingTransition(0, 0);
                                        Util.progressDialog.dismiss();
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());

                                    }
                                });

                    }
                });
        Log.d(TAG, "<<<FINISH<<<register()");
    }

    /**
     * This method makes the login using the email and
     * password local variables
     */
    private void login() {
        Log.d(TAG, ">>>RUN>>>login()");

        Util.validatedOnce = false;

        authButton.setEnabled(false);

        if (!Util.requireInternetConnection(this) /*requireEnabledInternetAndInternetConnection()*/) {
            Util.progressDialog.dismiss();
            return;
        }

        mEmail = emailEditText.getText().toString();
        mPassword = passwordEditText.getText().toString();

        Log.d(TAG, "mEmail=\"" + mEmail + "\"");

        // Catch input errors:
        if (mEmail.equals("")) {
            Util.progressDialog.dismiss();
            emailEditText.setError("Wrong mEmail");
            emailEditText.requestFocus();
            authButton.setEnabled(true);
            return;
        }

        if (mPassword.equals("")) {
            Util.progressDialog.dismiss();
            passwordEditText2.setError("Wrong Password!");
            passwordEditText2.requestFocus();
            authButton.setEnabled(true);
            return;
        }

        if (mPassword.length() < 6) {
            Util.progressDialog.dismiss();
            passwordEditText.setError("At least 6 character!");
            passwordEditText.requestFocus();
            authButton.setEnabled(true);
            return;
        }

        Util.mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        //FirebaseUser user = Util.mAuth.getCurrentUser();
                        //updateUI(user);
                        Util.userEmail = mEmail;
                        Util.isSignedIn = true;
                        authButton.setEnabled(true);
                        Util.progressDialog.dismiss();

                        checkUserModelAndSmallStuff();// Wait to get the model or create new one, will do the finish() !

                    } else {
                        Util.progressDialog.dismiss();
                        authButton.setEnabled(true);
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        //Toast.makeText(AuthenticationActivity.this, "login Failed!", Toast.LENGTH_LONG).show();
                        Util.hideKeyboard(AuthenticationActivity.this);
                        Snackbar.make(findViewById(R.id.auth_main_layout), "Email or Password is incorrect!", Snackbar.LENGTH_LONG).show();
                    }
                    // ...
                });
        Log.d(TAG, "<<<FINISHED<<<login()");

    }

    /*
     *
     *  Preparing the Authentication View for different login and register:
     *
     */

    /**
     * This method prepares the User Interface for login(email only) view.
     */
    private void prepareScreenUIForEmail() {
        Log.d(TAG, ">>>RUN>>>prepareScreenUIForEmail()");

        // Animations:
        if (passToEmail) {
            handleAnimationPasswordToEmail();
        }

        if (registerToEmail) {
            passwordEditText.setText("");
            handleAnimationRegisterToEmail();
        }
        hangleAnimationSwitchTitle(titleTextView, "login");

        // Remove error marks
        passwordEditText.setError(null);
        passwordEditText2.setError(null);

        // Update UI
        passwordEditText.setText("");
        //titleTextView.setText(R.string.login);    // Added animated text change
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText("");
        selectedEmailTextView.setVisibility(View.INVISIBLE);

        emailEditText.setVisibility(View.VISIBLE);
        deleteOrEditEmailImageView.setVisibility(View.VISIBLE);
        deleteOrEditEmailImageView.setOnClickListener(v -> emailEditText.setText(""));
        deleteOrEditEmailImageView.setImageDrawable(DeleteImageDrawable);

        passwordEditText.setVisibility(View.INVISIBLE);
        deletePasswordImageView.setVisibility(View.INVISIBLE);

        passwordEditText2.setVisibility(View.INVISIBLE);
        deletePasswordImageView2.setVisibility(View.INVISIBLE);

        authButton.setText(R.string.login);
        authButton.setEnabled(true);
        authButton.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>authButtonClickListener");
            authButton.setEnabled(false);

            if (Util.requireInternetConnection(this) /*requireEnabledInternetAndInternetConnection()*/) {            // This method gives feedback using Snackbar
                //Log.d(TAG, " isNetworkEnabled = true");
                //Log.d(TAG, " isNetworkConnection = true");
                mEmail = emailEditText.getText().toString();

                Util.progressDialog = new ProgressDialog(AuthenticationActivity.this, ProgressDialog.STYLE_SPINNER);
                Util.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                Util.progressDialog.setTitle("Authentication");
                Util.progressDialog.setMessage("Checking email.");
                Util.progressDialog.setCancelable(false);
                Util.progressDialog.show();

                Log.d(TAG, "Waiting for fetchProvidersForEmail() ...");
                if (!mEmail.equals("")) {
                    Util.mAuth.fetchProvidersForEmail(mEmail).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Checking to see if user exists in firebase or not");
                            ProviderQueryResult result = task.getResult();

                            if (result != null && result.getProviders() != null && result.getProviders().size() > 0) {
                                Log.d(TAG, "User exists, trying to go further");
                                Log.d(TAG, "Go To: PASSWORD_MODE");
                                emailToPass = true;         //
                                passToEmail = false;        //
                                emailToRegister = false;    // because of animations
                                registerToEmail = false;    //
                                Util.screenMode = Util.ScreenModeEnum.PASSWORD_MODE;
                                prepareScreenUIForPassword();
                                authButton.setEnabled(true);
                                authButton.setEnabled(true);
                                Util.progressDialog.dismiss();
                            } else {
                                userExists = false;
                                emailEditText.setError("Please fill the Email field with a registered email address!");
                                emailEditText.requestFocus();
                                authButton.setEnabled(true);
                                Log.d(TAG, "login user doesn't exist");
                                authButton.setEnabled(true);
                                Util.progressDialog.dismiss();
                            }
                        } else {
                            Log.w(TAG, "User check failed", task.getException());
                            Toast.makeText(AuthenticationActivity.this,
                                    "There is a problem, please try again later.",
                                    Toast.LENGTH_SHORT).show();
                            userExists = false;
                            authButton.setEnabled(true);
                        }
                        //hide progress dialog
                        //hideProgressDialog();
                        //enable and disable login, logout buttons depending on signin status
                        ///showAppropriateOptions();
                    });
                } else {
                    //Toast.makeText(AuthenticationActivity.this, "Please fill the Email field!", Toast.LENGTH_LONG).show();
                    emailEditText.setError("Please fill the Email field with a valid mEmail address!");
                    emailEditText.requestFocus();
                    Util.progressDialog.dismiss();

                }

            } else {
                authButton.setEnabled(true);
                Util.hideKeyboard(AuthenticationActivity.this);
                View view = findViewById(R.id.auth_main_layout);
                Snackbar.make(view, "No internet connection!", Snackbar.LENGTH_SHORT).show();
                Util.progressDialog.dismiss();
            }
        });
        params = (ConstraintLayout.LayoutParams) authButton.getLayoutParams();
        params.topToBottom = emailEditText.getId();
        authButton.setLayoutParams(params);
        authButton.requestLayout();

        registerORloginTextView.setText(R.string.createNewAccount);
        registerORloginTextView.setVisibility(View.VISIBLE);
        registerORloginTextView.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>registerORloginTextViewClickListener");
            Log.d(TAG, "Go To: REGISTER_MODE");
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
            prepareScreenUIForRegister();
        });

        forgotPassTextView.setVisibility(View.VISIBLE);
        forgotPassTextView.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>forgotPassTextViewClickListener");
            resetPassword();
        });

        backButton.setVisibility(View.INVISIBLE);


    }

    /**
     * This method prepares the User Interface for login(password only) view.
     */
    private void prepareScreenUIForPassword() {
        Log.d(TAG, ">>>RUN>>>prepareScreenUIForPassword()");

        // Animations
        if (emailToPass) {  // in current state there is only this way
            handleAnimationEmailToPassword();
        }
        hangleAnimationSwitchTitle(titleTextView, "login - Step 2");

        // FILL DEBUG EMAILS:   // TODO: REMOVE THIS PASS AUTOFILL
        if (mEmail.equals("millejanos31@gmail.com") || mEmail.equals("wolterwill31@gmail.com")) {
            passwordEditText.setText("01234567");
        }

        // Remove error marks
        emailEditText.setError(null);
        passwordEditText.setError(null);
        passwordEditText2.setError(null);

        // Update UI
        titleTextView.setText(R.string.login);
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText(mEmail);
        selectedEmailTextView.setVisibility(View.VISIBLE);

        emailEditText.setVisibility(View.INVISIBLE);
        deleteOrEditEmailImageView.setVisibility(View.VISIBLE);
        deleteOrEditEmailImageView.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>editEmailImageViewClickListener");
            Log.d(TAG, "Go To: EMAIL_MODE");
            emailToPass = false;        //
            passToEmail = true;         //
            emailToRegister = false;    // because of animations
            registerToEmail = false;    //
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            prepareScreenUIForEmail();
        });
        deleteOrEditEmailImageView.setImageDrawable(EditImageDrawable);

        passwordEditText.setVisibility(View.VISIBLE);
        deletePasswordImageView.setVisibility(View.VISIBLE);
        deletePasswordImageView.setOnClickListener(v -> passwordEditText.setText(""));

        passwordEditText2.setVisibility(View.INVISIBLE);
        deletePasswordImageView2.setVisibility(View.INVISIBLE);

        params = (ConstraintLayout.LayoutParams) authButton.getLayoutParams();
        params.topToBottom = passwordEditText.getId();
        //authButton.startAnimation(alphaAnimation);
        authButton.setLayoutParams(params);
        authButton.requestLayout();
        authButton.setText(R.string.login);
        authButton.setOnClickListener(v -> {

            // Finishing login
            Log.d(TAG, ">>>RUN>>>authButtonClickListener");
            if (Util.requireInternetConnection(this) /*requireEnabledInternetAndInternetConnection()*/) {            // This method gives feedback using Snackbar
                mPassword = passwordEditText.getText().toString();

                Util.progressDialog = new ProgressDialog(AuthenticationActivity.this, ProgressDialog.STYLE_SPINNER);
                Util.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                Util.progressDialog.setTitle("Authentication");
                Util.progressDialog.setMessage("Logging in.\nPlease wait.");
                Util.progressDialog.setCancelable(false);
                Util.progressDialog.show();

                login();        // will dismiss the Util.progressDialog

            } else {
                authButton.setEnabled(true);
                Util.hideKeyboard(AuthenticationActivity.this);
                View view = findViewById(R.id.auth_main_layout);
                Snackbar.make(view, "No internet connection!", Snackbar.LENGTH_SHORT).show();
            }
        });

        //registerORloginTextView.setText("Back."); //Van Back gomb bal oldalt fent
        registerORloginTextView.setVisibility(View.INVISIBLE);
        //registerORloginTextView.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Log.d(TAG,"Go To: EMAIL_MODE");
        //        Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
        //        prepareScreenUIForEmail();
        //    }
        //});

        forgotPassTextView.setText(R.string.forgotPassword);
        forgotPassTextView.setVisibility(View.VISIBLE);
        forgotPassTextView.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>forgotPassTextViewClickListener");
            resetPassword();
        });

        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>backButtonClickListener");
            Log.d(TAG, "Go To: EMAIL_MODE");
            emailToPass = false;        //
            passToEmail = true;         //
            emailToRegister = false;    // because of animations
            registerToEmail = false;    //
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            prepareScreenUIForEmail();
        });
        //Animation:

        // AlphaAnimation alphaAnimation = new AlphaAnimation(0.2f, 1.0f);
        // alphaAnimation.setDuration(300);
        // alphaAnimation.setFillBefore(true);
        // alphaAnimation.setFillAfter(false);

        Util.isAdminLoggedIn = false;
        for (String e : Util.adminList) {
            if (e.equals(mEmail)) {
                Util.isAdminLoggedIn = true;
                break;
            }
        }
        if (Util.isAdminLoggedIn) {
            Log.i(TAG, "Util.isAdminLoggedIn -> true");
        } else {
            Log.i(TAG, "Util.isAdminLoggedIn -> false");
        }

    }

    /**
     * This method prepares the User Interface for Registration view.
     */
    private void prepareScreenUIForRegister() {
        Log.d(TAG, ">>>RUN>>>prepareScreenUIForRegister()");

        // Animation
        if (emailToRegister) {
            handleAnimationEmailToRegister();
        }
        hangleAnimationSwitchTitle(titleTextView, "register");

        // Remove error marks
        emailEditText.setError(null);
        passwordEditText.setError(null);
        passwordEditText2.setError(null);

        // Update UI
        passwordEditText.setText("");
        //titleTextView.setText(R.string.register); // Added animated Text Change
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText("");
        selectedEmailTextView.setVisibility(View.INVISIBLE);

        emailEditText.setVisibility(View.VISIBLE);
        deleteOrEditEmailImageView.setVisibility(View.VISIBLE);
        deleteOrEditEmailImageView.setOnClickListener(v -> emailEditText.setText(""));
        deleteOrEditEmailImageView.setImageDrawable(DeleteImageDrawable);


        passwordEditText.setText("");
        passwordEditText.setVisibility(View.VISIBLE);
        deletePasswordImageView.setVisibility(View.VISIBLE);
        deletePasswordImageView.setOnClickListener(v -> passwordEditText.setText(""));

        passwordEditText2.setText("");
        passwordEditText2.setVisibility(View.VISIBLE);
        deletePasswordImageView2.setVisibility(View.VISIBLE);
        deletePasswordImageView2.setOnClickListener(v -> passwordEditText2.setText(""));

        params = (ConstraintLayout.LayoutParams) authButton.getLayoutParams();
        params.topToBottom = passwordEditText2.getId();
        authButton.setLayoutParams(params);
        authButton.requestLayout();

        authButton.setText(R.string.register);
        authButton.setOnClickListener(v -> {
            if (emailEditText.getText().toString().trim().equals("")) {
                emailEditText.setError("This field must be filled!");
                if (Util.progressDialog.isShowing()) {
                    Util.progressDialog.dismiss();
                }
                return;
            }
            if (passwordEditText.getText().toString().trim().equals("")) {
                passwordEditText.setError("This field must be filled!");
                if (Util.progressDialog.isShowing()) {
                    Util.progressDialog.dismiss();
                }
                return;
            }
            if (passwordEditText.getText().toString().trim().length() < 6) {
                passwordEditText.setError("Passwords has to be at least 6 characters!");
                if (Util.progressDialog.isShowing()) {
                    Util.progressDialog.dismiss();
                }
                return;
            }
            if (passwordEditText2.getText().toString().trim().equals("")) {
                passwordEditText2.setError("This field must be filled!");
                if (Util.progressDialog.isShowing()) {
                    Util.progressDialog.dismiss();
                }
                return;
            }
            if (!passwordEditText.getText().toString().trim().equals(passwordEditText2.getText().toString().trim())) {
                passwordEditText2.setError("The passwords must be the same!");
                if (Util.progressDialog.isShowing()) {
                    Util.progressDialog.dismiss();
                }
                return;
            }
            authButton.setEnabled(false);

            mEmail = emailEditText.getText().toString();
            if (!mEmail.equals("")) {

                if (Util.requireInternetConnection(this) /*requireEnabledInternetAndInternetConnection()*/) {

                    Util.progressDialog = new ProgressDialog(AuthenticationActivity.this, ProgressDialog.STYLE_SPINNER);
                    Util.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    Util.progressDialog.setTitle("Authentication");
                    Util.progressDialog.setMessage("Creating new user.\nPlease wait.");
                    Util.progressDialog.setCancelable(false);
                    Util.progressDialog.show();

                    Util.mAuth.fetchProvidersForEmail(mEmail).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "checking to see if user exists in firebase or not");
                            ProviderQueryResult result = task.getResult();

                            if (result != null && result.getProviders() != null && result.getProviders().size() > 0) {
                                Log.d(TAG, "User exists, stopping");
                                userExists = true;
                                Toast.makeText(AuthenticationActivity.this, "Email already registered!", Toast.LENGTH_LONG).show();
                            } else {
                                Log.d(TAG, "User doesn't exist ==> register");
                                //Toast.makeText(AuthenticationActivity.this, "Please fill the Email field!", Toast.LENGTH_LONG).show();
                                userExists = false;
                                mPassword = passwordEditText.getText().toString().trim();
                                register();
                            }
                        } else {
                            Log.w(TAG, "User check failed", task.getException());
                            Toast.makeText(AuthenticationActivity.this,
                                    "There is a problem, please try again later.",
                                    Toast.LENGTH_SHORT).show();
                            userExists = false;
                            mPassword = passwordEditText.getText().toString().trim();
                            register();     // will dismiss the Util.progressDialog
                        }
                        authButton.setEnabled(true);

                    });

                } else {
                    authButton.setEnabled(true);
                    Util.hideKeyboard(AuthenticationActivity.this);
                    View view = findViewById(R.id.auth_main_layout);
                    Snackbar.make(view, "No internet connection!", Snackbar.LENGTH_SHORT).show();
                }

            } else {
                if (Util.progressDialog.isShowing()) {
                    Util.progressDialog.dismiss();
                }
            }
                /*if(userExists){
                    Toast.makeText(AuthenticationActivity.this, "Email already registered!", Toast.LENGTH_LONG).show();
                    return;
                }
                mPassword = passwordEditText.getText().toString().trim();
                register();
                */

        });

        registerORloginTextView.setText(R.string.alreadyHaveAccount);
        registerORloginTextView.setVisibility(View.VISIBLE);
        registerORloginTextView.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>registerORloginTextViewClickListener");
            Log.d(TAG, "Go To: EMAIL_MODE");
            emailToPass = false;        //
            passToEmail = false;         //
            emailToRegister = false;    // because of animations
            registerToEmail = true;    //
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            prepareScreenUIForEmail();
        });

        //forgotPassTextView.setText("forgot password.");
        forgotPassTextView.setVisibility(View.INVISIBLE);
        forgotPassTextView.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>forgotPassTextViewClickListener");
            if (Util.requireInternetConnection(this) /*requireEnabledInternetAndInternetConnection()*/) {

                resetPassword();

            } else {
                Util.hideKeyboard(AuthenticationActivity.this);
                View view = findViewById(R.id.auth_main_layout);
                Snackbar.make(view, "No internet connection!", Snackbar.LENGTH_SHORT).show();
            }
        });

        backButton.setVisibility(View.INVISIBLE);
    }

    /*
     *
     *  Animations:
     *
     */

    private void handleAnimationEmailToPassword(/*View v*/) {
        float distanceY = TypedValue.applyDimension(         // dip to pixels
                TypedValue.COMPLEX_UNIT_DIP, 50,
                getResources().getDisplayMetrics()
        );
        // Translate:
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -distanceY, 0);
        translateAnimation.setDuration(ANIMATION_DURATION);

        // Alpha:
        Animation alphaAnimHide = new AlphaAnimation(1f, 0f);   // hiding
        alphaAnimHide.setDuration(ANIMATION_DURATION);
        alphaAnimHide.setStartOffset(0);
        alphaAnimHide.setFillAfter(true);

        Animation alphaAnimShow = new AlphaAnimation(0f, 1f);   // showing
        alphaAnimShow.setDuration(ANIMATION_DURATION);
        alphaAnimShow.setStartOffset(0);
        alphaAnimShow.setFillAfter(true);

        // Animation Set: (Translate+Alpha)
        AnimationSet as1 = new AnimationSet(false);
        as1.addAnimation(translateAnimation);
        as1.addAnimation(alphaAnimShow);
        AnimationSet as2 = new AnimationSet(false);
        as2.addAnimation(translateAnimation);
        as2.addAnimation(alphaAnimHide);

        // Start Animations:
        authButton.setAnimation(translateAnimation);
        passwordEditText.setAnimation(as1);
        deletePasswordImageView.setAnimation(as1);
        registerORloginTextView.setAnimation(as2);
        forgotPassTextView.setAnimation(translateAnimation);

    }

    private void handleAnimationEmailToRegister(/*View v*/) {
        float distanceY = TypedValue.applyDimension(         // dip to pixels
                TypedValue.COMPLEX_UNIT_DIP, 50,
                getResources().getDisplayMetrics()
        );
        TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, -distanceY, 0);
        translateAnimation1.setDuration(ANIMATION_DURATION);
        TranslateAnimation translateAnimation2 = new TranslateAnimation(0, 0, -2 * distanceY, 0);
        translateAnimation2.setDuration(ANIMATION_DURATION);
        TranslateAnimation translateAnimation3 = new TranslateAnimation(0, 0, -3 * distanceY, 0);
        translateAnimation3.setDuration(ANIMATION_DURATION);

        // Alpha:
        Animation alphaAnimHide = new AlphaAnimation(1f, 0f);   // hiding
        alphaAnimHide.setDuration(ANIMATION_DURATION);
        alphaAnimHide.setStartOffset(0);
        alphaAnimHide.setFillAfter(true);

        Animation alphaAnimShow = new AlphaAnimation(0f, 1f);   // showing
        alphaAnimShow.setDuration(ANIMATION_DURATION);
        alphaAnimShow.setStartOffset(0);
        alphaAnimShow.setFillAfter(true);

        // Animation Set: (Translate+Alpha)
        AnimationSet as1 = new AnimationSet(false);
        as1.addAnimation(translateAnimation1);
        as1.addAnimation(alphaAnimShow);
        AnimationSet as2 = new AnimationSet(false);
        as2.addAnimation(translateAnimation2);
        as2.addAnimation(alphaAnimShow);
        AnimationSet as3 = new AnimationSet(false);
        as3.addAnimation(translateAnimation2);
        as3.addAnimation(alphaAnimHide);

        // Start Animations:
        authButton.setAnimation(translateAnimation2);
        passwordEditText.setAnimation(as1);
        deletePasswordImageView.setAnimation(as1);
        passwordEditText2.setAnimation(as2);
        deletePasswordImageView2.setAnimation(as2);
        registerORloginTextView.setAnimation(translateAnimation2);
        forgotPassTextView.setAnimation(as3);

    }

    private void handleAnimationPasswordToEmail(/*View v*/) {
        float distanceY = TypedValue.applyDimension(         // dip to pixels
                TypedValue.COMPLEX_UNIT_DIP, 50,
                getResources().getDisplayMetrics()
        );
        // Translate:
        TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, distanceY, 0);
        translateAnimation1.setDuration(ANIMATION_DURATION);
        TranslateAnimation translateAnimation2 = new TranslateAnimation(0, 0, 0, -distanceY);
        translateAnimation2.setDuration(ANIMATION_DURATION);

        // Alpha:
        Animation alphaAnimHide = new AlphaAnimation(1f, 0f);   // hiding
        alphaAnimHide.setDuration(ANIMATION_DURATION);
        alphaAnimHide.setStartOffset(0);
        alphaAnimHide.setFillAfter(true);

        Animation alphaAnimShow = new AlphaAnimation(0f, 1f);   // showing
        alphaAnimShow.setDuration(ANIMATION_DURATION);
        alphaAnimShow.setStartOffset(0);
        alphaAnimShow.setFillAfter(true);

        // Animation Set: (Translate+Alpha)
        AnimationSet as1 = new AnimationSet(false);
        as1.addAnimation(translateAnimation1);
        as1.addAnimation(alphaAnimHide);
        AnimationSet as2 = new AnimationSet(false);
        as2.addAnimation(translateAnimation1);
        as2.addAnimation(alphaAnimShow);

        // Start Animations:
        selectedEmailTextView.setAnimation(alphaAnimHide);
        emailEditText.setAnimation(alphaAnimShow);
        authButton.setAnimation(translateAnimation1);
        passwordEditText.setAnimation(translateAnimation2);
        deletePasswordImageView.setAnimation(translateAnimation2);
        registerORloginTextView.setAnimation(as2);
        forgotPassTextView.setAnimation(translateAnimation1);
    }

    private void handleAnimationRegisterToEmail(/*View v*/) {
        float distanceY = TypedValue.applyDimension(         // dip to pixels
                TypedValue.COMPLEX_UNIT_DIP, 50,
                getResources().getDisplayMetrics()
        );
        // Translate:
        Animation translateAnimation1 = new TranslateAnimation(0, 0, 0, -1 * distanceY);
        translateAnimation1.setDuration(ANIMATION_DURATION);
        Animation translateAnimation2 = new TranslateAnimation(0, 0, 0, -2 * distanceY);
        translateAnimation2.setDuration(ANIMATION_DURATION);
        Animation translateAnimation3 = new TranslateAnimation(0, 0, 2 * distanceY, 0);
        translateAnimation3.setDuration(ANIMATION_DURATION);

        // Alpha:
        Animation alphaAnimHide = new AlphaAnimation(1f, 0f);   // hiding
        alphaAnimHide.setDuration(ANIMATION_DURATION);
        alphaAnimHide.setStartOffset(0);
        alphaAnimHide.setFillAfter(true);

        Animation alphaAnimShow = new AlphaAnimation(0f, 1f);   // showing
        alphaAnimShow.setDuration(ANIMATION_DURATION);
        alphaAnimShow.setStartOffset(0);
        alphaAnimShow.setFillAfter(true);

        // Animation Set: (Translate+Alpha)
        AnimationSet as1 = new AnimationSet(false);
        as1.addAnimation(translateAnimation1);
        as1.addAnimation(alphaAnimHide);
        AnimationSet as2 = new AnimationSet(false);
        as2.addAnimation(translateAnimation2);
        as2.addAnimation(alphaAnimHide);
        AnimationSet as3 = new AnimationSet(false);
        as3.addAnimation(translateAnimation2);
        as3.addAnimation(alphaAnimShow);
        AnimationSet as4 = new AnimationSet(false);
        as4.addAnimation(translateAnimation3);
        as4.addAnimation(alphaAnimShow);

        // Start Animations:
        passwordEditText.startAnimation(as1);
        deletePasswordImageView.startAnimation(as1);
        passwordEditText2.setAnimation(as2);
        deletePasswordImageView2.setAnimation(as2);
        authButton.setAnimation(translateAnimation3);
        registerORloginTextView.setAnimation(translateAnimation3);
        forgotPassTextView.setAnimation(as4);
    }

    private void hangleAnimationSwitchTitle(View view, String new_title) {

        float distanceX = TypedValue.applyDimension(         // dip to pixels
                TypedValue.COMPLEX_UNIT_DIP, 300,
                getResources().getDisplayMetrics()
        );
        // Translate:
        Animation translateAnimationOldOut = new TranslateAnimation(0, -distanceX, 0, 0);  // hiding animation
        translateAnimationOldOut.setDuration(ANIMATION_DURATION / 2);
        Animation translateAnimationNewIn = new TranslateAnimation(-distanceX, 0, 0, 0);   // showing animation
        translateAnimationNewIn.setDuration(ANIMATION_DURATION / 2);

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            translateAnimationOldOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // After the hiding animation finished, start the showing one
                    textView.setText(new_title);
                    textView.setAnimation(translateAnimationNewIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // do nothing
                }
            });
            textView.startAnimation(translateAnimationOldOut);
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            // do what you want with textView
            // TODO: imageView switch - maybe in a later version
        }
    }

    private void handleAnimationAppLogoIntro() {
        float distanceY = TypedValue.applyDimension(         // dip to pixels
                TypedValue.COMPLEX_UNIT_DIP, 45,
                getResources().getDisplayMetrics()
        );
        // Translate:
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, distanceY, 0);
        translateAnimation.setDuration(ANIMATION_DURATION);

        // Scale
        Animation scaleAnimation = new ScaleAnimation(
                2f, 1f, // Start and end values for the X axis scaling
                2f, 1f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        scaleAnimation.setFillAfter(true); // Needed to keep the result of the animation
        scaleAnimation.setDuration(ANIMATION_DURATION);

        // Animation Set: (Translate+Alpha)
        AnimationSet as = new AnimationSet(false);
        as.addAnimation(translateAnimation);
        as.addAnimation(scaleAnimation);

        // Start Animations:
        appLogoImageView.setAnimation(as);
    }

    /**
     * This method sets the view variables in Authentication activity.
     */
    private void findViewsById() {
        appNameTextView = findViewById(R.id.auth_appNameTextView);
        titleTextView = findViewById(R.id.auth_titleTextView);
        selectedEmailTextView = findViewById(R.id.auth_selectedEmailTextView);
        emailEditText = findViewById(R.id.auth_emailEditText);
        deleteOrEditEmailImageView = findViewById(R.id.auth_deleteOrEditEmailImageView);
        passwordEditText = findViewById(R.id.auth_passwordEditText);
        deletePasswordImageView = findViewById(R.id.auth_deletePasswordImageView);
        passwordEditText2 = findViewById(R.id.auth_passwordEditText2);
        deletePasswordImageView2 = findViewById(R.id.auth_deletePasswordImageView2);
        authButton = findViewById(R.id.auth_button);
        registerORloginTextView = findViewById(R.id.auth_registerORloginTextView);
        forgotPassTextView = findViewById(R.id.auth_forgotPassTextView);
        infoTextView = findViewById(R.id.auth_infoTextView);
        backButton = findViewById(R.id.auth_backButton);
        appLogoImageView = findViewById(R.id.auth_AppIconImageView);

        reportErrorTextView = findViewById(R.id.auth_errorReportTextView);
        progressBar = findViewById(R.id.auth_progressBar);
        loadingTextView = findViewById(R.id.auth_loadingCompleteTextView);
    }

    /*
     *
     *  Common used methods:
     *
     */

    /**
     * Sends reset password to currently logged in user.
     */
    private void resetPassword() {
        Log.d(TAG, ">>>RUN>>>resetPassword()");
        forgotPassTextView.setVisibility(View.INVISIBLE);
        mEmail = emailEditText.getText().toString().trim();
        if (mEmail.equals("")) {
            Log.d(TAG, ">>>RUN>>>Email field is empty ==> \"please fill it\"");
            emailEditText.setError("Type your mEmail before mPassword request.");
            //emailEditText.requestFocus();
            forgotPassTextView.setVisibility(View.VISIBLE);
            return;
        } else {
            Log.d(TAG, "Waiting for fetchProvidersForEmail() ...");
            Util.mAuth.fetchProvidersForEmail(mEmail).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "checking to see if user exists in firebase or not");
                        ProviderQueryResult result = task.getResult();

                        if (result != null && result.getProviders() != null && result.getProviders().size() > 0) {
                            Log.d(TAG, "User exists, trying to go further");
                            userExists = true;
                        } else {
                            Log.d(TAG, "User doesn't exist");
                            //Toast.makeText(AuthenticationActivity.this, "Please fill the Email field!", Toast.LENGTH_LONG).show();
                            emailEditText.setError("Please fill the Email field with a registered email address!");
                            emailEditText.requestFocus();
                            userExists = false;
                        }
                    } else {
                        Log.w(TAG, "User check failed", task.getException());
                        Toast.makeText(AuthenticationActivity.this,
                                "There is a problem, please try again later.",
                                Toast.LENGTH_SHORT).show();
                        userExists = false;
                    }
                }
            });
        }

        if (!userExists) {
            forgotPassTextView.setVisibility(View.VISIBLE);
            return;
        }

        Util.userEmail = emailEditText.getText().toString().trim();
        mEmail = Util.userEmail;

        if (requestPasswordResetCount == 0) {
            // First mPassword reset request
            Log.d(TAG, "requestPasswordResetCount(" + requestPasswordResetCount + ") > 0 ==> AlertDialog");

            Util.mAuth.sendPasswordResetEmail(Util.userEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Reset mPassword request sent.");
                            //View mainLayoutView = findViewById(R.id.auth_main_layout);
                            //Snackbar.make(mainLayoutView, "Reset mEmail is sent!", Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(AuthenticationActivity.this, "Reset mPassword request was sent!", Toast.LENGTH_LONG).show();
                        }
                    });
            requestPasswordResetCount++;

        } else {
            // If the user tries to send mPassword reset multiple times in a row
            Log.d(TAG, "requestPasswordResetCount(" + requestPasswordResetCount + ") > 0 ==> AlertDialog");

            AlertDialog.Builder builder = new AlertDialog.Builder(AuthenticationActivity.this);
            builder.setTitle("Confirm");
            builder.setMessage("Are you sure you want to send a reset request?");
            builder.setPositiveButton("YES", (dialog, which) -> {
                // leave the method to run
                //Toast.makeText(AuthenticationActivity.this, "SEND REQUEST NUMBER: " + requestPasswordResetCount, Toast.LENGTH_SHORT).show();
                Util.mAuth.sendPasswordResetEmail(Util.userEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Reset mPassword request sent.");
                                //View mainLayoutView = findViewById(R.id.auth_main_layout);
                                //Snackbar.make(mainLayoutView, "Reset mEmail is sent!", Snackbar.LENGTH_SHORT).show();
                                Toast.makeText(AuthenticationActivity.this, "Reset mPassword request was sent!", Toast.LENGTH_LONG).show();
                            }
                        });
                requestPasswordResetCount++;
            });
            builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
            AlertDialog alert = builder.create();
            alert.show();


        }


        forgotPassTextView.setVisibility(View.VISIBLE);
    }

    /**
     * This method reports an error customized by user.
     */
    private void reportError() {
        Log.d(TAG, ">>>RUN>>>reportErrorTextViewClickListener");
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "abc@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with authentication.");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
        startActivity(Intent.createChooser(emailIntent, "Send mEmail..."));
    }

    /**
     * This method downloads the user model (if user has one).
     */
    private void checkUserModelAndSmallStuff() {
        // Test user model existence in firebase
        // AFTER signInWithEmailAndPassword is succed !
        Log.d(TAG, ">>>RUN>>>checkUserModelAndSmallStuff()");

        Util.mRef = Util.mStorage.getReference().child("models/model_" + Util.mAuth.getUid() + ".mdl");

        Log.d(TAG, "Util.mRef= Util.mStorage.getReference().child(models/model_" + Util.mAuth.getUid() + ".mdl)");
        Log.d(TAG, "Util.mRef= " + Util.mRef);
        Log.d(TAG, "Util.mRef.toString()= " + Util.mRef.toString());


        loadingTextView.setVisibility(View.VISIBLE);
        loadingTextView.setText(R.string.downloading_model);
        progressBar.setVisibility(View.VISIBLE);

        final File localFile;
        try {
            //localFile = new File("/storage/emulated/0/logmein","model.mdl");
            localFile = File.createTempFile("model_", ".mdl");
            Util.mRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                // Local temp file has been created
                Util.hasUserModel = true;
                Util.isSetUserModel = true;
                Log.i(TAG, "### Util.hasUserModel = true;");
                Log.i(TAG, "MODEL FOUND: Local File Path: " + localFile.getAbsolutePath());
                Log.i(TAG, "<<<finish()<<<");
                loadingTextView.setText(R.string.downloaded);
                Util.progressDialog.dismiss();
                Util.mAuth = FirebaseAuth.getInstance();
                finish();
            }).addOnFailureListener(e -> {
                Util.hasUserModel = false;
                Util.isSetUserModel = true;
                Log.i(TAG, "### Util.hasUserModel = false;");
                Log.i(TAG, "MODEL NOT FOUND: ERROR: getFile()");
                Log.i(TAG, "<<<finish()<<<");
                //e.printStackTrace();
                loadingTextView.setText(R.string.downloaded);
                Util.progressDialog.dismiss();
                Util.mAuth = FirebaseAuth.getInstance();
                finish();
            }).addOnProgressListener(taskSnapshot -> {
                final double process = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                //progressDialog.setMessage("Downloaded: " + (int) process + "%");
                mHandler.post(() -> progressBar.setProgress((int) process));
            });
            /*mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"mHandler.post() --> Finish()");
                    loadingTextView.setVisibility(View.VISIBLE);
                }
            });*/
        } catch (Exception e) {
            Util.progressDialog.dismiss();
            Util.hasUserModel = false;
            Util.isSetUserModel = true;
            Log.i(TAG, "### Util.hasUserModel = false;");
            Log.e(TAG, "ERROR: MODEL: EXCEPTION !");
            e.printStackTrace();
            finish();
        }

        Log.d(TAG, "<<<FINISHED<<<checkUserModelAndSmallStuff()");
    }

    /*
     *
     * Activity methods:
     *
     */

    @Override
    public void onResume() {

        Util.isSetUserModel = false;

        if (Util.isFinished) {
            Log.d(TAG, " isFinished() = true");
            finish();
        }

        Date date = new Date();
        CharSequence s = DateFormat.format("yyyyMMdd_HHmmss", date.getTime());
        Util.mSharedPrefEditor.putString(Util.LAST_LOGGED_IN_DATE_KEY, s.toString());
        Util.mSharedPrefEditor.apply();

        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Animate Logo
        handleAnimationAppLogoIntro();
    }

    @Override
    public void onBackPressed() {
        // back button pressed:
        //  if EMAIL_MODE ==> exit app
        //  if PASSWORD_MODE ==> EMAIL_MODE
        //  if REGISTER_MODE ==> EMAIL_MODE
        if (Util.screenMode == Util.ScreenModeEnum.EMAIL_MODE) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                Util.isFinished = true;
                finish();
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
        if (Util.screenMode == Util.ScreenModeEnum.PASSWORD_MODE) {
            Log.d(TAG, "Go To: EMAIL_MODE");
            emailToPass = false;        //
            passToEmail = true;         //
            emailToRegister = false;    // because of animations
            registerToEmail = false;    //
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            prepareScreenUIForEmail();
        }
        if (Util.screenMode == Util.ScreenModeEnum.REGISTER_MODE) {
            Log.d(TAG, "Go To: EMAIL_MODE");
            emailToPass = false;        //
            passToEmail = false;        //
            emailToRegister = false;    // because of animations
            registerToEmail = true;    //
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            prepareScreenUIForEmail();
        }
    }

}
