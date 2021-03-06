/**
 * Author: Damilare Olowoniyi
 * Mobile application
 * Final year project 
 * */
package com.fitserv.androidapp;

import com.fitserv.app.AppConfig;
import com.fitserv.app.AppController;
import com.fitserv.helper.TrainerSQLiteHandler;
import com.fitserv.helper.TrainerSessionManager;
import com.fitserv.personaltrainer.profilemenu.TrainerProfileActivity;
import java.util.HashMap;
import java.util.Map;
import com.fitserv.androidapp.TrainerLogin;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class PersonalTrainerRegister extends Activity {
	private static final String TAG = PersonalTrainerRegister.class.getSimpleName();
	private Button btnRegister;
	private Button btnLinkToLogin;
	private EditText inputFullName;
	private EditText inputEmail;
	private EditText inputPassword;
	private EditText inputUsername;

	private ProgressDialog pDialog;
	private TrainerSessionManager session;
	private TrainerSQLiteHandler db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personaltrainerregister);

		inputFullName = (EditText) findViewById(R.id.name);
		inputEmail = (EditText) findViewById(R.id.email);
		inputPassword = (EditText) findViewById(R.id.password);
		inputUsername = (EditText) findViewById(R.id.username);

		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);

		// Session manager
		session = new TrainerSessionManager(getApplicationContext());

		// SQLite database handler
		db = new TrainerSQLiteHandler(getApplicationContext());

		// Check if trainer is already logged in or not
		if (session.isLoggedIn()) {
			// Trainer is already logged in. Take him to main activity
			Intent intent = new Intent(PersonalTrainerRegister.this,
					TrainerProfileActivity.class);
			startActivity(intent);
			finish();
		}
		// Register Button Click event
		btnRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String name = inputFullName.getText().toString();
				String email = inputEmail.getText().toString();
				String password = inputPassword.getText().toString();
				String username = inputUsername.getText().toString();

				if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !username.isEmpty()) {
					registerUser(name, username, email, password);
				} else {
					Toast.makeText(getApplicationContext(),
							"Please enter your details!", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		// Link to Login Screen
		btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						TrainerLogin.class);
				startActivity(i);
				finish();
			}
		});

	}

	/**
	 * Function to store trainer in MySQL database will post params(tag, name,
	 * email, password, username) to register url
	 * */
	private void registerUser(final String name,final String username, final String email,
			final String password) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setTitle("Trainer is about get get started");
		pDialog.setMessage("Registering ...Please wait");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_REGISTER_TRAINER, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Register Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {
								// User successfully stored in MySQL
								// Now store the user in sqlite
								String uid = jObj.getString("uid");
 								JSONObject trainer = jObj.getJSONObject("trainer");
								String name = trainer.getString("name");
								String email = trainer.getString("email");
								String created_at = trainer.getString("created_at");
								String username = trainer.getString("username");

								// Inserting row in users table
								db.addTrainer(name, email, uid, created_at, username);

								// Launch login activity
								Intent intent = new Intent(
										PersonalTrainerRegister.this,
										TrainerLogin.class);
								startActivity(intent);
								finish();
							} else {

								// Error occurred in registration. Get the error
								// message
								String errorMsg = jObj.getString("error_msg");
								Toast.makeText(getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Registration Error: " + error.getMessage());
						Toast.makeText(getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						hideDialog();
						error.printStackTrace();
					}
				}) {
 			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "register");
				params.put("name", name);
				params.put("email", email);
				params.put("password", password);
				params.put("username", username);
 				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
}
