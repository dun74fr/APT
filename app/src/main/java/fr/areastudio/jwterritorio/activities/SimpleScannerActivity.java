package fr.areastudio.jwterritorio.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.activeandroid.query.Select;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Date;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.Publisher;
import fr.areastudio.jwterritorio.model.Territory;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private String TAG = "SimpleScannerActivity";
    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        String result = rawResult.getText();
        if (!result.startsWith("jwterr://")) {
            return;
        }
        result = result.replace("jwterr://", "");
        if (result.contains("acknowledge")) {
            result = result.replace("acknowledge?", "");
            String[] parts = result.split("&user=");
            String uuid = parts[1];
            String[] uuids = parts[0].split(",");
            Publisher p = new Select().from(Publisher.class).where("uuid = ?", uuid).executeSingle();
            for (int i = 0; i < uuids.length; i++) {
                Territory t = new Select().from(Territory.class).where("uuid = ?", uuids[i]).executeSingle();
                Publisher me = new Select().from(Publisher.class).where("email = ?",settings.getString("user","")).executeSingle();
                t.assignedPub = p;
                t.save();
                DbUpdate dbUpdate = new DbUpdate();
                dbUpdate.date = new Date();
                dbUpdate.model = "TERRITORY";
                dbUpdate.updateType = "UPDATE";
                dbUpdate.publisherUuid = me.uuid;
                dbUpdate.uuid = t.uuid;
                dbUpdate.save();

            }
            finish();
        } else {
            String[] uuids = result.split(",");
            for (int i = 0; i < uuids.length; i++) {
                Territory t = new Select().from(Territory.class).where("uuid = ?", uuids[i]).executeSingle();
                Publisher me = new Select().from(Publisher.class).where("email = ?",settings.getString("user","")).executeSingle();
                t.assignedPub = me;
                t.save();
                DbUpdate dbUpdate = new DbUpdate();
                dbUpdate.date = new Date();
                dbUpdate.model = "TERRITORY";
                dbUpdate.updateType = "UPDATE";
                dbUpdate.publisherUuid = ((MyApplication) getApplication()).getMe().uuid;
                dbUpdate.uuid = t.uuid;
                dbUpdate.save();
            }
            Dialog dialog = new Dialog(this);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.confirm_assign_dialog);
            String text = "";

            text = "jwterr://" + "acknowledge?" + result + "&user=" + ((MyApplication) getApplication()).getMe().uuid;
            ImageView qr = dialog.findViewById(R.id.qr);

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 400, 400);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                qr.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
            dialog.show();

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                }
            });
        }
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }
}