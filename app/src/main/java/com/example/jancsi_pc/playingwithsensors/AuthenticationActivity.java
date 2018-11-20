package com.example.jancsi_pc.playingwithsensors;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

public class AuthenticationActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView selectedEmailTextView;

    private ImageView emailImageView;
    private EditText emailEditText;
    private ImageView deleteEmailImageView;

    private ImageView passwordImageView;
    private EditText passwordEditText;
    private ImageView deletePasswordImageView;

    private ImageView passwordImageView2;
    private EditText passwordEditText2;
    private ImageView deletePasswordImageView2;

    private Button authButton;
    private TextView registerORloginTextView;
    private TextView forgetPassTextView;

    private Button backButton;
    private TextView reportErrorTextView;
    private TextView infoTextView;

    private ConstraintLayout.LayoutParams params;

    private String email = "";
    private String password = "";
    private String password2 = "";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final String TAG = "AuthenticationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        titleTextView = findViewById(R.id.titleTextView);
        selectedEmailTextView= findViewById(R.id.selectedEmailTextView);

        emailImageView = findViewById(R.id.emailImageView);
        emailEditText = findViewById(R.id.emailEditText);
        deleteEmailImageView = findViewById(R.id.deleteEmailImageView);

        passwordImageView = findViewById(R.id.passwordImageView);
        passwordEditText = findViewById(R.id.passwordEditText);
        deletePasswordImageView = findViewById(R.id.deletePasswordImageView);

        passwordImageView2 = findViewById(R.id.passwordImageView2);
        passwordEditText2 = findViewById(R.id.passwordEditText2);
        deletePasswordImageView2 = findViewById(R.id.deletePasswordImageView2);

        authButton = findViewById(R.id.button);
        registerORloginTextView = findViewById(R.id.registerORloginTextView);
        forgetPassTextView = findViewById(R.id.forgetPassTextView);
        infoTextView = findViewById(R.id.infoTextView);

        backButton = findViewById(R.id.backButton);

        forgetPassTextView.setText("Forget password.");

        reportErrorTextView = findViewById(R.id.errorReportTextView);
        reportErrorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","abc@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with authentication.");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        emailImageView.setVisibility(View.VISIBLE);
        passwordImageView.setVisibility(View.VISIBLE);
        passwordImageView2.setVisibility(View.VISIBLE);


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
        /*
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }

        });

        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });

        TextView skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.isSignedIn= true;
                Util.userEmail= "(Guest)" +
                        "";
                finish();
            }
        });
        */

    }


    private void Register() {

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
                            Toast.makeText(AuthenticationActivity.this, getString(R.string.verifyMailbox),Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            //updateUI(null);
                            Toast.makeText(AuthenticationActivity.this, getString(R.string.registerFailed),Toast.LENGTH_LONG).show();
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

        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString(); //TODO ENCODE PASSWORD

        Log.d(TAG, "\nemail=\""+email+"\"" + "\npassword=\""+password+"\"\n");

        if( email == "" ){
            emailEditText.setError("Wrong email");
            emailEditText.requestFocus();
            return;
        }

        if( password.length() < 6 ){
            passwordEditText.setError("At least 6 character!");
            passwordEditText.requestFocus();
            return;
        }

        if(  password == "" ){
            passwordEditText2.setError("Wrong Password!");
            passwordEditText2.requestFocus();
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
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(AuthenticationActivity.this, "Login Failed!", Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }
                        // ...
                    }
                });
    }


    private void prepareScreenUIFor_email(){
        titleTextView.setText("Login");
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText("");
        selectedEmailTextView.setVisibility(View.INVISIBLE);

        emailImageView.setVisibility(View.VISIBLE);
        emailEditText.setVisibility(View.VISIBLE);
        deleteEmailImageView.setVisibility(View.VISIBLE);
        deleteEmailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEditText.setText("");
            }
        });

        passwordImageView.setVisibility(View.INVISIBLE);
        passwordEditText.setVisibility(View.INVISIBLE);
        deletePasswordImageView.setVisibility(View.INVISIBLE);

        passwordImageView2.setVisibility(View.INVISIBLE);
        passwordEditText2.setVisibility(View.INVISIBLE);
        deletePasswordImageView2.setVisibility(View.INVISIBLE);

        authButton.setText("Login");
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailEditText.getText().toString();
                if( ! email.equals("") ){
                    Log.d(TAG,"Go To: PASSWORD_MODE");
                    Util.screenMode = Util.ScreenModeEnum.PASSWORD_MODE;
                    prepareScreenUIFor_password();
                }else{
                    Toast.makeText(AuthenticationActivity.this, "Please fill the Email field!",Toast.LENGTH_LONG).show();
                }
            }
        });
        // params = (ConstraintLayout.LayoutParams) authButton.getLayoutParams();
        // params.startToEnd = emailEditText.getId();
        // authButton.setLayoutParams(params);
        // authButton.requestLayout();

        registerORloginTextView.setText("Create new account.");
        registerORloginTextView.setVisibility(View.VISIBLE);
        registerORloginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Go To: REGISTER_MODE");
                Util.screenMode = Util.ScreenModeEnum.REGISTER_MODE;
                prepareScreenUIFor_register();
            }
        });

        forgetPassTextView.setVisibility(View.VISIBLE);
        forgetPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.sendPasswordResetEmail( Util.userEmail )
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Reset e mail sent.");
                                }
                            }
                        });
            }
        });

        backButton.setVisibility(View.INVISIBLE);
    }


    private void prepareScreenUIFor_password(){
        titleTextView.setText("Login");
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText( email );
        selectedEmailTextView.setVisibility(View.VISIBLE);

        emailImageView.setVisibility(View.VISIBLE);
        emailEditText.setVisibility(View.INVISIBLE);
        deleteEmailImageView.setVisibility(View.INVISIBLE);

        passwordImageView.setVisibility(View.VISIBLE);
        passwordEditText.setVisibility(View.VISIBLE);
        deletePasswordImageView.setVisibility(View.VISIBLE);
        deletePasswordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEditText.setText("");
            }
        });

        passwordImageView2.setVisibility(View.INVISIBLE);
        passwordEditText2.setVisibility(View.INVISIBLE);
        deletePasswordImageView2.setVisibility(View.INVISIBLE);

        authButton.setText("Login");
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = passwordEditText.getText().toString();
                Login();
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
                // TODO SEND EMAIL WITH RESET PASSWORD
            }
        });

        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Go To: EMAIL_MODE");
                Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
                prepareScreenUIFor_email();
            }
        });
    }


    private void prepareScreenUIFor_register(){
        titleTextView.setText("Register");
        titleTextView.setVisibility(View.VISIBLE);

        selectedEmailTextView.setText("");
        selectedEmailTextView.setVisibility(View.INVISIBLE);

        emailImageView.setVisibility(View.VISIBLE);
        emailEditText.setVisibility(View.VISIBLE);
        deleteEmailImageView.setVisibility(View.VISIBLE);
        deleteEmailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailEditText.setText("");
            }
        });


        passwordImageView.setVisibility(View.VISIBLE);
        passwordEditText.setVisibility(View.VISIBLE);
        deletePasswordImageView.setVisibility(View.VISIBLE);
        deletePasswordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEditText.setText("");
            }
        });

        passwordImageView2.setVisibility(View.VISIBLE);
        passwordEditText2.setVisibility(View.VISIBLE);
        deletePasswordImageView2.setVisibility(View.VISIBLE);
        deletePasswordImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEditText2.setText("");
            }
        });

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
                Log.d(TAG,"Go To: EMAIL_MODE");
                Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
                prepareScreenUIFor_email();
            }
        });

        //forgetPassTextView.setText("Forget password.");
        forgetPassTextView.setVisibility(View.INVISIBLE);
        forgetPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO SEND EMAIL WITH RESET PASSWORD
            }
        });

        backButton.setVisibility(View.INVISIBLE);
    }


}
