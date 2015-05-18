package com.maxwell.bodysensor.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxwell.bodysensor.CameraActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.ui.SurfaceViewCamera;
import com.maxwell.bodysensor.ui.WarningUtil;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class FCamera extends Fragment implements SurfaceHolder.Callback {

    private static final int FILE_TYPE_IMAGE = 0;
    private static final int FILE_TYPE_VIDEO = 1;

    // The rotation degrees are suitable for landscape phone orientation only.
    private static final int ROTATION_DEGREE_BACK_CAMERA_LANDSCAPE = 0,
            ROTATION_DEGREE_BACK_CAMERA_PORTRAIT = 90,
            ROTATION_DEGREE_FRONT_CAMERA_LANDSCAPE = 0,
            ROTATION_DEGREE_FRONT_CAMERA_PORTRAIT = -90,
            CAMERA_PICTURE_SIZE_MAX = 3000 * 2000,  // Will cause Out-Of-Memory error if too large.
            CAMERA_PICTURE_SIZE_MIN = 640 * 480,  // 800 * 600,
            CAMERA_VIDEO_SIZE_MAX = 1500 * 1000,  // 720 * 480,
            CAMERA_VIDEO_SIZE_MIN = 600 * 400,
            CAMERA_VIDEO_WIDTH_DEFAULT = 720,
            CAMERA_VIDEO_HEIGHT_DEFAULT = 480,
            CAMERA_PREVIEW_SIZE_MAX = 3000 * 2000,
            CAMERA_PREVIEW_SIZE_MIN = 640 * 480;  // 800 * 600;

    public final static int MSG_UPDATE_DURATION_RECORDING_VIDEO = 100;

    private Context mContext;

    private ImageButton mImgBtnSwitchBackFrontCamera,
            mImgBtnOpenGallery;
    private ImageView mImgViewRecordingVideo;
    private TextView mTxtDurationRecordingVideo,
            mTxtDurationRecordingVideoRotate90;
    private SurfaceViewCamera mSurfViewCamera;

    public enum MEDIA_FILE_TYPE {
        PICTURE, VIDEO
    }
    private MEDIA_FILE_TYPE mCameraType = MEDIA_FILE_TYPE.PICTURE;

    private Camera mCamera;
    private int miCameraRotationDegree;
    private SurfaceHolder mSurfHolder;
    private boolean mbSupportJpeg = false,
            mbUsingBackCamera = true;
    private Size mBestVideoSize;

    // Rotation degrees for back/front cameras.
    // Back camera: 90, front camera: -90.
    private int miPictureRotationDegree;

    private SensorManager mSensorMgr;
    private Sensor mAcceleSensor, mMagSensor;
    private float[] mfArrAccSensorValues, mfArrMagSensorValues;

    private enum PHONE_ORIENTATION {
        PORTRAIT, LANDSCAPE
    }
    private PHONE_ORIENTATION mPhoneOrientation;
    private boolean mbIsFirstTimePhoneOrientation = true;  // To sync image buttons and phone orientation.

    // For recording video.
    private MediaRecorder mMediaRecorder;
    private boolean mbIsRecordingVideo = false;
    private File mFileRecordedVideo;
    private int mDurationRecordingInSecond;

    // Rotation animation to show/hide duration of recording video.
    private Animation mAnimRotate90Clockwise,
            mAnimRotate90Counterclockwise;

    // Options for getBestSize().
    private enum SIZE_TYPE {
        VIDEO, PICTURE, PREVIEW
    }

    private int miSurfaceWidth, miSurfaceHeight;

    // Samsung phones automatically focus before taking picture, so
    // AutoFocusCallback will be called iteratively. We use a
    // flag to avoid this.
    private boolean mbTakePictureAfterFocus;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_DURATION_RECORDING_VIDEO:

                    mDurationRecordingInSecond++;

                    UtilCalendar cal = new UtilCalendar(null);
                    cal.setTimeInMillis(mDurationRecordingInSecond * 1000);
                    Date date = new Date(cal.getTimeInMillis() - TimeZone.getDefault().getRawOffset());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

                    String strDuration = timeFormat.format(date);
                    mTxtDurationRecordingVideo.setText(strDuration);
                    mTxtDurationRecordingVideoRotate90.setText(strDuration);

                    sendEmptyMessageDelayed(MSG_UPDATE_DURATION_RECORDING_VIDEO, 1000);

                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getArguments();
        if (b.getBoolean(CameraActivity.KEY_IS_VIDEO_RECORDER)) {
            mCameraType = MEDIA_FILE_TYPE.VIDEO;
        } else {
            mCameraType = MEDIA_FILE_TYPE.PICTURE;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();

        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        mSurfViewCamera = (SurfaceViewCamera) view.findViewById(R.id.preview_camera);
        mSurfHolder = mSurfViewCamera.getHolder();
        mSurfHolder.addCallback(this);
//        mSurfHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Prepare phone's sensors.
        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAcceleSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Setup UI.
        mImgViewRecordingVideo = (ImageView) view.findViewById(R.id.img_recording_video);

        mImgBtnSwitchBackFrontCamera = (ImageButton) view.findViewById(R.id.imgb_switch_camera);
        mImgBtnSwitchBackFrontCamera.setOnClickListener(imgBtnSwitchBackFrontCameraOnClick);

        // If there is only one camera, hide the switch camera button.
        if (Camera.getNumberOfCameras() == 1)
            mImgBtnSwitchBackFrontCamera.setVisibility(View.GONE);

        mImgBtnOpenGallery = (ImageButton) view.findViewById(R.id.imgb_gallery);
        mImgBtnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do nothing if we are recording video.
                if (mbIsRecordingVideo)
                    return;

                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setType("image/*");
                startActivity(it);
            }
        });

        mTxtDurationRecordingVideo = (TextView) view.findViewById(R.id.txt_recording_duration);
        mTxtDurationRecordingVideo.setVisibility(View.INVISIBLE);
        mTxtDurationRecordingVideoRotate90 = (TextView) view.findViewById(R.id.txt_recording_duration_90);
        mTxtDurationRecordingVideoRotate90.setVisibility(View.INVISIBLE);

        // Prepare rotation animations.
        mAnimRotate90Clockwise = AnimationUtils.loadAnimation(mContext,
                R.anim.tween_anim_rotate_90_clockwise);
        mAnimRotate90Clockwise.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mbIsRecordingVideo) {
                    mTxtDurationRecordingVideo.setVisibility(View.INVISIBLE);
                    mTxtDurationRecordingVideoRotate90.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationStart(Animation animation) {}

        });

        mAnimRotate90Counterclockwise = AnimationUtils.loadAnimation(mContext,
                R.anim.tween_anim_rotate_90_counterclockwise);
        mAnimRotate90Counterclockwise.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mbIsRecordingVideo) {
                    mTxtDurationRecordingVideo.setVisibility(View.VISIBLE);
                    mTxtDurationRecordingVideoRotate90.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationStart(Animation animation) {}

        });

        // Update title and button's image according to the smart key setting.
        switch (mCameraType) {
            case VIDEO:
                mImgBtnOpenGallery.setImageResource(R.drawable.df_camera_open_gallery_for_video);
                break;
            default:
                mImgBtnOpenGallery.setImageResource(R.drawable.df_camera_open_gallery_for_photo);
        }

        return view;
    }

    @Override
    public void onResume() {
        // onResume() is called whenever this Fragment come to front.
        // surfaceCreated() is called after onResume().
        mSensorMgr.registerListener(sensorEventListener, mAcceleSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorMgr.registerListener(sensorEventListener, mMagSensor, SensorManager.SENSOR_DELAY_NORMAL);

        super.onResume();
    }

    @Override
    public void onPause() {
        mSensorMgr.unregisterListener(sensorEventListener);

        super.onPause();
    }

    private boolean setupCamera(int iSurfaceWidth, int iSurfaceHeight) {
        try {
            mCamera.setPreviewDisplay(mSurfHolder);

            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(0, camInfo);

            int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0; break;
                case Surface.ROTATION_90:
                    degrees = 90; break;
                case Surface.ROTATION_180:
                    degrees = 180; break;
                case Surface.ROTATION_270:
                    degrees = 270; break;
            }

            miCameraRotationDegree = (camInfo.orientation - degrees + 360) % 360;
            mCamera.setDisplayOrientation(miCameraRotationDegree);

            setFocusMode();

            Camera.Parameters camParas = mCamera.getParameters();

            mBestVideoSize = getBestSize(SIZE_TYPE.VIDEO, CAMERA_VIDEO_SIZE_MAX, CAMERA_VIDEO_SIZE_MIN,
                    camParas, iSurfaceWidth, iSurfaceHeight);

            List<String> listCameraMode;

            // Set Flash mode.
            listCameraMode = camParas.getSupportedFlashModes();
            if (listCameraMode != null && listCameraMode.contains(Camera.Parameters.FLASH_MODE_AUTO))
                camParas.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            else
                camParas.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

            // Set picture format.
            List<Integer> listCamerPicFormat = camParas.getSupportedPictureFormats();
            if (listCamerPicFormat != null && listCamerPicFormat.contains(ImageFormat.JPEG)) {
                camParas.setPictureFormat(ImageFormat.JPEG);
                mbSupportJpeg = true;
            }

            // Set picture size.
            Size bestPictureSize = getBestSize(SIZE_TYPE.PICTURE, CAMERA_PICTURE_SIZE_MAX, CAMERA_PICTURE_SIZE_MIN,
                    camParas, iSurfaceWidth, iSurfaceHeight);
            camParas.setPictureSize(bestPictureSize.width, bestPictureSize.height);

            // Set preview size.
            Size bestPreviewSize = getBestSize(SIZE_TYPE.PREVIEW, CAMERA_PREVIEW_SIZE_MAX, CAMERA_PREVIEW_SIZE_MIN,
                    camParas, iSurfaceWidth, iSurfaceHeight);
            camParas.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

            mCamera.setParameters(camParas);
            mCamera.startPreview();
            mCamera.autoFocus(null);
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(mContext, "Camera error!", Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        return true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Debug only.
//		Toast.makeText(mContext, "surfaceChanged()", Toast.LENGTH_LONG)
//				.show();

        miSurfaceWidth = width;
        miSurfaceHeight = height;

        // Destroy the fragment if error happens.
        if (!setupCamera(miSurfaceWidth, miSurfaceHeight)) {
            UtilDBG.e("Exception : setupCamera error!!");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // surfaceCreated() is called whenever this Fragment comes to front.
        // onResume() is called before surfaceCreated().

        // Enable camera according to mbUsingBackCamera.
        if (mbUsingBackCamera)
            mCamera = Camera.open();
        else
            mCamera = Camera.open(1);

        mMediaRecorder = new MediaRecorder();

        mSurfViewCamera.setCamera(mCamera);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mbIsRecordingVideo)
            stopRecordingVideo();

        mMediaRecorder.release();

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private Camera.AutoFocusCallback cameraAutoFocusCallBack = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
//			Toast.makeText(mContext, "AutoFocusCallback.onAutoFocus()", Toast.LENGTH_LONG)
//					.show();

            try {
                if (!mbTakePictureAfterFocus) {
                    // We have to cancel the callback, or Samsung phones will call
                    // it iteratively.
                    mCamera.autoFocus(null);
                    return;
                }

                mbTakePictureAfterFocus = false;  // Reset the flag.

                if (mbSupportJpeg)
                    mCamera.takePicture(camShutterCallback, null, camPictureCallbackJpeg);
                else
                    mCamera.takePicture(camShutterCallback, camPictureCallbackRawData, null);
            } catch (Exception e) {
                Toast.makeText(mContext, "Error in AutoFocusCallback.onAutoFocus()", Toast.LENGTH_LONG)
                        .show();
            }
        }
    };

    public void onSmartKeyTrigged() {
        switch(mCameraType) {
            case VIDEO:
                if (mbIsRecordingVideo) {
                    stopRecordingVideo();
                } else {
                    startRecordingVideo();
                }
                break;
            default:
                takePicture();
        }
    }

    private void takePicture() {
        // Do re-focus to get the best result.
        // If the camera support auto-focus, the callback is called after finishing focus.
        // Otherwise, the callback is called immediately.
        try {
            Camera.Parameters camParas = mCamera.getParameters();
            List<String> listCameraMode = camParas.getSupportedFocusModes();

            if (listCameraMode != null && listCameraMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // Debug only.
//				Toast.makeText(mContext, "FOCUS_MODE_AUTO", Toast.LENGTH_LONG)
//						.show();

                camParas.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(camParas);
            }

            // Samsung phones automatically focus before taking picture, so
            // AutoFocusCallback will be called iteratively. We use a
            // flag to avoid this.
            mbTakePictureAfterFocus = true;

            mCamera.autoFocus(cameraAutoFocusCallBack);
        } catch (Exception e) {
            Toast.makeText(mContext, "Error in takePicture()", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private ShutterCallback camShutterCallback = new ShutterCallback() {
        @Override
        public void onShutter() {
            // Set picture rotation degree according to phone's orientation
            // and the current camera.
            try {
                if (mbUsingBackCamera) {
                    switch (mPhoneOrientation) {
                        case PORTRAIT:
                            miPictureRotationDegree = ROTATION_DEGREE_BACK_CAMERA_PORTRAIT;
                            break;
                        case LANDSCAPE:
                            miPictureRotationDegree = ROTATION_DEGREE_BACK_CAMERA_LANDSCAPE;
                            break;
                    }
                } else {
                    switch (mPhoneOrientation) {
                        case PORTRAIT:
                            miPictureRotationDegree = ROTATION_DEGREE_FRONT_CAMERA_PORTRAIT;
                            break;
                        case LANDSCAPE:
                            miPictureRotationDegree = ROTATION_DEGREE_FRONT_CAMERA_LANDSCAPE;
                            break;
                    }
                }

                AudioManager mgr = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);

                /*MediaPlayer mp = MediaPlayer.create(mContext, R.raw.sound_camera_shutter);
                mp.start();*/
            } catch (Exception e) {
                Toast.makeText(mContext, "Error in ShutterCallback.onShutter()", Toast.LENGTH_LONG)
                        .show();
            }
        }
    };

    private PictureCallback camPictureCallbackRawData = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                rotatePictureAndSave(data, false);
            } catch (OutOfMemoryError e) {
                showDlgErrorCreateBitmap();
            } catch (Exception e) {
                showDlgErrorSavePicture();
            } finally {
                setFocusMode();  // Reset focus mode to default.
                mCamera.startPreview();
            }
        }
    };

    private PictureCallback camPictureCallbackJpeg = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                rotatePictureAndSave(data, true);
            } catch (OutOfMemoryError e) {
                showDlgErrorCreateBitmap();
            } catch (Exception e) {
                showDlgErrorSavePicture();
            } finally {
                setFocusMode();  // Reset focus mode to default.
                mCamera.startPreview();
            }
        }
    };

    private File getOutputFile(MEDIA_FILE_TYPE fileType) {
        File fileStoragePath, file;
        String timeStamp;
        switch (fileType) {
            case PICTURE:
                // Prepare storage path.
                fileStoragePath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM);

                // Prepare file name.
                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                file = new File(fileStoragePath.getPath() + File.separator +
                        "PICTURE_"+ timeStamp + ".jpg");
                break;
            default:
                // Prepare storage path.
                fileStoragePath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM);

                // Prepare file name.
                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                file = new File(fileStoragePath.getPath() + File.separator +
                        "VIDEO_"+ timeStamp + ".mpg");
        }

        return file;
    }

    private void showDlgErrorSavePicture() {
        WarningUtil.showDFMessageOK(
                (FragmentActivity) mContext,
                R.string.dlg_error_title,
                R.string.dlg_error_save_picture_file_description);
    }

    private void showDlgErrorCreateBitmap() {
        WarningUtil.showDFMessageOK(
                (FragmentActivity) mContext,
                R.string.dlg_error_title,
                R.string.dlg_error_create_bitmap_description);
    }

    private void showDlgErrorCamera() {
        WarningUtil.showDFMessageOK(
                (FragmentActivity) mContext,
                R.string.dlg_error_title,
                R.string.dlg_error_camera_description);
    }

    private void addPictureToGallery(File file, int fileType) {
        Intent it = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(file.getPath());
        Uri contentUri = Uri.fromFile(f);
        it.setData(contentUri);
        mContext.sendBroadcast(it);
        int toastStringId = R.string.image_save_success;
        switch (fileType) {
            case FILE_TYPE_IMAGE:
                toastStringId = R.string.image_save_success;
                break;
            case FILE_TYPE_VIDEO:
                toastStringId = R.string.video_save_success;
                break;
        }
        Toast.makeText(mContext, toastStringId, Toast.LENGTH_LONG).show();
    }

    private View.OnClickListener imgBtnSwitchBackFrontCameraOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // Do nothing if we are recording video.
            if (mbIsRecordingVideo)
                return;

            if (mbUsingBackCamera) {  // Switch to front camera.
                mCamera.stopPreview();
                mCamera.release();
                mCamera = Camera.open(1);
                mbUsingBackCamera = false;
            } else {  // Switch to back camera.
                mCamera.stopPreview();
                mCamera.release();
                mCamera = Camera.open();
                mbUsingBackCamera = true;
            }

            mSurfViewCamera.setCamera(mCamera);

            // Debug only.
