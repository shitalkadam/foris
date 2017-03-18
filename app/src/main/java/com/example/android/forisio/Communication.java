package com.example.android.forisio;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static android.R.id.list;

public class Communication extends AppCompatActivity implements OnClickListener, OnInitListener {

    private static final int VR_REQUEST = 999;
    private final String LOG_TAG = "SpeechRepeatActivity";
    TextView speechText,outputText;
    Button speechBtn;
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech repeatTTS;
    String speechToText,queryString;
    StringBuilder sb;
    private final TextWatcher mTextEditorWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // When No Password Entered
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            speechToText = speechText.getText().toString().replace(" ","+");

            queryString = analyzeSpeech(speechToText);

            if(queryString.equals("soil") || queryString.equals("weather")) {
                String url = "http://foris.mybluemix.net/HelloWorld?q=" + queryString;
                Log.d("URL", url);
                new RetrieveFeedTask().execute(url);
            }

            else
            {
                outputText.setText("No matched output for your question");
                repeatTTS.speak("No matched output for your question, Try different question" , TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    };

    public String analyzeSpeech(String speechvalue){

        if(speechvalue.contains("soil"))
            speechvalue = "soil";

        if(speechvalue.contains("temperature")  && !speechvalue.contains("soil"))
            speechvalue = "weather";

        if(speechvalue.contains("soil") && (speechvalue.contains("temperature") || speechvalue.contains("salinity") ||
                speechvalue.contains("ph") || speechvalue.contains("moisture")))
            speechvalue = "soil";

        if(speechvalue.contains("weather"))
            speechvalue = "weather";

        return speechvalue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        speechBtn = (Button) findViewById(R.id.mic_bt);
        speechText = (TextView) findViewById(R.id.speech_label);
        outputText = (TextView) findViewById(R.id.output_label);

        PackageManager packManager = getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        if (intActivities.size() != 0) {
            //speech recognition is supported - detect user button clicks
            speechBtn.setOnClickListener(this);
            //prepare the TTS to repeat chosen words
            Intent checkTTSIntent = new Intent();
            //check TTS data
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            //start the checking Intent - will retrieve result in onActivityResult
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        } else {
            //speech recognition not supported, disable button and output message
            speechBtn.setEnabled(false);
            Toast.makeText(this, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }
        speechText.addTextChangedListener(mTextEditorWatcher);
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String> {
        private Exception exception;
        protected String doInBackground(String... urls) {

            String url = urls[0];

            try {

                HttpClient client = new DefaultHttpClient();
                Log.d("Msg:", "Object Created");
                HttpGet request = new HttpGet(url);
                // replace with your url
                Log.d("Msg:", "Get executed");
                HttpResponse response = null;
                try {

                    response = client.execute(request);
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                    sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    Log.d("Response of GET request", sb.toString());

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return sb.toString();
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String str) {
            if(sb.toString().isEmpty()){
                outputText.setText("No matched output for your question");
                repeatTTS.speak("No matched output for your question" , TextToSpeech.QUEUE_FLUSH, null);

            }
            else {
                outputText.setText(sb.toString());
                repeatTTS.speak("" + sb.toString(), TextToSpeech.QUEUE_FLUSH, null);

            }
        }
    }

    @Override
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS)
            repeatTTS.setLanguage(Locale.US);
    }

    @Override
    public void onClick(View v) {
        outputText.setText("");
        listenToSpeech();
    }

    private void listenToSpeech() {
        //start the speech recognition intent passing required data
        Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //indicate package
        listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        //message to display while listening
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask question");
        //set speech model
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        //start listening
        startActivityForResult(listenIntent, VR_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check speech recognition result
        if (requestCode == VR_REQUEST && resultCode == RESULT_OK) {
            //store the returned word list as an ArrayList
            ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //set the retrieved list to display in the ListView using an ArrayAdapter
            speechText.setText(suggestedWords.get(0));
            //    wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, suggestedWords));
        }

        //tss code here
        if (requestCode == MY_DATA_CHECK_CODE) {
            //we have the data - create a TTS instance
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                repeatTTS = new TextToSpeech(getApplicationContext(), this);

            }
            //data not installed, prompt the user to install it
            else {
                //intent will take user to TTS download page in Google Play
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
        //call superclass method
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repeatTTS.shutdown();
    }
}
