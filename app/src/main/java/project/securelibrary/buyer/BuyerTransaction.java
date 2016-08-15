package project.securelibrary.buyer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import project.securelibrary.R;
import project.securelibrary.bluetooth.BluetoothReceive;
import project.securelibrary.bluetooth.BluetoothSend;
import project.securelibrary.callback.BuyerCallback;
import project.securelibrary.db.Other;
import project.securelibrary.db.Self;
import project.securelibrary.generator.Generator;
import project.securelibrary.main.Base;
import project.securelibrary.scanner.Scanner;


public class BuyerTransaction extends Activity {
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
	final String sendvia = "QRCODE";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transaction);
		this.activity = this;
		this.context = getApplicationContext();
		base = new Base(this.activity, this.context);
		Intent intent = getIntent();
		String data = intent.getStringExtra("data");
		String phno = intent.getStringExtra("phoneNumber");
		this.callback = (BuyerCallback) intent
				.getSerializableExtra("callbackObject");
		otherPhno = phno;
		pref = context.getSharedPreferences("Prepaye", Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString("OtherPhoneNumber", otherPhno);
		editor.commit();
		transaction(data, phno);
	}

	public BuyerTransaction() {

	}

	public BuyerTransaction(Activity activity, Context context) {
		this.activity = (BuyerTransaction) activity;
		this.context = (BuyerTransaction) context;
		base = new Base(this.activity, this.context);
	}

	final public void transaction(String data, String otherPhno) {
		pref = context.getSharedPreferences(Prepaye, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString("DEV_DATA", data);
		editor.commit();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar cal = Calendar.getInstance();
		Self selfObject = null;
		String transaction = "";
		try {
			selfObject = base.readOwnerDetails();
		} catch (Exception exception) {
			String errorMessage = "Error:" + exception;
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			this.finish();
			// startActivity(getIntent());
		}
		String phno = selfObject.getPhoneNumber();
		String lastTranTime = new String(sdf.format(cal.getTime()));
		String dataToEncrypt = phno + ";" + lastTranTime + ";" + data;
		try {
			List<String> array = base.sign(dataToEncrypt, this.context);
			String encryptedData = "";
			int i;
			for (i = 0; i < array.size(); i++) {
				encryptedData = encryptedData + array.get(i) + ";";
			}
			encryptedData = encryptedData.substring(0,
					encryptedData.length() - 1);
			Log.d("AFTER SIGNING DATA", encryptedData);
			Other otherObject = base.readOtherDetails(otherPhno);
			System.out.println("OTHER OBJECT DETAILS "
					+ otherObject.getPreviousEncrypted());
			if (otherObject.getPreviousEncrypted() == null) {
				transaction = phno + ";" + "null" + ";" + encryptedData;
			} else {
				transaction = phno + ";" + otherObject.getPreviousEncrypted()
						+ ";" + encryptedData;
			}
			Log.d("Transaction Phase 1", transaction);
			pref = context.getSharedPreferences(Prepaye, Context.MODE_PRIVATE);
			editor = pref.edit();
			editor.putString("PHASE", "transaction");
			editor.commit();
			// send data to generator or bluetooth.
		} catch (Exception exception) {
			System.out.println(exception);
			String errorMessage = "Error:" + exception;
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			this.finish();
		}
		SharedPreferences pref = getSharedPreferences("Prepaye",
				Context.MODE_PRIVATE);
		editor = pref.edit();
		editor.putString("dataToBeEncoded", transaction);
		editor.putString("dataToBeReceivedInAck", data);
		editor.commit();
		// intent.putExtra("dataToBeEncoded", transaction1);
		Log.d("TRANSACTION", "PHASE1");
		if (sendvia.equalsIgnoreCase("QRCODE")) {
			Intent intent = new Intent(this, Generator.class);
			// intent.putExtra("dataToBeEncoded", introduction);

			activity.startActivityForResult(intent, 447);
		} else {

			Intent intent = new Intent(this, BluetoothSend.class);
			activity.startActivityForResult(intent, 447);
		}

	}

	final public void transactionPhase1(String data, String contactNumber) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		String now = new String(sdf.format(cal2.getTime()));

		StringTokenizer tokenizer = new StringTokenizer(data, ";");
		List<String> packet = new ArrayList<String>();
		while (tokenizer.hasMoreElements()) {
			packet.add((String) tokenizer.nextElement());
		}
		Log.d("Transaction Phase 2", "After receive");
		System.out.println("Other phno: " + otherPhno
				+ " contactNumber received :" + contactNumber);
		contactNumber = packet.get(0);
		if (otherPhno.compareTo(contactNumber) != 0) {
			String Message = "Wrong message Scanned. Please retry with authentic person";
			toast = Toast.makeText(this.context, Message, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			this.finish();
			// startActivity(getIntent());
			// return null;
		} else {
			List<String> encryptedData = new ArrayList<String>();
			String prevEncryptedData = "";
			for (int i = 2; i < packet.size(); i++) {
				encryptedData.add(packet.get(i));
				prevEncryptedData += packet.get(i) + ":";
			}
			prevEncryptedData = prevEncryptedData.substring(0,
					prevEncryptedData.length() - 1);
			try {
				String decryptedData = base.unsign(encryptedData, otherPhno,
						this.context);
				StringTokenizer tokenizer2 = new StringTokenizer(decryptedData,
						";");
				List<String> Data = new ArrayList<String>();
				while (tokenizer2.hasMoreElements()) {
					Data.add((String) tokenizer2.nextElement());
				}
				if (otherPhno.compareTo(Data.get(0)) != 0) {
					String Message = "Wrong message Scanned. Please retry with authentic person";
					toast = Toast.makeText(this.context, Message, duration);
					toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
					toast.show();
					this.finish();
					// startActivity(getIntent());
					// return null;
				}
				SharedPreferences pref = this.context.getSharedPreferences(
						"Prepaye", context.MODE_PRIVATE);
				String ackData = pref
						.getString("dataToBeReceivedInAck", "none");
				Other otherObject = base.readOtherDetails(otherPhno);
				System.out.println("Data received: " + Data.get(2)
						+ " Actual data: " + ackData);
				data = Data.get(2);

				if ((packet.get(1)).equals("conflict_yes")) {

					// return data;
					String Message = "Conflict arised. Please resolve conflict";
					System.out.println(Message);
					toast = Toast.makeText(context, Message, duration);
					toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
					toast.show();

					pref = context.getSharedPreferences(Prepaye,
							Context.MODE_PRIVATE);
					Editor editor = pref.edit();
					editor.putString("DataForPhase2", Data.get(2));
					editor.putString("PreviousEncrypted", prevEncryptedData);
					editor.commit();
					System.out.println("Calling callback" + callback);
					if (callback != null) {
						Intent intent = new Intent(
								BuyerTransaction.this,
								Class.forName("com.example.democustomer.CallbackImplementor"));
						intent.putExtra("dataForCallback", Data.get(2));
						intent.putExtra("flag", 1);
						startActivityForResult(intent, 5000);
					}

				} else {
					if (ackData.equals(Data.get(2))) {
						otherObject.setPreviousEncrypted(prevEncryptedData);
						base.updateTable(otherObject);
						Intent intent = new Intent(
								BuyerTransaction.this,
								Class.forName("com.example.democustomer.CallbackImplementor"));
						intent.putExtra("dataForCallback", Data.get(2));
						intent.putExtra("flag", 2);
						startActivityForResult(intent, 5001);

					} else {
						String errorMessage = "Disagreement in data to be sent in acknowledgement.Restart the transaction.";
						System.out.println("ERROR in catch " + errorMessage);
						toast = Toast.makeText(this.context, errorMessage,
								duration);
						toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400,
								300);
						toast.show();
						this.finish();
						// startActivity(getIntent());
					}
					System.out.println("Going to finish transaction phase1");
					Intent intent = getIntent();
					intent.putExtra("dataFromTransaction", data);
					if (getParent() == null) {
						setResult(Activity.RESULT_OK, intent);
					} else {
						getParent().setResult(Activity.RESULT_OK, intent);
					}
					finish();
				}

				System.out.println("PACKET DATA" + packet.get(0) + ","
						+ packet.get(1) + "," + packet.get(2));

				// return Data.get(2);
			}// try block

			catch (Exception exception) {
				String errorMessage = "Error:" + exception;
				System.out.println("EXCEPTION:" + errorMessage);
				toast = Toast.makeText(this.context, errorMessage, duration);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
				toast.show();
				this.finish();

			}

		}// else block

	}

	final public void transactionPhase2() {
		SharedPreferences pref = this.context.getSharedPreferences("Prepaye",
				Context.MODE_PRIVATE);
		String data = pref.getString("DataForPhase2", "none");
		otherPhno = pref.getString("OtherPhoneNumber", null);
		Other otherObject = null;
		System.out.println(data + " TRAN PHASE 2 " + otherPhno);
		base = new Base(activity, context);
		try {
			otherObject = base.readOtherDetails(otherPhno);
			String prevEncryptedData = pref.getString("PreviousEncrypted",
					otherObject.getPreviousEncrypted());
			otherObject.setPreviousEncrypted(prevEncryptedData);
			base.updateTable(otherObject);
		} catch (Exception exception) {
			Log.d("Error in updating others details", exception.toString());
			String errorMessage = "Error:" + exception;
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			this.finish();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar cal = Calendar.getInstance();
		Self selfObject = null;
		String result = "";
		String transaction = "";
		try {
			selfObject = base.readOwnerDetails();
		} catch (Exception exception) {
			String errorMessage = "Error:" + exception;
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			this.finish();
			// startActivity(getIntent());
		}
		String phno = selfObject.getPhoneNumber();
		String lastTranTime = new String(sdf.format(cal.getTime()));
		String dataToEncrypt = phno + ";" + lastTranTime + ";" + data;
		try {
			List<String> array = base.sign(dataToEncrypt, this.context);
			String encryptedData = "";
			int i;
			for (i = 0; i < array.size(); i++) {
				encryptedData = encryptedData + array.get(i) + ";";
			}
			encryptedData = encryptedData.substring(0,
					encryptedData.length() - 1);
			Log.d("AFTER SIGNING DATA", encryptedData);
			// Other otherObject = base.readOtherDetails(otherPhno);
			System.out.println("OTHER OBJECT DETAILS "
					+ otherObject.getPreviousEncrypted());

			transaction = phno + ";" + "null" + ";" + encryptedData;

			Log.d("Transaction Phase 3", transaction);
			pref = context.getSharedPreferences(Prepaye, Context.MODE_PRIVATE);
			Editor editor = pref.edit();
			editor.putString("PHASE", "transactionPhase2");
			editor.commit();
			// send data to generator or bluetooth.
		} catch (Exception exception) {
			System.out.println(exception);
			String errorMessage = "Error:" + exception;
			toast = Toast.makeText(this.context, errorMessage, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
		}
		pref = getSharedPreferences("Prepaye", Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString("dataToBeEncoded", transaction);
		editor.putString("dataToBeReturnedToDev", data);
		editor.commit();

		Log.d("DEBUG", "In transaction Phase2");

		if (sendvia.equalsIgnoreCase("QRCODE")) {
			Intent intent = new Intent(BuyerTransaction.this, Generator.class);
			// intent.putExtra("dataToBeEncoded", introduction);

			activity.startActivityForResult(intent, 449);
		} else {

			Intent intent = new Intent(BuyerTransaction.this,
					BluetoothSend.class);
			activity.startActivityForResult(intent, 449);
		}
	}

	final public void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		System.out.println("IN BUYER RESULT");

		if (requestCode == 447) {
			if (sendvia.equalsIgnoreCase("QRCODE")) {
				Intent intentScanner = new Intent(this, Scanner.class);
				intentScanner.putExtra("SCANNER_WORKING", "yes");

				activity.startActivityForResult(intentScanner, 448);
			} else {
				Intent intentScanner = new Intent(this, BluetoothReceive.class);
				intentScanner.putExtra("SCANNER_WORKING", "yes");

				activity.startActivityForResult(intentScanner, 448);
			}

			return;
		}
		if (requestCode == 448) {

			if (resultCode == Activity.RESULT_OK) {
				Log.d("DEBUG", "Recieved data after scanner");
				SharedPreferences pref = this.context.getSharedPreferences(
						"Prepaye", Context.MODE_PRIVATE);
				System.out.println("PRINTING scanner result :"
						+ pref.getString("Scanner_result", "none"));
				receivedDetails = pref.getString("Scanner_result", "none");

				transactionPhase1(receivedDetails, otherPhno);

			}

		}
		if (requestCode == 449) {
			String Message = "Conflict resolved. your transaction is successful";
			toast = Toast.makeText(this.context, Message, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			SharedPreferences pref = this.context.getSharedPreferences(
					"Prepaye", context.MODE_PRIVATE);

			String x = pref.getString("dataToBeReturnedToDev", "none");
			System.out.println(x);
			Intent intent = new Intent();
			intent.putExtra("dataFromTransaction", x);
			if (getParent() == null) {
				setResult(Activity.RESULT_OK, intent);
			} else {
				getParent().setResult(Activity.RESULT_OK, intent);
			}
			finish();
			// startActivity(getIntent());
		}
		if (requestCode == 5000) {
			SharedPreferences pref = this.context.getSharedPreferences(
					"Prepaye", context.MODE_PRIVATE);
			String prevEncryptedData = pref.getString("PreviousEncrypted",
					"none");
			Other otherObject = null;
			String returnData = data.getStringExtra("data");
			if (returnData.equals("false") == false) {
				try {
					otherObject = base.readOtherDetails(otherPhno);
					otherObject.setPreviousEncrypted(prevEncryptedData);
					base.updateTable(otherObject);
				} catch (Exception e) {
					System.out.println(e.toString());
				}
				System.out.println(otherObject.toString());
				transactionPhase2();
			} else {
				finish();
			}

		}
		if (requestCode == 5001) {
			System.out.println("Successfull Transaction");
			finish();
		}
	}
}
