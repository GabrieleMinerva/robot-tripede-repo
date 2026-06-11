package com.robottripede.app.data.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import androidx.core.content.ContextCompat

class CameraPreviewView(
    context: Context,
    private val onCameraError: (String) -> Unit = {},
) : TextureView(context) {
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var cameraThread: HandlerThread? = null
    private var cameraHandler: Handler? = null

    init {
        surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                startPreview()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                stopPreview()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
        }
    }

    fun startPreview() {
        if (cameraDevice != null) return
        if (!isAvailable || ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = selectCameraId(manager) ?: run {
                onCameraError("Nessuna camera disponibile")
                return
            }
            ensureCameraThread()
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createSession(camera)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    onCameraError("Errore camera: $error")
                }
            }, cameraHandler)
        } catch (exception: Exception) {
            stopPreview()
            onCameraError(exception.message ?: "Preview camera non disponibile")
        }
    }

    fun stopPreview() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        cameraThread?.quitSafely()
        cameraThread = null
        cameraHandler = null
    }

    private fun createSession(camera: CameraDevice) {
        try {
            val texture = surfaceTexture ?: return
            texture.setDefaultBufferSize(width.coerceAtLeast(640), height.coerceAtLeast(480))
            val surface = Surface(texture)
            val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
            }
            camera.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        session.setRepeatingRequest(request.build(), null, cameraHandler)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        onCameraError("Configurazione preview non riuscita")
                    }
                },
                cameraHandler,
            )
        } catch (exception: Exception) {
            stopPreview()
            onCameraError(exception.message ?: "Sessione camera non disponibile")
        }
    }

    private fun ensureCameraThread() {
        if (cameraThread != null) return
        cameraThread = HandlerThread("RobotTripedeCamera").also { thread ->
            thread.start()
            cameraHandler = Handler(thread.looper)
        }
    }

    private fun selectCameraId(manager: CameraManager): String? {
        val backCamera = manager.cameraIdList.firstOrNull { cameraId ->
            manager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
        return backCamera ?: manager.cameraIdList.firstOrNull()
    }
}
