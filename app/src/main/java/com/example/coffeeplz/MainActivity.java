package com.example.coffeeplz;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private boolean nfcIsActivated = false;
	private boolean isFirstRun = true;
	private static final int SETTINGS_ACTIVITY_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		checkNfcActivation();
	}

	/**
	 * Checks NFC activation
	 */
	public boolean checkNfcActivation() {

		NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
		if (nfc == null){
		// you are fucked
		Toast toast = Toast.makeText(this,"No NFC capability on this device, go buy a real phone",Toast.LENGTH_LONG);
		toast.show();
			nfcIsActivated = false;
		} else if (!nfc.isEnabled()){
			if (isFirstRun) {
				Toast toast = Toast.makeText(this, "NFC must be activated", Toast.LENGTH_SHORT);
				toast.show();
			}else{
				Toast toast = Toast.makeText(this, "ACTIVATE NFC YOU DUMB FUCK", Toast.LENGTH_SHORT);
				toast.show();
			}
			// NFC present but not activated
			(new Handler()).postDelayed(this::activateNfc,1000);
			nfcIsActivated = false;
			isFirstRun = false;
		} else {
			// NFC activated -> switch to next activity
			Toast toast = Toast.makeText(this,"NFC activated",Toast.LENGTH_SHORT);
			toast.show();
			nfcIsActivated = true;
		}
		return nfcIsActivated;
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
		checkNfcActivation();
	}
}

