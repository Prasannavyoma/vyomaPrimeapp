package com.example.vyomalambda;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.jaeger.library.StatusBarUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    ProgressDialog pd;
    JSONObject currentContentJSON;
    VideoView videoView;
    //ArrayList<String> arrayList = new ArrayList<>(Arrays.asList("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"));


    private void setCurrentContentJSON(JSONObject obj) {
        currentContentJSON = obj;
    }

    private void GoVideoView() throws JSONException {
        setContentView(R.layout.activity_playvideo);

        videoView = findViewById(R.id.videoView);
        final MediaController mediacontroller = new MediaController(this);
        mediacontroller.setAnchorView(videoView);

        //TextView tv = findViewById(R.id.txtVideoTop);
        //tv.setText(currentContentJSON.getString("description"));
        makeToast(currentContentJSON.getString("description"));

        videoView.setMediaController(mediacontroller);
        videoView.setVideoURI(Uri.parse(currentContentJSON.getString("file")));
        videoView.requestFocus();
        videoView.showContextMenu();
        videoView.start();

        mediacontroller.show(0);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                makeToast("Complete");


            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("API123", "What " + what + " extra " + extra);
                return false;
            }
        });


    }

    private void GoHome() {
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                // makeToast(userStateDetails.getUserState().toString());

                switch (userStateDetails.getUserState()) {
                    case SIGNED_IN:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        setContentView(R.layout.activity_main);
                                        initHomeView();
                                    }
                                });
                            }
                        });
                        break;
                    case SIGNED_OUT:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GoSignIn();
                            }
                        });
                        break;

                    default:
                        AWSMobileClient.getInstance().signOut();
                        Signout();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("INIT", e.toString());
            }
        });
    }

    private void GoSignUp() {
        runOnUiThread(new Runnable() {
            public void run() {

                setContentView(R.layout.activity_signup);
                final Button btnSignUp = findViewById(R.id.btnSignup);
                btnSignUp.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Signup();
                    }
                });
            }
        });
    }

    private void GoSignIn() {
        runOnUiThread(new Runnable() {
            public void run() {
                setContentView(R.layout.activity_signin);

                final Button buttonSignIn = findViewById(R.id.btnSignIn);

                buttonSignIn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Signin("+91" + getTextVal(R.id.txtSignInName), getTextVal(R.id.txtSigninPassword));
                    }
                });

                final Button buttonSignUp = findViewById(R.id.btnGoSignUp);

                buttonSignUp.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        GoSignUp();
                    }
                });
            }
        });
    }

    private void Signout() {
        AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
            @Override
            public void onResult(final Void result) {
                Log.d("status", "signed-out");
                makeToast("Signed Out Successfully");
                GoSignIn();

            }

            @Override
            public void onError(Exception e) {
                Log.e("status", "sign-out error", e);
                //makeToast(e.getMessage());
                GoSignIn();
            }
        });
    }

    private void Signin(String username, String password) {
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
            }
        });
    }

    private void Signup() {
        final String username = "+91" + ((EditText) findViewById(R.id.txtSignupUsername)).getText().toString();
        final String password = ((EditText)findViewById(R.id.txtSignupPassword)).getText().toString();
        final String cPpassword = ((EditText) findViewById(R.id.txtSignupCPassword)).getText().toString();
        //
        final String emailId = ((EditText) findViewById(R.id.txtSignupEmail)).getText().toString();
        //
        if (!password.equals(cPpassword)) {
            makeToast("Passwords don't match.");
            return;
        }

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("custom:subscription", "free");
        attributes.put("email", emailId);
        AWSMobileClient.getInstance().signUp(username, password, attributes, null, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Sign-up callback state: ", " State");
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            makeToast("Confirm sign-up with: " + details.getDestination());
                        } else {
                            makeToast("Sign-up done.");
                        }
                        GoSignIn();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("Sign-up error", String.valueOf(e));
                String msg = e.getMessage();
                makeToast(msg.substring(0, msg.indexOf("(")));
            }
        });
    }

    private void makeToast(final String s) {

        runOnUiThread(new Runnable() {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            public void run() {
                Toast toast = Toast.makeText(context, s, duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

    }

    private void initHomeView() {
        StatusBarUtil.setTransparent(MainActivity.this);
        imageView = findViewById(R.id.imageView);
        //imageView.setBackground(R.drawable.app_background);
        Picasso.with(MainActivity.this).load("https://vyoma-content-bucket.s3.amazonaws.com/Vyoma+Sample+Products/shankaradigvijaya_slider.jpg").placeholder(R.drawable.ic_launcher_background).into(imageView);

        new ContentGetter().execute("https://vyoma-content-bucket.s3.amazonaws.com/Vyoma+Sample+Products/vyoma_content.json");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //StatusBarUtil.setTransparent(MainActivity.this);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //getSupportActionBar().setHomeButtonEnabled(true);
        GoHome();
    }

    @Override
    public void onBackPressed() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.suspend();
        }
        GoHome();
    }


    private String getTextVal(int viewId) {
        return ((EditText)findViewById(viewId)).getText().toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_home:
                GoHome();
                return true;
            case R.id.menu_logout:
                Signout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // JSON GETTER
    private class ContentGetter extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("Cache-Control", "max-stale=" + 30);
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            try {
                JSONObject resJSON = new JSONObject(result);
                JSONArray categoriesJSON = resJSON.getJSONArray("categories");

                LinearLayout contentLayout = findViewById(R.id.contentLayout);

                for (int i = 0; i < categoriesJSON.length(); i++) {
                    JSONObject categoryJSON = categoriesJSON.getJSONObject(i);
                    JSONArray contentArrayJSON = categoryJSON.getJSONArray("content");

                    // Dummy
                    //TextView dummyTV = new TextView(MainActivity.this);
                    //contentLayout.addView(dummyTV);
                    // Category Name
                    TextView tv = new TextView(MainActivity.this);
                    tv.setText(categoryJSON.getString("name"));
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setTextColor(Color.WHITE);
                    tv.setPadding(2, 10, 2, 4);

                    contentLayout.addView(tv);
                    //Content
                    HorizontalScrollView contentScrollView = new HorizontalScrollView(MainActivity.this);
                    contentLayout.addView(contentScrollView);

                    LinearLayout contentListLayout = new LinearLayout(MainActivity.this);
                    contentScrollView.addView(contentListLayout);


                    for (int j = 0; j < contentArrayJSON.length(); j++) {
                        final JSONObject contentJSON = contentArrayJSON.getJSONObject(j);
                        setCurrentContentJSON(contentJSON);

                        //RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
                        LinearLayout oneContentView = new LinearLayout(MainActivity.this);
                        oneContentView.setPadding(2, 2, 7, 2);
                        oneContentView.setOrientation(LinearLayout.VERTICAL);
                        contentListLayout.addView(oneContentView);
                        //Add Content

                        final ImageView contentView = new ImageView(MainActivity.this);
                        contentView.setLayoutParams(new LinearLayout.LayoutParams(450, 450));
                        oneContentView.addView((contentView));
                        contentView.setPadding(2, 2, 2, 2);
                        contentView.setClickable(true);
                        contentView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                MediaPlayer mp = null;
                                try {
                                    setCurrentContentJSON(contentJSON);
                                    GoVideoView();
                                    /*
                                    Uri u = Uri.parse(contentJSON.getString("file"));
                                    mp = MediaPlayer.create(MainActivity.this, u);
                                    //mp.setDataSource(contentJSON.getString("file"));
                                    mp.start();
                                    */

                                    //makeToast(contentJSON.getString("description"));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    //  mp.stop();
                                }
                            }
                        });
                        Picasso.with(MainActivity.this).load(contentJSON.getString("iconFile")).placeholder(R.drawable.ic_launcher_background).into(contentView);

                        // Spacer
                        View v = new View(MainActivity.this);
                        v.setLayoutParams(new LinearLayout.LayoutParams(445, 7));
                        v.setBackgroundColor(Color.parseColor("#090909"));
                        oneContentView.addView(v);

                        //Add text
                        TextView ctv = new TextView(MainActivity.this);
                        ctv.setText(contentJSON.getString("name"));
                        ctv.setPadding(10, 10, 5, 10);
                        ctv.setBackgroundColor(Color.BLACK);
                        oneContentView.addView(ctv);


                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            //txtJson.setText(result);
        }
    }

    //END JSON GETTER

}


