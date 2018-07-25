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
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_llamada_video.*
import android.widget.TextView
import android.widget.ProgressBar
import com.avaya.clientplatform.impl.*
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class LlamadaOneWay : AppCompatActivity(), HostnameVerifier, X509TrustManager, UserListener2, SessionListener2 {
    var mPlatform: ClientPlatform? = null
    var mUser: UserImpl? = null
    var mDevice: DeviceImpl? = null
    var mSession: SessionImpl? = null

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

        super.onCreate(null)
        setContentView(R.layout.activity_llamada_video)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        escondercontroles()
        volumeControlStream = AudioManager.STREAM_VOICE_CALL
        //Boton Colgar
        end_call.setOnClickListener {
            colgar()
            eliminartoken()
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



    fun obtenertoken() {
        var myPreferences = "myPrefs"
        var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        var displayname = sharedPreferences.getString("displayname", "John Doe")
        var username = sharedPreferences.getString("username", "1234")
        var host = sharedPreferences.getString("host", "amv.collaboratory.avaya.com")
        var puerto = sharedPreferences.getString("puerto", "443")

        //Definimos las variables de URL que tendra nuestra peticion con sus respectivas llaves
        //Key
        var paramKey1 = "displayName"
        //Parametro-Variable
        var paramValue1 = displayname
        //Key
        var paramKey2 = "userName"
        //Parametro Variable
        var paramValue2 = username
        //Invocamos FUEL Manager y lo asignamos a una variable para tener un mejor acceso a el
        val manager: FuelManager by lazy { FuelManager() }
        //Usamos el metodo request de FUUEL Manager, junto a la lusta de parametros
        manager.request(Method.GET, "https://$host:$puerto/avayatest/auth?", listOf(paramKey1 to paramValue1, paramKey2 to paramValue2)).responseString { req, res, result ->
            val (data, error) = result
            //Si no tenemos ningun error, procedemos a hacer la llamada, ya que el servidor respondio con un 200 y tendremos el Token de LLamada
            when (error) {
                null -> {
                    //Imprimimos el Response en el LogCat solo para asegurar que se hizo bien la peticion
                    Log.d("RESPONSES", data)
                    // creamos una variable llamada gson para la Funcion GSON() para que sea mas accesible
                    var gson = Gson()
                    //Asignamos a la variable Login el metodo gson?.fromJson(data, Login.Response::class.java) y le pasamos el response JSON para su conversion a un objeto que Android puede manejar
                    var Login = gson?.fromJson(data, Login.Response::class.java)
                    val myPreferences = "myPrefs"
                    val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("sessionid", Login.sessionid)
                    editor.putString("token", data)
                    editor.apply()
                    //llamamos
                    llamar(data.toString())
                }
            }
        }
    }
    fun llamar(token: String) {
        try {
            //Asignamos las superficies de video
            val rlRemote = findViewById<View>(R.id.remoteLayout) as RelativeLayout
            val rlLocal = findViewById<View>(R.id.localLayout) as RelativeLayout

            //asignamos a una varibale el ClientPlatform Factory
            mPlatform = ClientPlatformFactory.getClientPlatformInterface(this.applicationContext)
            //Obtenemos el Objeto Usuario
            mUser = mPlatform!!.user as UserImpl
            Log.d("SDK", token)
            //ASignamos el token al usuario
            val tokenAccepted = mUser!!.setSessionAuthorizationToken(token)
            if (tokenAccepted) {
                //Obtenemos el numero de las Shared Preferences
                var myPreferences = "myPrefs"
                var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
                var numero = sharedPreferences.getString("numero", "2681322102")
                //si es aceptado. registramos listeners
                mUser!!.registerListener(this)
                //aceptamos cualquier certificado
                mUser!!.acceptAnyCertificate(true)
                //asignamos la implementacio de Device a un objeto
                mDevice = mPlatform!!.device as DeviceImpl
                //Asignamos a las variable el tamaño de los paneles de video
                val remoteSize = Point(rlRemote.width, rlRemote.height)
                val localSize = Point(rlLocal.width, rlLocal.height)
                //Agregamos el video a las superficiones
                Log.d("SDK", mPlatform!!.device.toString())
                mSession = null
                when (mSession) {
                    null -> when {
                        mUser!!.isServiceAvailable -> {

                            val intent = intent
                            val gpslat = intent.getStringExtra("gpslat")
                            val gpslong = intent.getStringExtra("gpslong")

                            var mRemoteVideoSurface = VideoSurfaceImpl(this, remoteSize, null)
                            var mPreviewView = VideoSurfaceImpl(this, localSize, null)
                            mRemoteVideoSurface.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                            rlRemote.addView(mRemoteVideoSurface)
                            mPreviewView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                            rlLocal.addView(mPreviewView)
                            mDevice!!.localVideoView = mPreviewView
                            mDevice!!.remoteVideoView = mRemoteVideoSurface
                            Log.d("SDK", "Servicio Disponible, llamar")
                            mSession = mUser!!.createSession() as SessionImpl
                            //Registro de Listeners
                            mSession!!.registerListener(this)
                            //Si queremos Dual Video
                            mSession!!.enableAudio(true)
                            mSession!!.enableVideo(true)
                            mSession!!.muteAudio(false)
                            mSession!!.muteVideo(true)
                            Log.d("SDK", numero)
                            mSession!!.remoteAddress = numero
                            mostrarcontroles()
                            mSession!!.contextId = "$gpslat,$gpslong"
                            mSession!!.start()


                            //Funcion de Switch Video
                            btnSwitchVideo.setOnClickListener {
                                try {
                                    var camaras = mDevice!!.selectedCamera
                                    when (camaras.toString()){
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
                                    when (estadoaudio){
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
                                    when (estadovideo){
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
                                    when (videomute){
                                        true -> mSession!!.muteVideo(false)
                                        false -> mSession!!.muteVideo(true)
                                    }
                                } catch (e: Exception) {
                                    Log.d("SDK", "Fallo al mute video")
                                }
                            }
                            //Fin Drop


                        }
                        else -> {
                            toast("Servicio No Disponible. Reintente en unos segundos")
                            Log.d("SDK", "Servicio No Disponible")
                            colgar()
                        }

                    }
                    else -> Log.d("SDK", "Error")
                }
            } else {
                Log.d("SDK", "Token Invalido")
            }
        } catch (e: Exception) {
            Log.d("SDK", "Error" + e.message, e)
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

        try {
            if (mSession != null) {
                mDevice!!.localVideoView = null
                mDevice!!.remoteVideoView = null
                mSession!!.unregisterListener(this)
                mUser!!.unregisterListener(this)

                mSession!!.end()
                mSession = null

            }
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

    override fun onResume() {
        super.onResume()
        obtenertoken()


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
        toast("Estableciendo llamada")
    }

    override fun onConnLost(user: User) {

        toast("Se ha perdido conexion con el servidor, intente remarcar")
        colgar()
        finish()
    }

    override fun onServiceUnavailable(user: User) {


        toast("Servicio No disponible")
        colgar()
        finish()
    }

    override fun onNetworkError(user: User) {
        toast("Eror en la red")
        colgar()
        finish()
    }

    override fun onCriticalError(p0: User?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}