package com.example.alam.hdi

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.avaya.clientplatform.api.*
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_llamada_video.*
import android.widget.TextView
import android.widget.ProgressBar
import java.security.cert.X509Certificate
import javax.net.ssl.*
import java.lang.Compiler.disable
import com.avaya.vivaldi.internal.d
import com.avaya.clientplatform.api.ClientPlatformFactory
import com.avaya.clientplatform.api.AudioOnlyClientPlatform
import com.avaya.clientplatform.api.ClientPlatform
import com.avaya.clientplatform.impl.*
import com.avaya.vivaldi.internal.e
import com.avaya.vivaldi.internal.d
import com.avaya.clientplatform.impl.SessionImpl
import com.avaya.clientplatform.api.User






class LlamadaVideo : AppCompatActivity(),  HostnameVerifier, X509TrustManager, UserListener2, SessionListener2 {


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


//


    //
    var tag1 = "API"
    override fun onCreate(savedInstanceState: Bundle?) {
        volumeControlStream = AudioManager.STREAM_VOICE_CALL
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_llamada_video)

    }

    override fun onResume() {
        super.onResume()

        try {
            var myPreferences = "myPrefs"
            var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
            var token = sharedPreferences.getString("token", "")
            var  mPlatform = ClientPlatformFactory.getClientPlatformInterface(this.applicationContext)
            var mUser = mPlatform.user
            val tokenAccepted = mUser.setSessionAuthorizationToken(token)
            if (tokenAccepted) {
                mUser.registerListener(this)
                mUser.acceptAnyCertificate(true)
                // Create a device object that this application
                mPlatform.getDevice()
                var mSession: Nothing? = null
                if (mSession == null) {
                    if (mUser.isServiceAvailable()) {
                        Log.d("SDK", "Servicio Disponible")
                        llamar()
                    } else {
                        //
                       // colgar()

                    }
                } else {
                    Log.d("SDK", "Estado de la Sesion:")

                }
            } else {
                Log.d("SDK", "Token Invalido")
            }
        } catch (e: Exception) {
            Log.d("SDK", "Excepcion al llamar" + e.message)

        }

    }



    override fun onStart() {
        super.onStart()
        obtenertoken()
    }

    fun useInsecureSSL() {
        Log.d("Cert", "Aceptando")

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {

            override fun getAcceptedIssuers(): Array<X509Certificate>? = null
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        })

        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, java.security.SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)

        // Create all-trusting host name verifier
        val allHostsValid = HostnameVerifier { _, _ -> true }

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
    }


    private fun llamar() {
        Log.d("SDK","Iniciando LLamada")

        try {
            var mPlatform = ClientPlatformFactory.getClientPlatformInterface(this.applicationContext)
            var mUser = mPlatform.user as UserImpl
            var mDevice = mPlatform.device as DeviceImpl

            var mSession = mUser.createSession() as SessionImpl
            mSession.registerListener(this)

            mSession.enableAudio(true)
            mSession.enableVideo(true)
            mSession.muteAudio(false)
            mSession.muteVideo(false)

            if (mContextId != null && mContextId.length() > 0) {
                mLogger.d("Context ID:$mContextId")
                mSession.setContextId(mContextId)
            }

            val numberToDial = intent.extras!!.getString(Constants.KEY_NUMBER_TO_DIAL)

            mSession.setRemoteAddress(numberToDial)

            if (mEnableVideo) {
                // Initialize video transmission components
                // Select the camera device (front or back)

                mDevice.selectCamera(mCamera)
                mDevice.setCameraCaptureResolution(mPreferredVideoResolution)
            }


            // Lock into portrait mode// This may differ device to device
            // Set this in case the user escalates to a video call after initiating a voice call
            // The default orientation (0) refers to landscape right, so as this app is portrait only
            // change the orientation
            mDevice.setCameraCaptureOrientation(Orientation.TWO_SEVENTY)

            // Start the call session.
            // During the call establishment, the one-time session ID is obtained
            // from the server behind the scenes.
            // ICE candidate list is obtained and the correct local IP address is
            // determined.
            // The Session object implementation controls the media channels of the
            // call based on the signaling protocol feedback.
            mSession.start()

            setCalleeDisplayInformation(mSession.getState())
            mLogger.d("Browser: $browser, version: $version")
            mLogger.d("Orientation: " + mDevice.getCameraCaptureOrientation())
            mLogger.d("Camera capture resolution: " + mDevice.getCameraCaptureResolution())
            mLogger.d("Session authorisation token: " + mUser.getSessionAuthorizationToken())

        } catch (e: Exception) {
            mLogger.e("Exception occurred in AVCallActivityImpl.call(): " + e.message, e)
            displayMessage(resources.getString(R.string.call_failed) + e.message)
            finish()
        }

    }





    fun obtenertoken() {
      //  useInsecureSSL()
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
        val manager: FuelManager by lazy { FuelManager()  }
        //Usamos el metodo request de FUUEL Manager, junto a la lusta de parametros
        manager.request(Method.GET, "https://$host:$puerto/avayatest/auth?", listOf(paramKey1 to paramValue1, paramKey2 to paramValue2)).responseString { req, res, result ->
            val (data, error) = result
            //Si no tenemos ningun error, procedemos a hacer la llamada, ya que el servidor respondio con un 200 y tendremos el Token de LLamada
            when (error) {
                null -> {
                   // useInsecureSSL()
                    //Imprimimos el Response en el LogCat solo para asegurar que se hizo bien la peticion
                    Log.d("RESPONSES", data)
                    // creamos una variable llamada gson para la Funcion GSON() para que sea mas accesible
                    var gson = Gson()
                    //Asignamos a la variable Login el metodo gson?.fromJson(data, Login.Response::class.java) y le pasamos el response JSON para su conversion a un objeto que Android puede manejar
                    var Login = gson.fromJson(data, Login.Response::class.java)
                    val myPreferences = "myPrefs"
                    val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("sessionid", Login.sessionid)
                    editor.putString("token", data)
                    editor.apply()
                    //llamamos
                }
            }
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


    //Listeners

    override fun onSessionRemoteAlerting(session: Session, hasEarlyMedia: Boolean) {
        Log.d("SDK", "Timbrando")
    }

    override fun onSessionRemoteAddressChanged(p0: Session?, p1: String?, p2: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionEnded(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionVideoMuteFailed(p0: Session?, p1: Boolean, p2: SessionException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionFailed(p0: Session?, p1: SessionError?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        toast("Reconectado")    }

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
        toast("Se ha perdido la conexion con el servidor, intente mas tarde")

    }

    override fun onServiceUnavailable(p0: User?) {

    }

    override fun onNetworkError(p0: User?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCriticalError(p0: User?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }




}


