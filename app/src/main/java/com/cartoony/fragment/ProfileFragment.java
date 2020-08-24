package com.cartoony.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cartoony.allinonevideo.MainActivity;
import com.cartoony.allinonevideo.MyApplication;
import com.cartoony.allinonevideo.R;
import com.cartoony.util.API;
import com.cartoony.util.Constant;
import com.cartoony.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class ProfileFragment extends Fragment implements Validator.ValidationListener {

    @NotEmpty
    EditText edtFullName;
    @NotEmpty
    EditText edtEmail;
    EditText edtPassword;
    EditText edtMobile;
    Button btnSignUp;
    String strName, strEmail, strPassword, strMobile, strMessage, saveType, saveAId;
    private Validator validator;
    ProgressDialog pDialog;
    MyApplication myApp;
    ProgressBar progressBar;
    ScrollView scrollView;
    MyApplication myApplication;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        myApplication = MyApplication.getInstance();
        pDialog = new ProgressDialog(requireActivity());
        myApp = MyApplication.getInstance();
        edtFullName = rootView.findViewById(R.id.edt_name);
        edtEmail = rootView.findViewById(R.id.edt_email);
        edtPassword = rootView.findViewById(R.id.edt_password);
        edtMobile = rootView.findViewById(R.id.edt_phone);
        btnSignUp = rootView.findViewById(R.id.button);
        progressBar = rootView.findViewById(R.id.progressBar);
        scrollView = rootView.findViewById(R.id.lay_scroll);
        if (myApplication.getUserType().equals("Google")) {
            edtEmail.setEnabled(false);
        } else if (myApplication.getUserType().equals("Facebook")) {
            edtEmail.setEnabled(false);
        } else if (myApplication.getUserType().equals("Normal")) {
            edtEmail.setEnabled(true);
        }
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "user_profile");
        jsObj.addProperty("id", myApp.getUserId());
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new getProfile(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }


        validator = new Validator(this);
        validator.setValidationListener(this);

        return rootView;
    }

    @Override
    public void onValidationSucceeded() {
        strName = edtFullName.getText().toString();
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        strMobile = edtMobile.getText().toString();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "user_profile_update");
        jsObj.addProperty("user_id", myApp.getUserId());
        jsObj.addProperty("name", strName);
        jsObj.addProperty("email", strEmail);
        jsObj.addProperty("password", "");
        jsObj.addProperty("phone", strMobile);
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new MyTaskUpdate(API.toBase64(jsObj.toString())).execute(Constant.API_URL);

        }

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getActivity());
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class getProfile extends AsyncTask<String, Void, String> {

        String base64;

        private getProfile(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
            } else {
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        edtFullName.setText(objJson.getString(Constant.USER_NAME));
                        edtEmail.setText(objJson.getString(Constant.USER_EMAIL));
                        edtMobile.setText(objJson.getString(Constant.USER_PHONE));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayData();
            }
        }
    }

    private void displayData() {

    }

    @SuppressLint("StaticFieldLeak")
    private class MyTaskUpdate extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskUpdate(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dismissProgressDialog();

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        strMessage = objJson.getString(Constant.MSG);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }
        }
    }

    public void setResult() {

        if (Constant.GET_SUCCESS_MSG == 0) {
            showToast(getString(R.string.error_title) + "\n" + strMessage);
        } else {
            if (myApplication.getUserType().equals("Google")) {
                saveType = "Google";
            } else if (myApplication.getUserType().equals("Facebook")) {
                saveType = "Facebook";
            } else if (myApplication.getUserType().equals("Normal")) {
                saveType = "Normal";
            }
            myApp.saveLogin(myApp.getUserId(), strName, strEmail, saveType, saveAId);
            showToast(getString(R.string.your_profile_update));
            Intent i = new Intent(requireActivity(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            requireActivity().finish();
        }
    }

    public void showToast(String msg) {
        Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }
}
