package com.example.coffeeplz;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.app.PendingIntent;
import android.nfc.tech.MifareClassic;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class NfcActivity extends AppCompatActivity {

	private NfcAdapter adapter;
	private static final byte[] KEY_A = {(byte)0xa0,(byte)0xa1,(byte)0xa2,(byte)0xa3,(byte)0xa4,(byte)0xa5};
	private static final byte[] KEY_B = {(byte)0x41,(byte)0x5a,(byte)0x54,(byte)0x45,(byte)0x4b,(byte)0x4d};

	private PendingIntent pendingIntent;
	private TextView topMessage;
	private TextView uid;
	private boolean isMifare;
	private MifareClassic mifareTag;

	// Sector 8, 9 ,10 ,11
	private byte[][] payload = new byte[5][];
	private byte[] sector13;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);
		isMifare = false;
		adapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
		topMessage = (TextView) findViewById(R.id.topMessage);
		uid = (TextView) findViewById(R.id.uid);
	}

	@Override
	protected void onResume() {
		super.onResume();
		assert adapter != null;
		//nfcAdapter.enableForegroundDispatch(context,pendingIntent,
		//                                    intentFilterArray,
		//                                    techListsArray)
		adapter.enableForegroundDispatch(this, pendingIntent, null, null);
	}

	protected void onPause() {
		super.onPause();
		//Onpause stop listening
		if (adapter != null) {
			adapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		resolveIntent(intent);
	}

	private void resolveIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			assert tag != null;
			byte[] payload = detectTagData(tag).getBytes();
		}
	}

	//For detection
	private String detectTagData(Tag tag) {
		StringBuilder sb = new StringBuilder();
		byte[] id = tag.getId();
		sb.append("ID (hex): ").append(toHex(id)).append('\n');
		sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');

		String prefix = "android.nfc.tech.";
		sb.append("Technologies: ");
		for (String tech : tag.getTechList()) {
			sb.append(tech.substring(prefix.length()));
			sb.append(", ");
		}

		sb.delete(sb.length() - 2, sb.length());

		for (String tech : tag.getTechList()) {

			if (tech.equals(MifareClassic.class.getName())) {
				sb.append('\n');
				String type = "Unknown";

				try {
					mifareTag = MifareClassic.get(tag);

					switch (mifareTag.getType()) {
						case MifareClassic.TYPE_CLASSIC:
							type = "Classic";
							isMifare = true;
							break;
						case MifareClassic.TYPE_PLUS:
							type = "Plus";
							break;
						case MifareClassic.TYPE_PRO:
							type = "Pro";
							break;
					}
					sb.append("Mifare Classic type: ");
					sb.append(type);
					sb.append('\n');

					sb.append("Mifare size: ");
					sb.append(mifareTag.getSize() + " bytes");
					sb.append('\n');

					sb.append("Mifare sectors: ");
					sb.append(mifareTag.getSectorCount());
					sb.append('\n');

					sb.append("Mifare blocks: ");
					sb.append(mifareTag.getBlockCount());


				} catch (Exception e) {
					sb.append("Mifare classic error: " + e.getMessage());
				}
			}
		}

		uid.setText(toHex(id));
		topMessage.setText("Tag found, deciphering... Plz keep the tag on the phone");


		if (isMifare) {
			authenticate(mifareTag);
		}else{
			topMessage.setText("That is not a mifare Tag you dumb fuck");
		}

		return sb.toString();
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	private void authenticate(MifareClassic tag) {

		try {
			mifareTag.connect();
			if (mifareTag.isConnected()){
				Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
				// Block 0
				if (mifareTag.authenticateSectorWithKeyA(0,MifareClassic.KEY_DEFAULT)){
						int blockIndex=mifareTag.blockToSector(0);
						byte[] block = mifareTag.readBlock(blockIndex);
					}
				// Block 8 -> 11
				for(int i=8;i<12;i++){
					if (mifareTag.authenticateSectorWithKeyB(i,KEY_B)){
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						int blockIndex=mifareTag.sectorToBlock(i);
						for (int j=blockIndex;j<blockIndex+3;j++) {
							out.write(mifareTag.readBlock(j));
						}
						payload[i-8] = out.toByteArray();
					}else{
						Toast.makeText(this,"ERROR : Sector " +i+" Could not be authenticated",Toast.LENGTH_SHORT).show();
					}
				}
				if (mifareTag.authenticateSectorWithKeyB(13,KEY_B)){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					int blockIndex=mifareTag.sectorToBlock(13);
					for (int j=blockIndex;j<blockIndex+3;j++) {
						out.write(mifareTag.readBlock(j));
					}
					payload[4] = out.toByteArray();
					out.close();
				}else{
					Toast.makeText(this,"ERROR : Sector 13 Could not be authenticated",Toast.LENGTH_SHORT).show();
				}
			}
		} catch (IOException e) {
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}

	}

	private String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
			int b = bytes[i] & 0xff;
			if (b < 0x10)
				sb.append('0');
			sb.append(Integer.toHexString(b));
			if (i > 0) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private String toReversedHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; ++i) {
			if (i > 0) {
				sb.append(" ");
			}
			int b = bytes[i] & 0xff;
			if (b < 0x10)
				sb.append('0');
			sb.append(Integer.toHexString(b));
		}
		return sb.toString();
	}

}