//			Toast.makeText(mContext, "imgBtnSwitchBackFrontCameraOnClick -> setupCamera()", Toast.LENGTH_LONG)
//					.show();

            // Destroy the fragment if error happens.
            if (!setupCamera(miSurfaceWidth, miSurfaceHeight)) {
                UtilDBG.e("Exception : setupCamera error!!");
            }
        }
    };

    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // Check if this is the sensor values we want.
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mfArrAccSensorValues = event.values;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mfArrMagSensorValues = event.values;
                    break;
                default:
                    return;		// Not sensor value we want.
            }

            // mfArrAccSensorValues[0]: acceleration on the x-axis.
            // mfArrAccSensorValues[1]: acceleration on the y-axis.
            // mfArrAccSensorValues[2]: acceleration on the z-axis.
            // Example:
            // When the device lies flat on a table and is pushed on its left side toward the right, the x acceleration value is positive.
            // When the device lies flat on a table, the acceleration value is +9.81.
            // When the device lies flat on a table and is pushed toward the sky with an acceleration of A m/s^2, the acceleration value is equal to A+9.81.

            // Sometimes sensor sends an event with null values when launching app.
            double dAccDevice;
            try {
                dAccDevice = Math.sqrt(
                        mfArrAccSensorValues[0] * mfArrAccSensorValues[0] +
                                mfArrAccSensorValues[1] * mfArrAccSensorValues[1] +
                                mfArrAccSensorValues[2] * mfArrAccSensorValues[2]);
            } catch (Exception e) {
                return;
            }

            // Check if sensor values are all ready for further calculation.
            if (mfArrAccSensorValues == null || mfArrMagSensorValues == null)
                return;

            // Call getRotationMatrix() to get the rotation matrix first.
            float[] rotationMatrix = new float[9];
            if (!SensorManager.getRotationMatrix(
                    rotationMatrix, null, mfArrAccSensorValues, mfArrMagSensorValues))
                return;		// Fail.

            // Remap the rotation matrix.
            float[] rotationMatrixRemapped = new float[9];
            int iDeviceRotationDegree = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
            switch (iDeviceRotationDegree) {
                case Surface.ROTATION_0:	// Screen is upright (portrait).
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_MINUS_X, SensorManager.AXIS_Y,
                            rotationMatrixRemapped);
                    break;
                default:
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                            rotationMatrixRemapped);
            }

            // Now ready to calculate orientation.
            // orientationValues[0] is rotation degree on the plane.
            // North: 0 degree; South: 180/-180 degree;
            // East: 90 degree; West: -90 degree.
            float[] orientationValues = new float[3];
            SensorManager.getOrientation(rotationMatrixRemapped, orientationValues);
