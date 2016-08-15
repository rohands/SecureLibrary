package project.securelibrary.bluetooth;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import project.securelibrary.R;


public class BluetoothReceive extends Activity implements View.OnClickListener {
	File myFile;
	int duration = Toast.LENGTH_LONG;
	Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_receive);
		Log.d("DEBUG BLUETOOTH", "In bluetooth Receive");
		Button receiveButton = (Button) findViewById(R.id.button1);
		Button refresh = (Button) findViewById(R.id.button2);
		
		File file = null;
		Log.d("In main", "In main");
		file = new File("/sdcard/bluetooth/btfile.txt");
		if (!file.exists()) {
			
			String Message = "File not found. Make sure you click on Refresh after  you have received the file";
			toast = Toast.makeText(getApplicationContext(), Message, duration);
			toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 400, 300);
			toast.show();
			receiveButton.setEnabled(false);

		} else {
			receiveButton.setEnabled(true);
		}

		receiveButton.setOnClickListener(this);
		refresh.setOnClickListener(this);

	}

	// here starts the main method for creating , writing and sharing the file .

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.button1) {
	
			readFile();
		
		}
		if (v.getId() == R.id.button2) {
		
			Intent intent = new Intent(this, BluetoothReceive.class);
			startActivity(intent);
		}
	}

	public void readFile() {
		String returnString = null;
		File file = null;

		file = new File("/sdcard/bluetooth/btfile.txt");
		if (file.exists()) {
			try {

				FileInputStream recievedFile = new FileInputStream(file);
				byte fileContent[] = new byte[(int) recievedFile.available()];
				// The information will be content on the buffer.
				recievedFile.read(fileContent);
				returnString = new String(fileContent);
				recievedFile.close();

			} catch (Exception ex) {
				String Message = "Corrupted file! Please try again";
				toast = Toast.makeText(getApplicationContext(), Message,
						duration);
				toast.setGravity(Gravity.TOP | Gravity.LEFT, 150, 100);
				toast.show();

			}
			SharedPreferences pref = getSharedPreferences("Prepaye",
					getApplicationContext().MODE_PRIVATE);
			Editor editor = pref.edit();
			editor.putString("Scanner_result", returnString);
			editor.commit();

			boolean deleted = file.delete();
			if (deleted) {
				Log.d("INFORMATION", "File deleted");
				finish();
			}
			
			
		}
	}

}
