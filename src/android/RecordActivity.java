package com.example.bmu.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import android.Manifest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class RecordActivity extends Activity {

    private static final String TAG = "Recorder";
    private static final int DURATION = 6000;

    private ArrayAdapter<String> _delayAdapter;
    private int _delay = 0;

    private int _cameraOrientation;
    private Camera _camera;
    private boolean _isRecording = false;
    private MediaRecorder _recorder;
    private int _orientation;
    private CamcorderProfile _profile;
    private String _outputFile;

    private boolean _timerRunning = false;
    private long _startTime = 0;
    private Handler _timerHandler = new Handler();
    private Runnable _timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - _startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            long ms = millis - seconds * 1000;
            seconds = seconds % 60;

            if (millis > DURATION)
            {
                stopTimer();
                _recorder.stop();
                releaseMediaRecorder();

                success();
            }
            else {
                _timerDisplay.setText(String.format("%02d:%02d:%03d", minutes, seconds, ms));
            }

            if (_timerRunning) {
                _timerDisplay.postDelayed(this, 10);
            }
        }
    };

    private ImageButton _capture;
    private LinearLayout _delayContainer;
    private Spinner _delayList;
    private TextureView _preview;
    private TextView _timerDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        _capture = (ImageButton) findViewById(R.id.capture);
        _delayContainer = (LinearLayout) findViewById(R.id.delay_container);
        _delayList = (Spinner) findViewById(R.id.delay_list);
        _preview = (TextureView) findViewById(R.id.preview);
        _timerDisplay = (TextView) findViewById(R.id.timer_display);

        this.setupDelay();

        int orientation = getScreenOrientation();

        RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) _capture.getLayoutParams();

        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
    }

    private void requestPermission() {
        String[] permissions = new String[]
        {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        for (int i = 0; i < permissions.length; i++)
        {
           if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED)
           {
               ActivityCompat.requestPermissions(this, permissions, 1);
               return;
           }
        }

        new MediaPrepareTask().execute(null, null, null);
    }

    private void setupDelay()
    {
        String delays[] = { "0s", "3s", "5s", "10s"};
        _delayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_view, delays);
        _delayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _delayList.setAdapter(_delayAdapter);

        _delayList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _delay = Integer.parseInt(_delayAdapter.getItem(position).toString().replace("s", "")) * 1000;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++)
        {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
            {
                this.setResult(Activity.RESULT_CANCELED);
                this.finish();
                break;
            }
        }

        new MediaPrepareTask().execute(null, null, null);
    }

    public void onCaptureClick(View view) {
        if (_isRecording) {
            _recorder.stop();

            releaseMediaRecorder();

            //_capture.setText("Capture");
            _isRecording = false;

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            _orientation = getScreenOrientation();
            setRequestedOrientation(_orientation);

            _isRecording = true;

            hideControls();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startRecording();
                    startTimer();
                }
            }, _delay);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        requestPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseMediaRecorder();
        releaseCamera();

        stopTimer();

        _isRecording = false;
        //_capture.setText("Capture");
    }

    private void startTimer()
    {
        _timerRunning = true;
        _startTime = System.currentTimeMillis();
        _timerHandler.postDelayed(_timerRunnable, 0);
    }

    private void stopTimer()
    {
        _timerDisplay.setText("");
        _timerRunning = false;
        _timerHandler.removeCallbacks(_timerRunnable);
    }

    private void hideControls()
    {
        Animation anim = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(300);
        anim.setFillAfter(true);

        _capture.startAnimation(anim);

        _delayContainer.setVisibility(View.GONE);
    }

    private int calcCameraRotation() {
        int orientation = getScreenOrientation();

        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            return 270;
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            return 180;
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            return 0;
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            return 90;
        }

        return 0;
    }

    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    private void releaseMediaRecorder() {
        if (_recorder != null) {
            _recorder.reset();
            _recorder.release();
            _recorder = null;

            _camera.lock();
        }
    }

    private void releaseCamera() {
        if (_camera != null) {
            _camera.release();
            _camera = null;
        }
    }

    private void startRecording() {
        _recorder = new MediaRecorder();

        _recorder.setCamera(_camera);
        _recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        _recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        _recorder.setProfile(_profile);

        _recorder.setVideoFrameRate(_profile.videoFrameRate);
        _recorder.setVideoEncodingBitRate(_profile.videoBitRate);

        _outputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO).toString();
        _recorder.setOutputFile(_outputFile);
        _recorder.setOrientationHint(_cameraOrientation);

        try {
            _recorder.prepare();
            _recorder.start();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
        }
    }

    private void success()
    {
        Intent intent = new Intent();
        intent.putExtra("file", _outputFile);
        
        this.setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean prepareVideoRecorder() {
        _camera = CameraHelper.getDefaultCameraInstance();

        Camera.Parameters parameters = _camera.getParameters();
        List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalPreviewSize(supportedSizes, _preview.getWidth(), _preview.getHeight());

        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH_SPEED_HIGH)) {
            _profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH_SPEED_HIGH);
        }
        else {
            _profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        }

        _profile.videoFrameWidth = optimalSize.width;
        _profile.videoFrameHeight = optimalSize.height;
        _profile.videoFrameRate = 120;

        List<Camera.Size> a = _camera.getParameters().getSupportedPreviewSizes();
        List<int[]> b = _camera.getParameters().getSupportedPreviewFpsRange();

        parameters.setPreviewSize(_profile.videoFrameWidth, _profile.videoFrameHeight);
        _camera.setParameters(parameters);

        try {
            _cameraOrientation = this.calcCameraRotation();
            _camera.setDisplayOrientation(_cameraOrientation);
            _camera.setPreviewTexture(_preview.getSurfaceTexture());
            _camera.startPreview();
        }
        catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }

        _camera.unlock();

        return true;
    }

    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            if (prepareVideoRecorder()) {
                return true;
            }
            else {
                releaseMediaRecorder();

                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                this.setResult(Activity.RESULT_CANCELED);
                RecordActivity.this.finish();
            }
        }
    }
}