package project.securelibrary.seller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import project.securelibrary.callback.SellerCallback;
import project.securelibrary.db.Self;
import project.securelibrary.generator.Generator;
import project.securelibrary.main.Base;
import project.securelibrary.scanner.Scanner;


public class SellerIntroduction extends Activity {
	int duration = Toast.LENGTH_LONG;
	Context context;
	Toast toast;
	String receivedDetails = "";
	SharedPreferences pref;
	public static final String Prepaye = "Prepaye";
	Activity activity;
	Base base;
	String otherPhno = "";
	Self selfObject = null;
	String returnString = "";
	String sendvia = "QRCODE";//"BLUETOOTH";
	SellerCallback callback;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.introduction);
		this.activity = this;
		this.context = getApplicationContext();
		base = new Base(this.activity, this.context);
		Intent intent = getIntent();
		String data = intent.getStringExtra("data");
		this.callback = (SellerCallback) intent
				.getSerializableExtra("callbackObject");
		introduction(data);
	}
	

	public void introduction(String data) {
		pref = context.getSharedPreferences(Prepaye, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("INTRODUCTION_DATA", data);
		editor.commit();
		if (sendvia.compareToIgnoreCase("QRCODE") == 0) {
			Intent intentScanner = new Intent(this, Scanner.class);
			intentScanner.putExtra("SCANNER_WORKING", "yes");
			activity.startActivityForResult(intentScanner, 445);
		} else {
			Intent intentScanner = new Intent(this, BluetoothReceive.class);
			intentScanner.putExtra("SCANNER_WORKING", "yes");
			activity.startActivityForResult(intentScanner, 445);
		}
	}

	public void introductionPhase1() {


		if (receivedDetails != null) {
			StringTokenizer tokenizer = new StringTokenizer(receivedDetails,
					";");
			List<String> packet = new ArrayList<String>();
			while (tokenizer.hasMoreElements()) {
				packet.add((String) tokenizer.nextElement());
			}
			System.out.println(packet.get(1));
	
			try {
				receivedDetails = base.populateOther(packet.get(1),
						packet.get(0), packet.get(2), packet.get(3));
				toast = Toast.makeText(this.context, receivedDetails, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();
			
				pref = context.getSharedPreferences(Prepaye, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("dev_data", packet.get(4));
				editor.commit();
				
			} catch (Exception exception) {
				String errorMessage = "Error:" + exception;
				toast = Toast.makeText(this.context, errorMessage, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();
			}
			introductionPhase2();

		}

	}

	public void introductionPhase2() {
		String data = pref.getString("INTRODUCTION_DATA", "NOT SET");
		Self selfObject = null;
		try {
			selfObject = base.readOwnerDetails();
		} catch (Exception exception) {
			String errorMessage = "Error:" + exception;
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
		}

		final String introduction = selfObject.getPhoneNumber() + ";"
				+ selfObject.getName() + ";" + selfObject.getModpubkey() + ";"
				+ selfObject.getExpopubkey() + ";" + data;
		
		pref = context.getSharedPreferences(Prepaye, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("dataToBeEncoded", introduction);
		editor.commit();
		
		if (sendvia.compareToIgnoreCase("QRCODE") == 0) {
			System.out.println("In generate " + introduction);
			Intent intent = new Intent(this, Generator.class);
			
			activity.startActivityForResult(intent, 444);
		} else {
			System.out.println("In generate " + introduction);
			Intent intent = new Intent(this, BluetoothSend.class);
			
			activity.startActivityForResult(intent, 444);
		}

	}

	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("IN BUYER RESULT");

		if (requestCode == 445) {
			Log.d("DEBUGGING", "In base in recieve");
			if (resultCode == Activity.RESULT_OK) {

				SharedPreferences pref = this.context.getSharedPreferences(
						"Prepaye", Context.MODE_PRIVATE);
				System.out.println("PRINTING scanner result :"
						+ pref.getString("Scanner_result", "none"));
				receivedDetails = pref.getString("Scanner_result", "none");
				this.activity.finishActivity(requestCode);
				introductionPhase1();
			}

		}
		if (requestCode == 444) {
		
			SharedPreferences pref = this.context.getSharedPreferences(
					"Prepaye", Context.MODE_PRIVATE);
			
			String dev_data = pref.getString("dev_data", "none");

			Intent intent = getIntent();
			intent.putExtra("dataFromIntroduction", dev_data);
			setResult(Activity.RESULT_OK, intent);
			finish();
			
		}

		
	}
}

	