package com.cartoony.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cartoony.allinonevideo.MyApplication;
import com.cartoony.allinonevideo.R;
import com.cartoony.util.API;
import com.cartoony.util.Constant;
import com.cartoony.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ReportFragment extends BottomSheetDialogFragment {

    public ReportFragment() {

    }

    EditText edtReport;
    Button btnSubmit;
    String postId;
    MyApplication myApplication;
    ProgressDialog pDialog;
    String strMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_report, container, false);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        myApplication = MyApplication.getInstance();
        edtReport = rootView.findViewById(R.id.et_report);
        btnSubmit = rootView.findViewById(R.id.button_report_submit);
        pDialog = new ProgressDialog(getActivity());

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = edtReport.getText().toString();
                if (!comment.isEmpty()) {
                    if (JsonUtils.isNetworkAvailable(requireActivity())) {
                        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
                        jsObj.addProperty("method_name", "user_report");
                        jsObj.addProperty("report", edtReport.getText().toString());
                        jsObj.addProperty("user_id", myApplication.getUserId());
                        jsObj.addProperty("post_id", postId);
                        new MyTaskComment(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                    } else {
                        showToast(getString(R.string.no_connect));
                    }
                }else {
                    showToast(getString(R.string.require_report));
                }
            }
        });

        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTaskComment extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskComment(String base64) {
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
            showToast( strMessage);
            dismiss();
        }
    }
    public void showToast(String msg) {
        Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show();
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


}
