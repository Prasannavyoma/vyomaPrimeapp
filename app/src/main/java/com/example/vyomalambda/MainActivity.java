package com.example.vyomalambda;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;


import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private void GoHome()
    {
        runOnUiThread(new Runnable() {
            public void run() {

                setContentView(R.layout.activity_main);
                ImageButton btnSignout = (ImageButton) findViewById(R.id.btnSignout);
                btnSignout.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Signout();
                    }
                });
            }
        });
    }

    private void GoSignIn()
    {
        runOnUiThread(new Runnable() {
            public void run() {
                setContentView(R.layout.activity_signin);

                final Button button = findViewById(R.id.btnSignIn);

                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Signin(getTextVal(R.id.txtSignInName), getTextVal(R.id.txtSigninPassword));
                    }
                });
            }
        });
    }

    private void Signout()
    {
        AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
            @Override
            public void onResult(final Void result) {
                Log.d("status", "signed-out");
                makeToast("Signed Out");
                GoSignIn();

            }

            @Override
            public void onError(Exception e) {
                Log.e("status", "sign-out error", e);
                makeToast(e.getMessage());
                GoSignIn();
            }
        });
    }

    private void Signin(String username, String password)
    {
        AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
            @Override
            public void onResult(final SignInResult signInResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Sign in", ": " + signInResult.getSignInState());
                        switch (signInResult.getSignInState()) {
                            case DONE:
                                makeToast("Sign-in done.");
                                GoHome();
                                break;
                            case SMS_MFA:
                                makeToast("Please confirm sign-in with SMS.");
                                break;
                            case NEW_PASSWORD_REQUIRED:
                                makeToast("Please confirm sign-in with new password.");
                                break;
                            default:
                                makeToast("Unsupported sign-in confirmation: " + signInResult.getSignInState());
                                break;
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                //Log.e("Error", "Sign-in error", e);
                String msg = e.getMessage();
                makeToast(msg.substring(0, msg.indexOf("(") ));
                /*
                setContentView(R.layout.activity_);

                //sign up

                final Button button = findViewById(R.id.btnSignin);

                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Signup();
                    }
                });

                 */

            }
        });
    }

    private void Signup()
    {
        final String username = ((EditText)findViewById(R.id.txtSignupUsername)).getText().toString();
        final String password = ((EditText)findViewById(R.id.txtSignupPassword)).getText().toString();
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("email", "name@email.com");
        AWSMobileClient.getInstance().signUp(username, password, attributes, null, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e( "Sign-up callback state: " + signUpResult.getConfirmationState()," State");
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            makeToast("Confirm sign-up with: " + details.getDestination());
                        } else {
                            makeToast("Sign-up done.");
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("Sign-up error", String.valueOf(e));
            }
        });
    }

    private void makeToast(final String s) {

        runOnUiThread(new Runnable() {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            public void run() {
                Toast toast = Toast.makeText(context, s, duration);
                toast.show();
            }
        });

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Events

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GoHome();
                            }
                        });
                        break;
                    case SIGNED_OUT:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("applog",getApplicationContext().toString());
                                GoSignIn();
                            }
                        });
                        break;

                    default:
                        AWSMobileClient.getInstance().signOut();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("INIT", e.toString());
            }
        });



    }

    private String getTextVal(int viewId) {
        return ((EditText)findViewById(viewId)).getText().toString();
    }
}
