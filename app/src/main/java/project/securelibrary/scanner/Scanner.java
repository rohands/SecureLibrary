package project.securelibrary.scanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.TextView;

import project.securelibrary.R;

public class Scanner extends Activity implements QRCodeReaderView.OnQRCodeReadListener {
	String phno;
	private TextView myTextView;
	private QRCodeReaderView mydecoderview;
	private Context context;
	SharedPreferences pref;
	Intent intentScanner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_decoder);
		pref = getSharedPreferences("Prepaye", Context.MODE_PRIVATE);
		System.out.println("In SCANNER'S ON CREATE :" + R.id.qrdecoderview);
		mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
		mydecoderview.setOnQRCodeReadListener(this);
		context = getApplicationContext();
		intentScanner = getIntent();
		String yes = intentScanner.getStringExtra("SCANNER_WORKING");
	}

	// Called when a QR is decoded
	// "text" : the text encoded in QR
	// "points" : points where QR control points are placed
	@Override
	public void onQRCodeRead(String text, PointF[] points) {
		// Log.d("scanner", text);

		String phase = pref.getString("PHASE", "NOT SET");
		// if (phase.equals("INTRODUCTIONPHASE1")) {

		System.out.println(text);
		intentScanner.putExtra("result", text);
		System.out.println("Check");

		setResult(Activity.RESULT_OK, intentScanner);
		finish();
		// startActivityForResult(intent, 441);
		System.out.println("Checking");

		System.out.println(getCallingActivity());
		SharedPreferences pref = getSharedPreferences("Prepaye",
				context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString("Scanner_result", text);
		editor.commit();
		// }
		System.out.println("Before finish in scanner");

	}

	// Called when your device have no camera
	@Override
	public void cameraNotFound() {

	}

	// Called when there's no QR codes in the camera preview image
	@Override
	public void QRCodeNotFoundOnCamImage() {

	}

	@Override
	protected void onResume() {
		super.onResume();
		mydecoderview.getCameraManager().startPreview();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mydecoderview.getCameraManager().stopPreview();
	}
}
