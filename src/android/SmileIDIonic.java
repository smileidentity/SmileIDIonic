package cordova.plugin.smileid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.smileidentity.libsmileid.core.RetryOnFailurePolicy;
import com.smileidentity.libsmileid.core.SIDConfig;
import com.smileidentity.libsmileid.core.SIDNetworkRequest;
import com.smileidentity.libsmileid.core.SIDResponse;
import com.smileidentity.libsmileid.exception.SIDException;
import com.smileidentity.libsmileid.model.PartnerParams;
import com.smileidentity.libsmileid.model.SIDMetadata;
import com.smileidentity.libsmileid.model.SIDNetData;
import com.smileidentity.libsmileid.model.SIDUserIdInfo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class echoes a string called from JavaScript.
 */
public class SmileIDIonic extends CordovaPlugin implements SIDNetworkRequest.OnCompleteListener,
  SIDNetworkRequest.OnErrorListener,
  SIDNetworkRequest.OnUpdateListener,
  SIDNetworkRequest.OnEnrolledListener,
  SIDNetworkRequest.OnAuthenticatedListener {
  private int REQUEST_SELFIE_CAPTURE = 777;
  private int REQUEST_ID_CAPTURE = 778;
  private static final String TAG = "SMILE_ID_TAG";
  private static final String RESULT = "SMILE_ID_RESULT";
  private static final String ACTION = "SMILE_ID_ACTION";
  private JSONArray requestArgs;
  private CallbackContext callbackContext;
  private String pendingAction;
  private static final int PERMISSION_ALL = 1;
  SIDNetworkRequest mSINetworkrequest;

  protected String[] PERMISSIONS = {
    Manifest.permission.CAMERA,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE};

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.callbackContext = callbackContext;
    this.requestArgs = args;
    this.pendingAction = action;
    Log.v("JAPHET", args.getString(0));

    if (permissionGranted(PERMISSIONS)) {
      if (action.equals("captureSelfie")) {
        String message = args.getString(0);
        this.captureSelfie(message, callbackContext);
        return true;
      }
      if (action.equals("captureID")) {
        String message = args.getString(0);
        this.captureID(message, callbackContext);
        return true;
      }
      if (action.equals("upload")) {
        JSONObject jobInfo = args.optJSONObject(0);
        JSONObject partnerParams = args.optJSONObject(1);
        JSONObject userIdInfo = args.optJSONObject(2);
        JSONObject sidNetInfo = args.optJSONObject(3);
        this.upload(jobInfo, partnerParams, userIdInfo, sidNetInfo);
        return true;
      }
    } else {
      ActivityCompat.requestPermissions(cordova.getActivity(), PERMISSIONS, PERMISSION_ALL);
      return true;
    }
    return false;
  }

  private void captureSelfie(String tag, CallbackContext callbackContext) {
    if (TextUtils.isEmpty(tag)) {
      callbackContext.error("Please pass a unique tag for SmartSelfie");
    } else {
      final CordovaPlugin that = this;
      Intent intent = new Intent(that.cordova.getActivity().getBaseContext(), SIDSelfieActivity.class);
      intent.putExtra(SIDStringExtras.EXTRA_TAG, tag);
      intent.setPackage(that.cordova.getActivity().getApplicationContext().getPackageName());
      that.cordova.startActivityForResult(that, intent, REQUEST_SELFIE_CAPTURE);
    }
  }

  private void captureID(String tag, CallbackContext callbackContext) {
    if (TextUtils.isEmpty(tag)) {
      callbackContext.error("Please pass a unique tag for SmartSelfie");
    } else {
      final CordovaPlugin that = this;
      Intent intent = new Intent(that.cordova.getActivity().getBaseContext(), SIDIDCardActivity.class);
      intent.putExtra(SIDStringExtras.EXTRA_TAG, tag);
      intent.setPackage(that.cordova.getActivity().getApplicationContext().getPackageName());
      that.cordova.startActivityForResult(that, intent, REQUEST_ID_CAPTURE);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == REQUEST_SELFIE_CAPTURE && this.callbackContext != null) {
      JSONObject obj = new JSONObject();
      try {
        switch (resultCode) {
          case Activity.RESULT_OK:
            obj.put(TAG, intent.getStringExtra(SIDStringExtras.EXTRA_TAG));
            obj.put(RESULT, "success");
            obj.put(ACTION, "SELFIE");
            this.callbackContext.success(obj);
            break;
          case Activity.RESULT_CANCELED:
            obj.put(TAG, intent.getStringExtra(SIDStringExtras.EXTRA_TAG));
            obj.put(RESULT, "cancelled");
            obj.put(ACTION, "SELFIE");
            this.callbackContext.success(obj);
            break;
          default:
            this.callbackContext.error("SELFIE : Unexpected error " + intent.getStringExtra(SIDStringExtras.EXTRA_TAG));
        }
      } catch (JSONException e) {
        this.callbackContext.error("SELFIE : Unexpected error " + intent.getStringExtra(SIDStringExtras.EXTRA_TAG));
      }
    } else if (requestCode == REQUEST_ID_CAPTURE && this.callbackContext != null) {
      JSONObject obj = new JSONObject();
      try {
        switch (resultCode) {
          case Activity.RESULT_OK:
            obj.put(TAG, intent.getStringExtra(SIDStringExtras.EXTRA_TAG));
            obj.put(RESULT, "success");
            obj.put(ACTION, "IDCAPTURE");
            this.callbackContext.success(obj);
            break;
          case Activity.RESULT_CANCELED:
            obj.put(TAG, intent.getStringExtra(SIDStringExtras.EXTRA_TAG));
            obj.put(RESULT, "cancelled");
            obj.put(ACTION, "IDCAPTURE");
            this.callbackContext.success(obj);
            break;
          default:
            this.callbackContext.error("IDCAPTURE : Unexpected error " + intent.getStringExtra(SIDStringExtras.EXTRA_TAG));
        }
      } catch (JSONException e) {
        if (callbackContext != null) {
          this.callbackContext.error("IDCAPTURE : Unexpected error " + intent.getStringExtra(SIDStringExtras.EXTRA_TAG));
        }
      }
    } else {
      if (callbackContext != null) {
        callbackContext.error("Unexpected error ");
      }
    }
  }

  private boolean permissionGranted(String... permissions) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
      for (String permission : permissions) {
        if (ActivityCompat.checkSelfPermission(cordova.getActivity().getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    super.onRequestPermissionResult(requestCode, permissions, grantResults);
    if (permissionGranted(PERMISSIONS)) {
      if (pendingAction.equals("captureSelfie")) {
        String message = requestArgs.getString(0);
        this.captureSelfie(message, callbackContext);
      }
      if (pendingAction.equals("captureID")) {
        String message = requestArgs.getString(0);
        this.captureID(message, callbackContext);
      }
    } else {
      ActivityCompat.requestPermissions(cordova.getActivity(), PERMISSIONS, PERMISSION_ALL);
    }
  }

  private void upload(JSONObject jobInfo, JSONObject partnerParams, JSONObject userIdInfo, JSONObject SIDNetInfo) {
    mSINetworkrequest = new SIDNetworkRequest(cordova.getContext());
    mSINetworkrequest.setOnCompleteListener(this);
    mSINetworkrequest.set0nErrorListener(this);
    mSINetworkrequest.setOnUpdateListener(this);
    mSINetworkrequest.setOnEnrolledListener(this);
    mSINetworkrequest.setOnAuthenticatedListener(this);

    int jobType = -1;
    String tag = null;
    boolean useIdCard = false;
    if (jobInfo.has("tag")) {
      tag = jobInfo.optString("tag");
    } else {
      return;
    }
    if (jobInfo.has("jobType")) {
      jobType = jobInfo.optInt("jobType");
    } else {
      this.callbackContext.error("jobInfo: jobType is required");
      return;
    }

    if (jobInfo.has("useIdCard")) {
      useIdCard = jobInfo.optBoolean("useIdCard");
    }


    SIDMetadata metadata = new SIDMetadata();
    if (partnerParams != null) {
      createPartnerParams(metadata, partnerParams);
    }

    if (userIdInfo == null && (jobType == 1 || jobType == 5)) {
      this.callbackContext.error("userIdInfo is required");
      return;
    }

    if (userIdInfo != null) {
      if (createUserIdInfo(metadata, userIdInfo) == null) {
        return;
      }
    }

    if (SIDNetInfo == null) {
      this.callbackContext.error("SIDNetInfo is required");
      return;
    }
    SIDNetData sidNetData = createSIDNetData(SIDNetInfo);
    if (sidNetData == null) {
      return;
    }
    SIDConfig sidConfig = createConfig(tag, metadata, sidNetData, jobType, useIdCard);


    if (haveNetworkConnection(cordova.getContext())) {
      mSINetworkrequest.submit(sidConfig);
    } else {
      this.callbackContext.error("No internet connection");
    }
  }

  private PartnerParams createPartnerParams(SIDMetadata sidMetadata, JSONObject jsonObject) {
    PartnerParams partnerParams = sidMetadata.getPartnerParams();
    if (jsonObject.has("userId")) {
      partnerParams.setUserId(jsonObject.optString("userId"));
    }
    if (jsonObject.has("jobId")) {
      partnerParams.setJobId(jsonObject.optString("jobId"));
    }
    for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
      String key = it.next();
      if (!key.equals("userId") && !key.equals("jobId")) {
        partnerParams.additionalValue(key, jsonObject.optString(key));
      }
    }
    return partnerParams;
  }

  private SIDUserIdInfo createUserIdInfo(SIDMetadata sidMetadata, JSONObject jsonObject) {
    SIDUserIdInfo sidUserIdInfo = sidMetadata.getSidUserIdInfo();
    List<String> typedValue = new ArrayList<String>() {
      {
        add("country");
        add("countryCode");
        add("idType");
        add("idNumber");
        add("email");
        add("firstName");
        add("lastName");
        add("middleName");
      }
    };
    if (jsonObject.has("country")) {
      sidUserIdInfo.setCountry(jsonObject.optString("country"));
    }
    if (jsonObject.has("countryCode")) {
      sidUserIdInfo.setCountryCode(jsonObject.optString("countryCode"));
    } else {
//      this.callbackContext.error("userIdInfo: countryCode is required");
//      return null;
    }
    if (jsonObject.has("idType")) {
      sidUserIdInfo.setIdType(jsonObject.optString("idType"));
    } else {
//      this.callbackContext.error("userIdInfo: idType is required");
//      return null;
    }
    if (jsonObject.has("idNumber")) {
      sidUserIdInfo.setIdNumber(jsonObject.optString("idNumber"));
    } else {
//      this.callbackContext.error("userIdInfo: idNumber is required");
//      return null;
    }
    if (jsonObject.has("email")) {
      sidUserIdInfo.setEmail(jsonObject.optString("email"));
    }
    if (jsonObject.has("firstName")) {
      sidUserIdInfo.setFirstName(jsonObject.optString("firstName"));
    }
    if (jsonObject.has("lastName")) {
      sidUserIdInfo.setLastName(jsonObject.optString("lastName"));
    }
    if (jsonObject.has("middleName")) {
      sidUserIdInfo.setMiddleName(jsonObject.optString("middleName"));
    }
    for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
      String key = it.next();
      if (!typedValue.contains(key)) {
        sidUserIdInfo.additionalValue(key, jsonObject.optString(key));
      }
    }
    return sidUserIdInfo;
  }

  private SIDNetData createSIDNetData(JSONObject jsonObject) {
    SIDNetData data = new SIDNetData();
    if (jsonObject.has("authUrl")) {
      data.setAuthUrl(jsonObject.optString("authUrl"));
    } else {
      this.callbackContext.error("netData: authUrl is required");
      return null;
    }
    if (jsonObject.has("partnerUrl")) {
      data.setPartnerUrl(jsonObject.optString("partnerUrl"));
    } else {
      this.callbackContext.error("netData: partnerUrl is required");
      return null;
    }
    if (jsonObject.has("partnerPort")) {
      data.setPartnerPort(jsonObject.optString("partnerPort"));
    } else {
      this.callbackContext.error("netData: partnerPort is required");
      return null;
    }
    if (jsonObject.has("lambdaUrl")) {
      data.setLambdaUrl(jsonObject.optString("lambdaUrl"));
    } else {
      this.callbackContext.error("netData: lambdaUrl is required");
      return null;
    }
    if (jsonObject.has("siPort")) {
      data.setSidPort(jsonObject.optString("siPort"));
    } else {
      this.callbackContext.error("netData: siPort is required");
      return null;
    }
    return data;
  }

  private SIDConfig createConfig(String tag, SIDMetadata metadata, SIDNetData sidNetData, int jobType, boolean useIdCard) {

    SIDConfig.Builder builder;
    if (metadata != null) {
      builder = new SIDConfig.Builder(cordova.getContext())
        .setRetryOnfailurePolicy(getRetryOnFailurePolicy())
        .setMode(jobType == 2 ? SIDConfig.Mode.AUTHENTICATION : SIDConfig.Mode.ENROLL)
        .setSmileIdNetData(sidNetData)
        .setSIDMetadata(metadata)
        .setJobType(jobType)
        .useIdCard(useIdCard);

    } else {
      builder = new SIDConfig.Builder(cordova.getContext())
        .setRetryOnfailurePolicy(getRetryOnFailurePolicy())
        .setMode(jobType == 2 ? SIDConfig.Mode.AUTHENTICATION : SIDConfig.Mode.ENROLL)
        .setSmileIdNetData(sidNetData)
        .setJobType(jobType)
        .useIdCard(useIdCard);
    }
    return builder.build(tag);
  }

  private RetryOnFailurePolicy getRetryOnFailurePolicy() {
    RetryOnFailurePolicy retryOnFailurePolicy = new RetryOnFailurePolicy();
    retryOnFailurePolicy.setRetryCount(10);
    retryOnFailurePolicy.setRetryTimeout(TimeUnit.SECONDS.toMillis(15));
    return retryOnFailurePolicy;
  }

  private boolean haveNetworkConnection(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return (cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected());
  }

  @Override
  public void onAuthenticated(SIDResponse sidResponse) {
    Log.v("JAPHET", "onAuthenticated ");
    if (sidResponse != null && sidResponse.getStatusResponse()!=null) {
      callbackContext.success(sidResponse.getStatusResponse().getRawJsonString());
    } else {
      this.callbackContext.error("onAuthenticated error ");
    }
  }

  @Override
  public void onComplete() {
    Log.v("JAPHET", "onComplete");
  }

  @Override
  public void onEnrolled(SIDResponse sidResponse) {
    Log.v("JAPHET", "onEnrolled ");
    if (sidResponse != null && sidResponse.getStatusResponse()!=null) {
      callbackContext.success(sidResponse.getStatusResponse().getRawJsonString());
    } else {
      this.callbackContext.error("onEnrolled error ");
    }
  }

  @Override
  public void onError(SIDException e) {
    Log.v("JAPHET", "onError " + e.getMessage());
    this.callbackContext.error("onError error " + e.getMessage());
  }

  @Override
  public void onUpdate(int i) {
    Log.v("JAPHET", "progress " + i);
  }
}

