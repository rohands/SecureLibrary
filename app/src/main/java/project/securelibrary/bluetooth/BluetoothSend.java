package project.securelibrary.bluetooth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import project.securelibrary.R;


public class BluetoothSend extends Activity implements View.OnClickListener {
	File myFile;
	String inputText = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_send);

		Intent intent = getIntent();
		SharedPreferences pref = getSharedPreferences("Prepaye",
				Context.MODE_PRIVATE);
		inputText = pref.getString("dataToBeEncoded", "NOT SET");
		Log.d("INFORMATION", "first create a file then switch on bluetooth");
		Button sendButton = (Button) findViewById(R.id.button1);
		sendButton.setOnClickListener(this);
	

	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.button1) {
	
			createfile(inputText);
		
			return;

		} else {
			return;
		}

	}

	// here starts the main method for creating , writing and sharing the file .

	public void createfile(String data) {
		String path = "/sdcard/btfile.txt";
		myFile = new File(path);
		if(myFile.exists())
		{
			boolean deleted = myFile.delete();
			if (deleted) {
				Log.d("INFORMATION", "stale file deleted");
			}
		}
		try {
			Log.d("Bluetooth Send Data", data);
			myFile = new File(path);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			if (myFile.exists()) {
				System.out.println("File created");
			}
			// pass your string in the method append given below

			myOutWriter.append(data);
			myOutWriter.close();
			fOut.close();
			Toast.makeText(getApplicationContext(),
					"Done writing SD 'mysdfile.txt'", Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			System.out.println("In bluetooth send" + e);
			Toast.makeText(getApplicationContext(),
					"error writing" + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
			this.finish();
			startActivity(getIntent());
		}

		sendviabluetooth(path);

	}

	public void sendviabluetooth(String path) {
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		Uri screenshotUri = Uri.fromFile(new File(path));
		Log.d(" Send BLUETOOTH method", screenshotUri.toString());
		sharingIntent.setType("text/plain");
		sharingIntent.setPackage("com.android.bluetooth");
		sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
		startActivityForResult(
				Intent.createChooser(sharingIntent, "Share Text_file"), 411);

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 411) {
			if (resultCode == RESULT_OK) {
				Log.d("INFORMATION", "Sent a file");
			}

			finishActivity(requestCode);
		}
	}

}
