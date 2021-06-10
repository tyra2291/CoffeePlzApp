package com.example.coffeeplz;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private boolean isFirstRun = true;
	private static final int SETTINGS_ACTIVITY_CODE = 1;
	private Intent nfcActivity;
	private ImageView menuImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		menuImage = (ImageView) findViewById(R.id.menuImage);
		try {
			checkNfcActivation();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks NFC activation
	 */
	public void checkNfcActivation() throws InterruptedException {

		NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
		if (nfc == null){
		// you are fucked
		Toast.makeText(this,"No NFC capability on this device, go buy a real phone",Toast.LENGTH_LONG).show();
		} else if (!nfc.isEnabled()){
			if (isFirstRun) {
				Toast.makeText(this, "NFC must be activated you fool", Toast.LENGTH_SHORT).show();
			}else{
			Toast.makeText(this, "ACTIVATE NFC YOU DUMB FUCK", Toast.LENGTH_SHORT).show();
			}
			// NFC present but not activated
			(new Handler()).postDelayed(this::activateNfc,1000);
			isFirstRun = false;
		} else {
			// NFC activated -> switch to next activity
			menuImage.setImageResource(R.drawable.mainthief2);
			Toast.makeText(this,"NFC activated. OK you can pass",Toast.LENGTH_SHORT).show();
			(new Handler()).postDelayed(this::launchNfcActivity, 2000);

		}
	}

	/**
	 * Ask for NFC activation
	 */
	public void activateNfc(){
		Intent activateNfc;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			activateNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
		} else {
			activateNfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
		}
		startActivityForResult(activateNfc,SETTINGS_ACTIVITY_CODE);
	}

	/**
	 * Checks that NFC has been activated
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			checkNfcActivation();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Launch next activity
	 */
	protected void launchNfcActivity(){
		nfcActivity = new Intent(this,NfcActivity.class);
		startActivity(nfcActivity);
	}
}