//			Log.d("qqq", "Orientation values[0]: " + orientationValues[0] +
//						 "\nOrientation values[1]: " + orientationValues[1] +
//						 "\nOrientation values[2]: " + orientationValues[2]);

            if (mbIsFirstTimePhoneOrientation) {
                if(Math.abs((int)(orientationValues[1]*90)) > 30) {  // Phone is portrait.
                    // The image buttons are portrait at beginning, so don't do rotation.
                    mPhoneOrientation = PHONE_ORIENTATION.PORTRAIT;
                } else {  // Phone is landscape, so rotate the image buttons.
                    mPhoneOrientation = PHONE_ORIENTATION.LANDSCAPE;
                    rotateUIComponent(mPhoneOrientation);
                }

                mbIsFirstTimePhoneOrientation = false;
            } else {  // After first time phone orientation.
                if(Math.abs((int)(orientationValues[1]*90)) > 30) {
                    if (mPhoneOrientation != PHONE_ORIENTATION.PORTRAIT) {
                        mPhoneOrientation = PHONE_ORIENTATION.PORTRAIT;
                        rotateUIComponent(mPhoneOrientation);
                    }
                } else if(Math.abs((int)(orientationValues[0]*90)) > 30) {
                    if (mPhoneOrientation != PHONE_ORIENTATION.LANDSCAPE) {
                        mPhoneOrientation = PHONE_ORIENTATION.LANDSCAPE;
                        rotateUIComponent(mPhoneOrientation);
                    }
                } else {
                    if (mPhoneOrientation != PHONE_ORIENTATION.LANDSCAPE) {
                        mPhoneOrientation = PHONE_ORIENTATION.LANDSCAPE;
                        rotateUIComponent(mPhoneOrientation);
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void rotateUIComponent(PHONE_ORIENTATION phoneOrientation) {
        switch (phoneOrientation) {
            case PORTRAIT:
                // Use View Animation instead. Property animation will cause
                // components to disappear after switching app to background and coming back to foreground.
                mImgViewRecordingVideo.startAnimation(
                        AnimationUtils.loadAnimation(mContext,
                                R.anim.tween_anim_rotate_90_counterclockwise));
                mImgBtnSwitchBackFrontCamera.startAnimation(
                        AnimationUtils.loadAnimation(mContext,
                                R.anim.tween_anim_rotate_90_counterclockwise));
                mImgBtnOpenGallery.startAnimation(mAnimRotate90Counterclockwise);
                break;
            default:  // LANDSCAPE.
                // Use View Animation instead. Property animation will cause
                // components to disappear after switching app to background and coming back to foreground.
                mImgViewRecordingVideo.startAnimation(
                        AnimationUtils.loadAnimation(mContext,
                                R.anim.tween_anim_rotate_90_clockwise));
                mImgBtnSwitchBackFrontCamera.startAnimation(
                        AnimationUtils.loadAnimation(mContext,
                                R.anim.tween_anim_rotate_90_clockwise));
                mImgBtnOpenGallery.startAnimation(mAnimRotate90Clockwise);
		}
    }

    private void startRecordingVideo() {
        mCamera.stopPreview();
        mCamera.unlock();

        mFileRecordedVideo = getOutputFile(MEDIA_FILE_TYPE.VIDEO);

        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile camProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        if (camProfile != null)
            mMediaRecorder.setProfile(camProfile);
        else {
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // Parameters to determine video quality.
            if (mBestVideoSize != null)
                mMediaRecorder.setVideoSize(mBestVideoSize.width, mBestVideoSize.height);
            else
                mMediaRecorder.setVideoSize(CAMERA_VIDEO_WIDTH_DEFAULT, CAMERA_VIDEO_HEIGHT_DEFAULT);

            mMediaRecorder.setVideoFrameRate(15);
            mMediaRecorder.setVideoEncodingBitRate(3000000);

            // Parameters to determine audio quality.
//			mMediaRecorder.setAudioEncodingBitRate(12200);
//			mMediaRecorder.setAudioSamplingRate(8000);  // or 44100.

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        }

        mMediaRecorder.setOutputFile(mFileRecordedVideo.getPath());
        mMediaRecorder.setPreviewDisplay(mSurfHolder.getSurface());

        // Video will be upside-down when using front camera to record video with
        // portrait phone orientation, so do 180 degrees rotation.
        if (!mbUsingBackCamera && mPhoneOrientation == PHONE_ORIENTATION.PORTRAIT)
            mMediaRecorder.setOrientationHint(miCameraRotationDegree + 180);
        else {
            switch (mPhoneOrientation) {
                case PORTRAIT:
                    mMediaRecorder.setOrientationHint(miCameraRotationDegree);
                    break;
                case LANDSCAPE:
                    mMediaRecorder.setOrientationHint(miCameraRotationDegree - 90);
                    break;
            }
        }

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mbIsRecordingVideo = true;
            mImgViewRecordingVideo.setImageResource(R.drawable.anim_recording_video);
        } catch (Exception e) {
            showDlgErrorCamera();
            return;
        }

        // Count duration of recording video.
        mDurationRecordingInSecond = 0;
        mTxtDurationRecordingVideo.setText("00:00");
        mTxtDurationRecordingVideoRotate90.setText("00:00");

        // Show one of them depends on phone's orientation.
        if (mPhoneOrientation == PHONE_ORIENTATION.PORTRAIT) {
            mTxtDurationRecordingVideo.setVisibility(View.VISIBLE);
            mTxtDurationRecordingVideoRotate90.setVisibility(View.INVISIBLE);
        } else {
            mTxtDurationRecordingVideo.setVisibility(View.INVISIBLE);
            mTxtDurationRecordingVideoRotate90.setVisibility(View.VISIBLE);
        }

        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DURATION_RECORDING_VIDEO, 1000);
    }

    private void stopRecordingVideo() {
        mMediaRecorder.stop();
        mbIsRecordingVideo = false;
        mImgViewRecordingVideo.setImageResource(R.drawable.df_camera_recording_blink_grey);

        try {
            mCamera.reconnect();
        } catch (IOException e) {
            showDlgErrorCamera();
        }

        addPictureToGallery(mFileRecordedVideo, FILE_TYPE_VIDEO);

        mCamera.startPreview();

        // Reset duration of recording video.
        mTxtDurationRecordingVideo.setVisibility(View.INVISIBLE);
        mTxtDurationRecordingVideoRotate90.setVisibility(View.INVISIBLE);

        mHandler.removeMessages(MSG_UPDATE_DURATION_RECORDING_VIDEO);
    }

    // Find the best video width and height based on the area and the difference of
    // aspect ratio with the surface.
    private Size getBestSize(SIZE_TYPE sizeType, int iSizeMax, int iSizeMin, Camera.Parameters camParas,
                             int iSurfaceWidth, int iSurfaceHeight) {
        // Calculate aspect ratio of app's preview window.
        // The aspect ratio is max(weight, height) / min(weight, height).
        double dSurfaceAspectRatio = (iSurfaceWidth > iSurfaceHeight ?
                (double) iSurfaceWidth / (double) iSurfaceHeight :
                (double) iSurfaceHeight / (double) iSurfaceWidth);
        double dAspectRatioDiffMin = Double.MAX_VALUE;
        Size sizeBest = null;
        int areaBestSize = 0;

        List<Size> listSize = null;
        switch (sizeType) {
            case PREVIEW:
                listSize = camParas.getSupportedPreviewSizes();
                break;
            case VIDEO:
                // The returned sizes are not all usable. Some may cause crash.
                listSize = camParas.getSupportedVideoSizes();
                break;
            default:  // Picture.
                listSize = camParas.getSupportedPictureSizes();
        }

        // getSupportedVideoSizes() may return null for some phones.
        if (listSize == null)
            return null;

        for (Size s : listSize) {
            int area = s.width * s.height;
            if (area > iSizeMax || area < iSizeMin)
                continue;

            double dAspectRatioDiff = (s.width > s.height ?
                    Math.abs(dSurfaceAspectRatio - (double) s.width / (double) s.height) :
                    Math.abs(dSurfaceAspectRatio - (double) s.height / (double) s.width));
            if (dAspectRatioDiffMin > dAspectRatioDiff ||
                    (dAspectRatioDiffMin == dAspectRatioDiff && area > areaBestSize)) {
                // Record this as the best size.
                sizeBest = s;
                areaBestSize = area;
                dAspectRatioDiffMin = dAspectRatioDiff;
            }
        }

        return sizeBest;
    }

    private void rotatePictureAndSave(byte[] pictureData, boolean bIsJpegFormat) throws Exception {
        // Save bitmap directly without rotation.
        File pictureFile = getOutputFile(MEDIA_FILE_TYPE.PICTURE);
        FileOutputStream fileOutStream = new FileOutputStream(pictureFile);

        if (bIsJpegFormat)  // Write pictureData directly.
            fileOutStream.write(pictureData);
        else {  // Compress pictureData into JPEG format.
            Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutStream);
        }

        fileOutStream.close();

        // Modify orientation in Exif tags.
        ExifInterface exif=new ExifInterface(pictureFile.getPath());

        if (miPictureRotationDegree == ROTATION_DEGREE_BACK_CAMERA_PORTRAIT)
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
        else if (miPictureRotationDegree == ROTATION_DEGREE_BACK_CAMERA_LANDSCAPE)
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
        else if (miPictureRotationDegree == ROTATION_DEGREE_FRONT_CAMERA_PORTRAIT)
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
        else if (miPictureRotationDegree == ROTATION_DEGREE_FRONT_CAMERA_LANDSCAPE)
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));

        exif.saveAttributes();

        addPictureToGallery(pictureFile, FILE_TYPE_IMAGE);
    }

    private void setFocusMode() {
        // Determine focus mode depends on video or picture mode.
        Camera.Parameters camParas = mCamera.getParameters();
        List<String> listCameraMode = camParas.getSupportedFocusModes();
        if (listCameraMode == null) {
            return;
        }

        String focusMode = null;
        switch (mCameraType) {
            case VIDEO:
                if (listCameraMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
                } else if (listCameraMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
                } else if (listCameraMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    focusMode = Camera.Parameters.FOCUS_MODE_AUTO;
                }

                break;
            default:
                if (listCameraMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
                } else if (listCameraMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    focusMode = Camera.Parameters.FOCUS_MODE_AUTO;
                } else if (listCameraMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
                }
        }

        if (focusMode != null) {
            camParas.setFocusMode(focusMode);
            mCamera.setParameters(camParas);
        }
    }

}
