package cordova.plugin.smileid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.smileidentity.libsmileid.core.idcapture.SmartCardView;
import com.smileidentity.libsmileid.exception.SIDException;

import io.ionic.starter.R;

public class SIDIDCardActivity extends Activity implements SmartCardView.SmartCardViewCallBack {

  private SmartCardView mSmartCardView;
  private String mCurrentTag;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setFullScreen();
    setContentView(R.layout.activity_id_card);
    mSmartCardView = findViewById(R.id.id_capture);
    mSmartCardView.setListener(this);
    mCurrentTag = getIntent().getStringExtra(cordova.plugin.smileid.SIDStringExtras.EXTRA_TAG);
    Log.v("JAPHET", "current : " + mCurrentTag);
  }

  @Override
  protected void onResume() {
    super.onResume();
    try {
      mSmartCardView.startCapture(mCurrentTag);
    } catch (SIDException e) {
      e.printStackTrace();
      Intent intent = new Intent();
      intent.putExtra("error", e.getMessage());
      intent.putExtra("tag", mCurrentTag);
      setResult(RESULT_CANCELED, intent);
    }
  }

  private void setFullScreen() {
    final View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility(
      View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_FULLSCREEN);
  }

  @Override
  public void onSmartCardViewError(Exception error) {
    String errorMessage = "Could not initialise ID capture camera";
    if (error != null && error.getMessage() != null) {
      errorMessage = error.getMessage();
    }
    error.printStackTrace();
    Intent intent = new Intent();
    intent.putExtra("error", errorMessage);
    intent.putExtra("tag", mCurrentTag);
    setResult(RESULT_CANCELED, intent);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSmartCardView.pauseCapture();
  }

  @Override
  public void onSmartCardViewComplete(Bitmap idCardBitmap, boolean faceFound) {
    Intent intent = new Intent();
    intent.putExtra("idCardBitmap", idCardBitmap != null);
    intent.putExtra("faceFound", faceFound);
    intent.putExtra("tag", mCurrentTag);
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override
  public void onSmartCardViewClosed() {
    Intent intent = new Intent();
    intent.putExtra("tag", mCurrentTag);
    setResult(RESULT_CANCELED, intent);
    finish();
  }
}
