package no.realitylab.chatbot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener {

    private static final int PERMISSION_REQUEST_AUDIO = 0;
    private static final String TAG = "MainActivity";

    private Button listenButton;
    private TextView inputTextView;
    private TextView fulfillmentTextView;
    private TextView actionTextView;
    private AIConfiguration config;
    private AIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listenButton = findViewById(R.id.listenButton);
        fulfillmentTextView = findViewById(R.id.fulfillmentText);
        inputTextView = findViewById(R.id.inputText);
        actionTextView = findViewById(R.id.actionText);
        //TODO: add here your client access token from DialogFlow console
        config = new AIConfiguration("ACCESS_TOKEN",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listenButton.setText(R.string.listening);
                listen();
            }
        });
    }

    private void listen() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            aiService.startListening();
        } else {
            // Permission is missing and must be requested.
            requestAudioPermission();
        }
    }

    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();
        inputTextView.setText(getString(R.string.input , result.getResolvedQuery()));
        fulfillmentTextView.setText(getString(R.string.answer , result.getFulfillment().getSpeech()));
        actionTextView.setText(getString(R.string.intent , result.getMetadata().getIntentName()));
    }

    @Override
    public void onError(AIError error) {
        Log.d(TAG, "Listening error: " + error.getMessage());
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {}

    @Override
    public void onListeningCanceled() {}

    @Override
    public void onListeningFinished() {
        listenButton.setText(R.string.ask);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_AUDIO) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                aiService.startListening();
            } else {
                Log.d(TAG, "permission denied");
            }
        }
    }

    private void requestAudioPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(findViewById(R.id.main_container), getString(R.string.permission_text_audio),
                    Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            PERMISSION_REQUEST_AUDIO);
                }
            }).show();

        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_AUDIO);
        }
    }

}
