package com.example.alam.hdi

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import com.avaya.clientplatform.api.*
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import kotlinx.android.synthetic.main.activity_llamada_video.*
import android.widget.TextView
import android.widget.ProgressBar
import com.avaya.clientplatform.impl.*
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import com.avaya.clientplatform.impl.VideoSurfaceImpl
import com.avaya.clientplatform.api.ClientPlatform

class LlamadaOneWay : AppCompatActivity(), HostnameVerifier, X509TrustManager, UserListener2, SessionListener2 {
    //Asignamos las superficies de video

    var mPlatform: ClientPlatform? = null
    var mUser: UserImpl? = null
    var mDevice: DeviceImpl? = null
    var mSession: SessionImpl? = null
    var mRemoteVideoSurface: VideoSurface? = null
    var mPreviewView: VideoSurface? = null


    //Override Certificados
    override fun verify(hostname: String, session: SSLSession): Boolean {
        Log.d("Certs", "Null Host")
        return true
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        Log.d("Certs", "Certificados Aceptados 3")
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        Log.d("Certs", "Certificados Aceptados 2")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate>? {
        Log.d("Certs", "Certificados Aceptados")
        return null
    }



    var tag1 = "API"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llamada_video)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        escondercontroles()
        volumeControlStream = AudioManager.STREAM_VOICE_CALL
        //Boton Colgar
        end_call.setOnClickListener {
            colgar()
            finish()


        }
        //

    }
    private fun escondercontroles() {
        setButtonsVisibility(View.INVISIBLE)
    }
    private fun mostrarcontroles() {
        setButtonsVisibility(View.VISIBLE)
    }
    private fun setButtonsVisibility(visibility: Int) {
        setVisibility(findViewById(R.id.btnMuteAudio), visibility)
        setVisibility(findViewById(R.id.btnEnableVideo), visibility)
        setVisibility(findViewById(R.id.mute_video), visibility)
        setVisibility(findViewById(R.id.btnSwitchVideo), visibility)
        // setVisibility(findViewById(R.id.end_call), visibility)
        setVisibility(findViewById(R.id.call_quality_bar), visibility)
        setVisibility(findViewById(R.id.textView7), visibility)
        setVisibility(findViewById(R.id.call_quality), visibility)
    }
    private fun setVisibility(button: View, visibility: Int) {
        when (visibility) {
            View.VISIBLE, View.INVISIBLE -> button.visibility = visibility
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        crearvideo()
    }

    fun crearvideo() {
        try {
            if (mRemoteVideoSurface == null) {

                mostrarcontroles()

                var clientPlatform = ClientPlatformManager.getClientPlatform(this)

                var rlRemote = findViewById<View>(R.id.remoteLayout) as RelativeLayout
                var rlLocal = findViewById<View>(R.id.localLayout) as RelativeLayout

                mDevice = clientPlatform!!.device as DeviceImpl?
                val remoteSize = Point(rlRemote.width, rlRemote.height)
                val localSize = Point(rlLocal.width, rlLocal.height)

                mRemoteVideoSurface = VideoSurfaceImpl(this, remoteSize, null)
                mPreviewView = VideoSurfaceImpl(this, localSize, null)

                (mRemoteVideoSurface as VideoSurfaceImpl).layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                rlRemote.addView(mRemoteVideoSurface)

                (mPreviewView as VideoSurfaceImpl).layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                rlLocal.addView(mPreviewView)

                mDevice!!.localVideoView = mPreviewView
                mDevice!!.remoteVideoView = mRemoteVideoSurface
            }
        } catch (e: Exception) {
            Log.d("SDK", "Error al crear video")
        }

    }


    private fun call() {
        try {
            var myPreferences = "myPrefs"
            var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
            var numero = sharedPreferences.getString("numero", "2681322102")
            val intent = intent
            val gpslat = intent.getStringExtra("gpslat")
            val gpslong = intent.getStringExtra("gpslong")
            mDevice = mPlatform!!.device as DeviceImpl?

            var clientPlatform = ClientPlatformManager.getClientPlatform(this.applicationContext)

            val browser = clientPlatform!!.userAgentBrowser
            val version = clientPlatform!!.userAgentVersion

            var mSession = mUser!!.createSession() as SessionImpl
            mSession.registerListener(this)

            mSession.enableAudio(true)
            mSession.enableVideo(true)
            mSession.muteAudio(false)
            mSession.muteVideo(false)
            mSession.contextId = "$gpslat,$gpslong"

            mSession.remoteAddress = numero
            mSession.start()
            end_call.setOnClickListener {
                mDevice!!.localVideoView = null
                mDevice!!.remoteVideoView = null
                mSession!!.unregisterListener(this)
                mUser!!.unregisterListener(this)
                mSession!!.end()
                finish()


            }
            //Funcion de Switch Video
            btnSwitchVideo.setOnClickListener {
                try {
                    var camaras = mDevice!!.selectedCamera
                    when (camaras.toString()) {
                        "FRONT" -> mDevice!!.selectCamera(CameraType.BACK)
                        "BACK" -> mDevice!!.selectCamera(CameraType.FRONT)
                    }
                } catch (e: Exception) {
                    Log.d("SDK", "Fallo al cambiar de camara")
                }
            }
            // Fin boton
            //Boton Mute Audio
            btnMuteAudio.setOnClickListener {
                try {
                    var estadoaudio = mSession!!.isAudioMuted
                    when (estadoaudio) {
                        true -> mSession!!.muteAudio(false)
                        false -> mSession!!.muteAudio(true)
                    }
                } catch (e: Exception) {
                    Log.d("SDK", "Fallo al hacer mute de audio")
                }
            }
            //Fin Mute
            //Boton DropVideo
            btnEnableVideo.setOnClickListener {
                try {
                    var estadovideo = mSession!!.isVideoEnabled
                    when (estadovideo) {
                        true -> mSession!!.enableVideo(false)
                        false -> mSession!!.enableVideo(true)
                    }
                } catch (e: Exception) {
                    Log.d("SDK", "Fallo al hacer dropvideo")
                }
            }
            //Fin Drop
            //Boton DropVideo
            mute_video.setOnClickListener {
                try {
                    var videomute = mSession!!.isVideoMuted
                    when (videomute) {
                        true -> mSession!!.muteVideo(false)
                        false -> mSession!!.muteVideo(true)
                    }
                } catch (e: Exception) {
                    Log.d("SDK", "Fallo al mute video")
                }
            }
            //Fin Drop


        } catch (e: Exception) {
            toast("Error" + e)
            Log.d("SDK", "error" + e)
            finish()
        }

    }

    fun eliminartoken() {
        var myPreferences = "myPrefs"
        var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        var host = sharedPreferences.getString("host", "amv.collaboratory.avaya.com")
        var puerto = sharedPreferences.getString("puerto", "443")
        var sessionid = sharedPreferences.getString("sessionid", "")
        val editor = sharedPreferences.edit()
        editor.putString("token", "")
        editor.apply()
        val manager: FuelManager by lazy { FuelManager() }
        //Usamos el metodo request de FUUEL Manager, junto a la lusta de parametros
        manager.request(Method.DELETE, "https://$host:$puerto/avayatest/auth/id/$sessionid").responseString { req, res, result ->
            val (data, error) = result
            when (error) {
                null -> {
                    toast("Se ha finalizado la llamada")
                    finish()
                }
            }
        }
    }


    fun colgar() {
        Log.d("SDK", "Colgar")
        try {
            if (mSession != null) {
                mDevice!!.localVideoView = null
                mDevice!!.remoteVideoView = null
                mSession!!.unregisterListener(this)
                mUser!!.unregisterListener(this)
                mSession!!.end()

            }
            finish()
        } catch (e: Exception) {
            Log.d("SDK", "Error al colgar$e")
            toast("Error al Colgar$e")
        }


    }

    //Toast
    fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        runOnUiThread { Toast.makeText(this, message, duration).show() }
    }


    //Clases
    class Login {
        data class Response(
                val sessionid: String,
                val uuid: String,
                val defaultDomain: String
        )
    }


    override fun onRestart() {
        Log.d("API", "Recreando onResume")


        super.onRestart()

    }

    //Listeners

    override fun onSessionRemoteAlerting(session: Session, hasEarlyMedia: Boolean) {
        Log.d("SDK", "Timbrando")
    }
    override fun onSessionRemoteAddressChanged(p0: Session?, p1: String?, p2: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionEnded(session: Session) {
        toast("Sesion finalizada")
        colgar()
        finish()

    }

    override fun onSessionVideoMuteFailed(p0: Session?, p1: Boolean, p2: SessionException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionFailed(p0: Session?, p1: SessionError?) {

        colgar()
        finish()

    }

    override fun onSessionQueued(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDialError(p0: Session?, p1: SessionError?, p2: String?, p3: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onGetMediaError(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionRedirected(session: Session) {
        Log.d("SDK", "Sesion Redirigida")
    }

    override fun onSessionServiceAvailable(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionServiceUnavailable(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQualityChanged(session: Session, i: Int) {
        Log.d("SDK", "La Calidad ha cambiado$i")
        try {
            runOnUiThread {
                val progress = findViewById<View>(R.id.call_quality_bar) as ProgressBar
                if (i in 0..100) {
                    progress.progress = i
                    val mTextView = findViewById<View>(R.id.textView7) as TextView
                    mTextView.text = i.toString() + "%"
                }
            }

        } catch (e: Exception) {
            Log.d("SDK", "Error Calidad$e")
        }


    }

    override fun onSessionEstablished(session: Session) {
        Log.d("SDK", "Sesion Establecida")
    }

    override fun onSessionRemoteDisplayNameChanged(p0: Session?, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionAudioMuteFailed(p0: Session?, p1: Boolean, p2: SessionException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionVideoMuteStatusChanged(session: Session, muted: Boolean) {
        Log.d("SDK", "Video Mute Off")
    }

    override fun onSessionVideoRemovedRemotely(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCallError(p0: Session?, p1: SessionError?, p2: String?, p3: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCapacityReached(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionAudioMuteStatusChanged(session:Session, muted: Boolean) {
        Log.d("SDK", "Audio Mute")

    }

    override fun onConnReestablished(user: User) {
        toast("Reconectado")
    }

    override fun onServiceAvailable(user: User) {
        Log.d("SDK", "Servicio Disponible")

    }

    override fun onConnRetry(user: User) {
        toast("Reintentando conectar")

    }

    override fun onConnectionInProgress(arg0: User) {
        Log.d("SDK", "Conexion en Progreso")
    }

    override fun onConnLost(user: User) {

        toast("Se ha perdido conexion con el servidor, intente remarcar")
        colgar()
    }

    override fun onServiceUnavailable(user: User) {
        toast("Servicio No disponible")
        colgar()
        finish()
    }

    override fun onNetworkError(user: User) {
        toast("Eror en la red")
        colgar()
    }

    override fun onCriticalError(p0: User?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlatform = null
        mUser = null
        if (mDevice != null) {
            mDevice!!.localVideoView = null
            mDevice = null
        }

        mRemoteVideoSurface = null
        mPreviewView = null
    }

    override fun onResume() {
        super.onResume()
        var myPreferences = "myPrefs"
        var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        var token = sharedPreferences.getString("token", "")

        try {

            mPlatform = ClientPlatformManager.getClientPlatform(this.applicationContext)
            mUser = mPlatform!!.user as UserImpl?

            Log.d("SDK", token)

            val tokenAccepted = mUser!!.setSessionAuthorizationToken(token)
            mSession = null
            if (tokenAccepted) {
                mUser!!.registerListener(this)
                mUser!!.acceptAnyCertificate(true)
                mPlatform!!.getDevice() as DeviceImpl
                Log.d("SDK", mPlatform!!.getDevice().toString())
                if (mSession == null) {
                    if (mUser!!.isServiceAvailable()) {
                        Log.d("SDK", "Llamar")

                        call()

                    } else {
                        Log.d("SDK", "Servicio No disponible")
                        colgar()
                    }
                } else {
                    Log.d("SDK", "no se puede llamar")


                }
            } else {
                Log.d("SDK", "Token Invalida")

            }
        } catch (e: Exception) {
            Log.d("SDK", "Error al resumir $e")
        }

    }

}


