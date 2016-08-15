package project.securelibrary.generator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import project.securelibrary.R;
import project.securelibrary.buyer.Buyer;

public class Generator extends Activity {

	private String qrInputText = "";
	Context context;

	Buyer buyerObj;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qr_display);
		context = getApplicationContext();
		Log.d("BEFORE GETTING INTENT STRING", "HERE");
		SharedPreferences pref = getSharedPreferences("Prepaye",
				Context.MODE_PRIVATE);
		qrInputText = pref.getString("dataToBeEncoded", "");
		// Intent intent = getIntent();
		// qrInputText =
		// getIntent().getExtras().get("dataToBeEncoded").toString();
		System.out.println("IN GENERATOR " + getCallingActivity());
		// qrInputText = intent.getStringExtra("dataToBeEncoded");
		System.out.println("DATA TO BE ENCODED!!" + qrInputText);
		WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		int width = point.x;
		int height = point.y;
		int smallerDimension = width < height ? width : height;
		smallerDimension = smallerDimension * 3 / 4;

		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText, null,
				"TEXT", BarcodeFormat.QR_CODE.toString(), smallerDimension);
		try {
			Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
			ImageView myImage = (ImageView) findViewById(R.id.imageView1);
			myImage.setImageBitmap(bitmap);
		} catch (WriterException e) {
			System.out.println("INSIDE CATCH!!!!!!!!IN GENERATOR");
			e.printStackTrace();
		}
		System.out.println("AFTER TRY CATCH IN GENERATOR!");
		Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(onclickListener);
	}

	private OnClickListener onclickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			SharedPreferences pref = getSharedPreferences("Prepaye",
					Context.MODE_PRIVATE);
			String sendingFrom = pref.getString("PHASE", "NOT SET");
			// if (sendingFrom.equals("INTRODUCTIONPHASE1")) {
			Log.d("ERROR", "If reached here danger");
			Intent intent = getIntent();
			intent.putExtra("Generator_Success", true);
			setResult(Activity.RESULT_OK, intent);
			finish();
			// }
		}

	};

}
