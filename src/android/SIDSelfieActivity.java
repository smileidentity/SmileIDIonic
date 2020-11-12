package cordova.plugin.smileid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.smileidentity.libsmileid.core.CameraSourcePreview;
import com.smileidentity.libsmileid.core.SelfieCaptureConfig;
import com.smileidentity.libsmileid.core.SmartSelfieManager;
import com.smileidentity.libsmileid.core.captureCallback.FaceState;
import com.smileidentity.libsmileid.core.captureCallback.OnFaceStateChangeListener;

import io.ionic.starter.R;

public class SIDSelfieActivity extends Activity implements OnFaceStateChangeListener,
  SmartSelfieManager.OnCompleteListener, View.OnClickListener {

  CameraSourcePreview mPreview;
  private TextView mPromptTv;
  private SmartSelfieManager smartSelfieManager;
  View mMultiEnrollContainer;
  private boolean mMultipleEnroll = false, mUseOffLineAuth = false;
  private String currentTag;
  private View mCapturePictureBtn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sid_activity_selfie);
    mPreview = findViewById(R.id.previewCamera);
    mPromptTv = findViewById(R.id.prompt_tv);
    currentTag = getIntent().getStringExtra(cordova.plugin.smileid.SIDStringExtras.EXTRA_TAG);
    Log.v("JAPHET", "current : " + currentTag);
    mMultiEnrollContainer = findViewById(R.id.multiple_selfie_container_ll);
    Button mTakeAnotherSelfieBtn = findViewById(R.id.take_another_selfie_btn);
    mCapturePictureBtn = findViewById(R.id.capture_btn);

    Button mContinueBtn = findViewById(R.id.continue_btn);
    mContinueBtn.setOnClickListener(this);

    Button continueWithIdBtn = findViewById(R.id.continue_with_id_btn);
    continueWithIdBtn.setOnClickListener(this);

    mCapturePictureBtn.setOnClickListener(this);
    mTakeAnotherSelfieBtn.setOnClickListener(this);
    smartSelfieManager = new SmartSelfieManager(getCaptureConfig());
    smartSelfieManager.setOnCompleteListener(this);
    smartSelfieManager.setOnFaceStateChangeListener(this);
    setBrightnessToMax(this);
    smartSelfieManager.captureSelfie(currentTag);
  }


  @Override
  protected void onResume() {
    super.onResume();
    smartSelfieManager.resume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    smartSelfieManager.pause();
  }

  @Override
  public void onFaceStateChange(FaceState faceState) {
    switch (faceState) {
      case DO_SMILE:
        if ((mMultipleEnroll || mUseOffLineAuth) && !mMultiEnrollContainer.isShown()) {
          mCapturePictureBtn.setVisibility(View.VISIBLE);
        } else {
          mPromptTv.setText("Smile for the camera.");
        }
        break;
      case CAPTURING:
        mPromptTv.setText("");
        break;
      case DO_SMILE_MORE:
        mPromptTv.setText("Smile more");
        break;
      case NO_FACE_FOUND:
        mPromptTv.setText("No face found.");
        break;
      case DO_MOVE_CLOSER:
        mPromptTv.setText("Move closer");
        break;
    }
  }

  private void setBrightnessToMax(Activity activity) {
    // for brightness
    WindowManager.LayoutParams layout = activity.getWindow().getAttributes();
    layout.screenBrightness = 1F;
    activity.getWindow().setAttributes(layout);
    // keep our app screen on
    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override
  public void onError(Throwable e) {
    String error = "Could not initialise selfie camera";
    if (e != null && e.getMessage() != null) {
      error = e.getMessage();
    }
    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    e.printStackTrace();
    Intent intent = new Intent();
    intent.putExtra(cordova.plugin.smileid.SIDStringExtras.EXTRA_TAG, currentTag);
    setResult(RESULT_CANCELED, intent);
    finish();
  }

  @Override
  protected void onStop() {
    super.onStop();
    smartSelfieManager.stop();
  }

  @Override
  public void onComplete(Bitmap fullPreviewFrame) {
    if (fullPreviewFrame != null) {
      //Process returned full preview frame
    }
    smartSelfieManager.stop();
    Intent intent = new Intent();
    intent.putExtra(cordova.plugin.smileid.SIDStringExtras.EXTRA_TAG, currentTag);
    setResult(RESULT_OK, intent);
    finish();
  }

  private SelfieCaptureConfig getCaptureConfig() {
    return new SelfieCaptureConfig.Builder(this)
      .setCameraType(/*mMultipleEnroll ? SelfieCaptureConfig.BACK_CAMERA :*/ SelfieCaptureConfig.FRONT_CAMERA)
      .setPreview(mPreview)
      .setManualSelfieCapture((mMultipleEnroll || mUseOffLineAuth))
      .setFlashScreenOnShutter(!mMultipleEnroll && !mUseOffLineAuth)
      .build();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.continue_btn:
        Intent intent = new Intent();
        intent.putExtra(cordova.plugin.smileid.SIDStringExtras.EXTRA_TAG, currentTag);
        setResult(RESULT_OK, intent);
        finish();
        break;
      case R.id.take_another_selfie_btn:
        mMultiEnrollContainer.setVisibility(View.GONE);
        smartSelfieManager.captureSelfie(currentTag);
        smartSelfieManager.resume();
        break;
      case R.id.capture_btn:
        smartSelfieManager.takePicture();
        break;
    }
  }

  @Override
  public void onBackPressed() {
    Intent intent = new Intent();
    intent.putExtra(cordova.plugin.smileid.SIDStringExtras.EXTRA_TAG, currentTag);
    setResult(RESULT_CANCELED, intent);
    finish();
  }
}
