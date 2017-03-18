package com.example.android.forisio;

import android.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.LocationListener;
import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreException;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DocumentBodyFactory;
import com.cloudant.sync.datastore.DocumentException;
import com.cloudant.sync.datastore.DocumentRevision;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener{
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    static final String SETTINGS_CLOUDANT_USER = "sachet";
    static final String SETTINGS_CLOUDANT_DB = "mobile_app";
    static final String SETTINGS_CLOUDANT_API_KEY = "anowicentioughtereestani";
    static final String SETTINGS_CLOUDANT_API_SECRET = "6a64c6c33ef7309f0f001b237ec02ccca686a770";
    static final String DOC_TYPE = "com.cloudant.sync.example.task";
    LocationManager locationManager;
    Location mLastLocation;
    double lat;
    double lang;
    TextView latText, langText, sensorText,sampleText;
    int sensorID;
    String contents = null;
    FarmSensor farmSensor;
    Button saveButton;
    Datastore ds;
    private DocumentRevision rev;
    private String type = DOC_TYPE;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latText = (TextView) findViewById(R.id.lat_text);
        langText = (TextView) findViewById(R.id.lang_text);
        sensorText = (TextView) findViewById(R.id.sensor_id_text);
        saveButton = (Button) findViewById(R.id.save_bt);

        farmSensor = new FarmSensor();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION} , PERMISSION_ACCESS_COARSE_LOCATION);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lang = mLastLocation.getLongitude();

            farmSensor.setLat(lat);
            farmSensor.setLang(lang);
            latText.setText(String.valueOf(lat));
            langText.setText(String.valueOf(lang));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private URI createServerURI()
            throws URISyntaxException {
        // We store this in plain text for the purposes of simple demonstration,
        // you might want to use something more secure.
        String username = SETTINGS_CLOUDANT_USER;
        String dbName = SETTINGS_CLOUDANT_DB;
        String apiKey = SETTINGS_CLOUDANT_API_KEY;
        String apiSecret = SETTINGS_CLOUDANT_API_SECRET;
        String host = username + ".cloudant.com";

        // We recommend always using HTTPS to talk to Cloudant.
        return new URI("https", apiKey + ":" + apiSecret, host, 443, "/" + dbName, null, null);
    }

    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence Yes, CharSequence No) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(Yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(No, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    public void scan(View view) {
        Intent intent = new Intent(ACTION_SCAN);
        try {
            intent.putExtra("SCAN_FORMATS", "PRODUCT_MODE,QR_CODE_MODE,CODE_39,CODE_93,CODE_128,DATA_MATRIX,ITF,CODABAR");

            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                contents = intent.getStringExtra("SCAN_RESULT");
                sensorID = Integer.parseInt(contents);
                farmSensor.setSensorID(sensorID);
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Toast toast = Toast.makeText(this, "Sensor ID:" + contents, Toast.LENGTH_LONG);
                toast.show();
                sensorText.setText(contents);
                saveButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void saveSensor(View view) {
        pushSensor(farmSensor);
        saveButton.setVisibility(View.VISIBLE);
    }

    public void pushSensor(FarmSensor farmSensor) {

        File path = getApplicationContext().getDir("datastores", Context.MODE_PRIVATE);
        DatastoreManager manager = new DatastoreManager(path.getAbsolutePath());

        try {
            ds = manager.openDatastore("my_datastore");
            // Create a document
            DocumentRevision revision = new DocumentRevision();
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("SensorID", farmSensor.getSensorID());
            body.put("Latitude", farmSensor.getLat());
            body.put("Longitude", farmSensor.getLang());
            revision.setBody(DocumentBodyFactory.create(body));
            DocumentRevision saved = ds.createDocumentFromRevision(revision);

            // Add an attachment -- binary data like a JPEG
//            UnsavedFileAttachment att1 =
//                    new UnsavedFileAttachment(new File("/path/to/image.jpg"), "image/jpeg");
//            saved.getAttachments().put(att1.name, att1);
//            DocumentRevision updated = ds.updateDocumentFromRevision(saved);
//
//            // Read a document
//            DocumentRevision aRevision = ds.getDocument(updated.getId());
        } catch (DatastoreException datastoreException) {

            // this will be thrown if we don't have permissions to write to the
            // datastore path
            System.err.println("Problem opening datastore: " + datastoreException);
        } catch (DocumentException documentException) {

            // this will be thrown in case of errors performing CRUD operations on
            // documents
            System.err.println("Problem accessing datastore: " + documentException);
        }

        URI uri = null;
        try {
            uri = createServerURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        // Replicate from the local to remote database
        Replicator replicator = ReplicatorBuilder.push().from(ds).to(uri).build();

        // Fire-and-forget (there are easy ways to monitor the state too)
        replicator.start();

        Toast toast = Toast.makeText(this, "Sensor with ID: " + farmSensor.getSensorID() + " is saved successfully", Toast.LENGTH_LONG);
        toast.show();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @SuppressLint("LongLogTag")
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Connection failed: ConnectionResult.getCollectionStatus() = ", String.valueOf(connectionResult.getErrorCode()));
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status,
                        1).show();
            } else {
                Toast.makeText(this, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        lat = mLastLocation.getLatitude();
        lang = mLastLocation.getLongitude();

        farmSensor.setLat(lat);
        farmSensor.setLang(lang);
        latText.setText(String.valueOf(lat));
        langText.setText(String.valueOf(lang));
    }
}