package project.securelibrary.buyer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import project.securelibrary.R;
import project.securelibrary.bluetooth.BluetoothReceive;
import project.securelibrary.bluetooth.BluetoothSend;
import project.securelibrary.callback.BuyerCallback;
import project.securelibrary.db.Self;
import project.securelibrary.generator.Generator;
import project.securelibrary.main.Base;
import project.securelibrary.scanner.Scanner;


public class BuyerIntroduction extends Activity {
	int duration = Toast.LENGTH_LONG;
	Context context;
	Toast toast;
	String receivedDetails = "";
	SharedPreferences pref;
	public static final String Prepaye = "Prepaye";
	Activity activity;
	Base base;
	String otherPhno;
	BuyerCallback callback;
	final String sendvia = "QRCODE"; // Use Bluetooth

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.introduction);
		this.activity = this;
		this.context = getApplicationContext();
		base = new Base(this.activity, this.context);
		Intent intent = getIntent();
		String data = intent.getStringExtra("data");
		this.callback = (BuyerCallback) intent
				.getSerializableExtra("callbackObject");
		introduction(data);
	}

	// public BuyerIntroduction(Activity activity, Context context) {
	// this(activity, context, null);
	// }
	//
	// public BuyerIntroduction(Activity activity, Context context,
	// BuyerCallback callback) {
	// this.activity = (BuyerIntroduction) activity;
	// this.context = (BuyerIntroduction) context;
	// this.callback = callback;
	// // base = new Base(this.context, this.activity);
	// }

	final public void introduction(String data) {
		Self selfObject = null;
		try {
			selfObject = base.readOwnerDetails();
		} catch (Exception exception) {
			String errorMessage = "Error:" + exception;
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
		}
		Log.d("Introduction", "Finished reading owner");
		final String introduction = selfObject.getPhoneNumber() + ";"
				+ selfObject.getName() + ";" + selfObject.getModpubkey() + ";"
				+ selfObject.getExpopubkey() + ";" + data;
		pref = context.getSharedPreferences(Prepaye, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("PHASE", "introduction");
		// editor.putString("PHASE", "introduction");
		editor.commit();

		Log.d("INFORMATION", "USING QR CODE FOR TRANSMISSION");

		editor.putString("dataToBeEncoded", introduction);
		editor.commit();
		System.out.println("In generate " + introduction);
		if (sendvia.equalsIgnoreCase("QRCODE")) {
			Intent intent = new Intent(this, Generator.class);
			// intent.putExtra("dataToBeEncoded", introduction);

			activity.startActivityForResult(intent, 444);
		} else {

			Intent intent = new Intent(this, BluetoothSend.class);
			activity.startActivityForResult(intent, 444);
		}

	}

	final public void introductionPhase1() {
		List<String> packet = null;
		// String receivedDetails = receive();
		System.out.println("About to scan!!!!");

		if (receivedDetails != null) {
			StringTokenizer tokenizer = new StringTokenizer(receivedDetails,
					";");
			packet = new ArrayList<String>();
			while (tokenizer.hasMoreElements()) {
				packet.add((String) tokenizer.nextElement());
			}
			System.out.println(packet.get(1));
			Log.d("Introduction", "After receive");
			try {
				receivedDetails = base.populateOther(packet.get(1),
						packet.get(0), packet.get(2), packet.get(3));
			} catch (Exception exception) {
				String errorMessage = "Error:" + exception;
				System.out.println(errorMessage);
				toast = Toast.makeText(this.context, errorMessage, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();
			}
			// SEND DATA BACK TO DEV
			toast = Toast.makeText(this.context, receivedDetails, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
		}
		Intent intent = getIntent();
		System.out.println("CALLING INTENT " + getCallingActivity());
		intent.putExtra("dataFromIntroduction", packet.get(4));
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	final public void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		System.out.println("IN BUYER RESULT");

		if (requestCode == 445) {
			Log.d("DEBUGGING", "In base in recieve");
			if (resultCode == Activity.RESULT_OK) {
				Log.d("INFORMATION", "Recieved data");
				SharedPreferences pref = this.context.getSharedPreferences(
						"Prepaye", context.MODE_PRIVATE);
				System.out.println("PRINTING scanner result :"
						+ pref.getString("Scanner_result", "none"));
				receivedDetails = pref.getString("Scanner_result", "none");
				this.activity.finishActivity(requestCode);
				introductionPhase1();
			}

		}
		if (requestCode == 444) {
			if (sendvia.equalsIgnoreCase("QRCODE")) {
				Intent intentScanner = new Intent(this, Scanner.class);
				intentScanner.putExtra("SCANNER_WORKING", "yes");
				activity.startActivityForResult(intentScanner, 445);
			} else {
				Intent intentScanner = new Intent(this, BluetoothReceive.class);
				intentScanner.putExtra("SCANNER_WORKING", "yes");
				activity.startActivityForResult(intentScanner, 445);
			}
			return;
		}

	}

}
