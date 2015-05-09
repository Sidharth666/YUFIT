package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.SurfaceView;

public class SurfaceViewCamera extends SurfaceView {

	private static double CAMERA_ZOOM_IN_BASE = 30,
						  CAMERA_ZOOM_OUT_BASE = 100;

	private Context mContext;
	private ScaleGestureDetector mScaleDetector;
	private Camera mCamera;

	public SurfaceViewCamera(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;

		mScaleDetector = new ScaleGestureDetector(context, new OnScaleGestureListener() {
			private Camera.Parameters mCamParas;
			private int miZoomOriginal, miZoomMax;

			@Override
			public void onScaleEnd(ScaleGestureDetector detector) {
			}
			
			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				mCamParas = mCamera.getParameters();
				miZoomOriginal = mCamParas.getZoom();
				miZoomMax = mCamParas.getMaxZoom();
				return true;
			}
			
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				if (mCamParas.isZoomSupported()) {
					float scaleFactor = detector.getScaleFactor();

					int zoomNew;
					if (scaleFactor > 1) {
						zoomNew = (int) (miZoomOriginal + CAMERA_ZOOM_IN_BASE * (scaleFactor - 1));
						if (zoomNew > miZoomMax)
							zoomNew = miZoomMax;
					} else {
						zoomNew = (int) (miZoomOriginal + CAMERA_ZOOM_OUT_BASE * (scaleFactor - 1));
						if (zoomNew < 0)
							zoomNew = 0;
					}

					mCamParas.setZoom(zoomNew);
					mCamera.setParameters(mCamParas);

//					Toast.makeText(mContext, "Camera zoom (max " + zoomMax + "): " + (zoomNew <= zoomMax ? zoomNew : zoomMax), Toast.LENGTH_SHORT)
//							.show();
				}

				return false;
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
		return true;
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
	}
}
