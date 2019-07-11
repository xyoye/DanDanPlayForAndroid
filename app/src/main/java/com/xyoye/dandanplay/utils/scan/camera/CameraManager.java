/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xyoye.dandanplay.utils.scan.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.blankj.utilcode.util.ToastUtils;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.xyoye.dandanplay.utils.scan.camera.open.OpenCamera;
import com.xyoye.dandanplay.utils.scan.camera.open.OpenCameraInterface;
import com.xyoye.dandanplay.utils.scan.view.SimpleLog;

import java.io.IOException;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

  private static final String TAG = CameraManager.class.getSimpleName();

  private final Context context;
  private final CameraConfigurationManager configManager;
  private OpenCamera openCamera;
  private AutoFocusManager autoFocusManager;
  private boolean initialized;
  private boolean previewing;
  private Camera.PreviewCallback previewCallback;
  private int displayOrientation = 0;
  private int surfaceWidth, surfaceHeight;

  private RectF framingRect;

  // PreviewCallback references are also removed from original ZXING authors work,
  // since we're using our own interface.
  // FramingRects references are also removed from original ZXING authors work,
  // since We're using all view size while detecting QR-Codes.
  private int requestedCameraId = OpenCameraInterface.NO_REQUESTED_CAMERA;
  private long autofocusIntervalInMs = AutoFocusManager.DEFAULT_AUTO_FOCUS_INTERVAL_MS;

  public CameraManager(Context context) {
    this.context = context;
    this.configManager = new CameraConfigurationManager(context);
  }

  public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
    this.previewCallback = previewCallback;

    if (isOpen()) {
      openCamera.getCamera().setPreviewCallback(previewCallback);
    }
  }

  public void setDisplayOrientation(int degrees) {
    this.displayOrientation = degrees;

    if (isOpen()) {
      openCamera.getCamera().setDisplayOrientation(degrees);
    }
  }

  public void setAutofocusInterval(long autofocusIntervalInMs) {
    this.autofocusIntervalInMs = autofocusIntervalInMs;
    if (autoFocusManager != null) {
      autoFocusManager.setAutofocusInterval(autofocusIntervalInMs);
    }
  }

  public void forceAutoFocus() {
    if (autoFocusManager != null) {
      autoFocusManager.start();
    }
  }

  public Point getPreviewSize() {
    return configManager.getCameraResolution();
  }

  /**
   * Opens the camera driver and initializes the hardware parameters.
   *
   * @param holder The surface object which the camera will draw preview frames into.
   * @param height @throws IOException Indicates the camera driver failed to open.
   */
  public synchronized void openDriver(SurfaceHolder holder, int width, int height)
      throws IOException {
    surfaceWidth = width;
    surfaceHeight = height;
    OpenCamera theCamera = openCamera;
    if (!isOpen()) {
      theCamera = OpenCameraInterface.open(requestedCameraId);
      if (theCamera == null || theCamera.getCamera() == null) {
        throw new IOException("Camera.open() failed to return object from driver");
      }
      openCamera = theCamera;
    }
    theCamera.getCamera().setPreviewDisplay(holder);
    theCamera.getCamera().setPreviewCallback(previewCallback);
    theCamera.getCamera().setDisplayOrientation(displayOrientation);

    if (!initialized) {
      initialized = true;
      configManager.initFromCameraParameters(theCamera, width, height);
    }

    Camera cameraObject = theCamera.getCamera();
    Camera.Parameters parameters = cameraObject.getParameters();
    String parametersFlattened =
        parameters == null ? null : parameters.flatten(); // Save these, temporarily
    try {
      configManager.setDesiredCameraParameters(theCamera, false);
    } catch (RuntimeException re) {
      // Driver failed
      SimpleLog.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
      SimpleLog.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
      // Reset:
      if (parametersFlattened != null) {
        parameters = cameraObject.getParameters();
        parameters.unflatten(parametersFlattened);
        try {
          cameraObject.setParameters(parameters);
          configManager.setDesiredCameraParameters(theCamera, true);
        } catch (RuntimeException re2) {
          // Well, darn. Give up
          SimpleLog.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
        }
      }
    }
    cameraObject.setPreviewDisplay(holder);
  }

  /**
   * Allows third party apps to specify the camera ID, rather than determine
   * it automatically based on available cameras and their orientation.
   *
   * @param cameraId camera ID of the camera to use. A negative value means "no preference".
   */
  public synchronized void setPreviewCameraId(int cameraId) {
    requestedCameraId = cameraId;
  }

  public int getPreviewCameraId() {
    return requestedCameraId;
  }

  /**
   * @param enabled if {@code true}, light should be turned on if currently off. And vice versa.
   */
  public synchronized void setTorchEnabled(boolean enabled) {
    OpenCamera theCamera = openCamera;
    if (theCamera != null && enabled != configManager.getTorchState(theCamera.getCamera())) {
      boolean wasAutoFocusManager = autoFocusManager != null;
      if (wasAutoFocusManager) {
        autoFocusManager.stop();
        autoFocusManager = null;
      }
      configManager.setTorchEnabled(theCamera.getCamera(), enabled);
      if (wasAutoFocusManager) {
        autoFocusManager = new AutoFocusManager(theCamera.getCamera());
        autoFocusManager.start();
      }
    }
  }

  public synchronized boolean isOpen() {
    return openCamera != null && openCamera.getCamera() != null;
  }

  /**
   * Closes the camera driver if still in use.
   */
  public synchronized void closeDriver() {
    if (isOpen()) {
      openCamera.getCamera().release();
      openCamera = null;
      // Make sure to clear these each time we close the camera, so that any scanning rect
      // requested by intent is forgotten.
      // framingRect = null;
      // framingRectInPreview = null;
    }
  }

  /**
   * Asks the camera hardware to begin drawing preview frames to the screen.
   */
  public synchronized void startPreview() {
    OpenCamera theCamera = openCamera;
    if (theCamera != null && !previewing) {
      theCamera.getCamera().startPreview();
      previewing = true;
      autoFocusManager = new AutoFocusManager(theCamera.getCamera());
      autoFocusManager.setAutofocusInterval(autofocusIntervalInMs);
    }
  }

  /**
   * Tells the camera to stop drawing preview frames.
   */
  public synchronized void stopPreview() {
    if (autoFocusManager != null) {
      autoFocusManager.stop();
      autoFocusManager = null;
    }
    if (openCamera != null && previewing) {
      openCamera.getCamera().stopPreview();
      previewing = false;
    }
  }

  /**
   * A factory method to build the appropriate LuminanceSource object based on the format
   * of the preview buffers, as described by Camera.Parameters.
   *
   * @param data A preview frame.
   * @param dataWidth The width of the image.
   * @param dataHeight The height of the image.
   * @return A PlanarYUVLuminanceSource instance.
   */
  public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int dataWidth, int dataHeight) {
    //not preview size use all data
    if (framingRect == null){
      ToastUtils.showShort("1：dataWidth："+dataWidth+"dataHeight："+dataHeight);
      return new PlanarYUVLuminanceSource(data, dataWidth, dataHeight, 0, 0, dataWidth, dataHeight, false);
    }
    //ratio not equals use all data
    int realHeight = Math.max(dataHeight, dataWidth);
    int realWidth = Math.min(dataHeight, dataWidth);
    if (realHeight*surfaceWidth != realWidth*surfaceHeight){
      return new PlanarYUVLuminanceSource(data, dataWidth, dataHeight, 0, 0, dataWidth, dataHeight, false);
    }

    //finder view size
    float width = framingRect.width();
    float height = framingRect.height();
    float x = framingRect.left;
    float y = framingRect.top;

    if (dataWidth > dataHeight){
      width = framingRect.width() * 1.3f;
      if (width+y > dataWidth)
        width = dataWidth-y;
      if (height+x > dataHeight){
        width = dataWidth;
        height = dataHeight;
        x = 0;
        y = 0;
      }

      return new PlanarYUVLuminanceSource(data, dataWidth, dataHeight, (int)y, (int)x, (int)width, (int)height, false);
    }else {
      height = framingRect.height() * 1.3f;
      if (height+y > dataHeight)
        height = dataHeight-y;

      if (width+x > dataWidth){
        width = dataWidth;
        height = dataHeight;
        x = 0;
        y = 0;
      }
      return new PlanarYUVLuminanceSource(data, dataWidth, dataHeight, (int)x, (int)y, (int)width, (int)height, false);
    }
  }

  public void setFramingRectF(RectF rect){
    framingRect = rect;
  }
}
