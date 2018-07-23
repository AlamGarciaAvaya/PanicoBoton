package com.example.alam.hdi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.avaya.clientplatform.api.SessionListener2
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.security.cert.X509Certificate
import javax.net.ssl.*

open class MainActivity : AppCompatActivity(), HostnameVerifier, X509TrustManager, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener  {
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

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null

    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()
    private val FASTEST_INTERVAL: Long = 2000




    //Listeners GPS
    override fun onConnected(p0: Bundle?) {
        when {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED -> return
            else -> {
                actualizargps()

                mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

                when (mLocation) {
                    null -> actualizargps()
                }
                when {
                    mLocation != null -> Log.d("GPS", "Iniciando Actualizaciones")
                    else -> Toast.makeText(this, "No s epuede obtener ubicacion", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }




    override fun onConnectionSuspended(i: Int) {
        Log.d("GPS", "GPS Suspendido")
        mGoogleApiClient!!.connect()

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d("GPS", "Error al conectar GPS" + + connectionResult.errorCode)
    }

    override fun onLocationChanged(location: Location) {
        Log.d("GPS", "Ubicacion Cambiada")
        Log.d("GPS", mLocation?.longitude.toString())
        Log.d("GPS", mLocation?.latitude.toString())

    }
    //Fin Listeners




    @SuppressLint("MissingPermission")
    fun actualizargps() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this)

    }

    override fun onStart() {
        super.onStart()
        when {
            mGoogleApiClient != null -> mGoogleApiClient!!.connect()

        }
    }
    override fun onStop() {
        super.onStop()
        when {
            mGoogleApiClient!!.isConnected -> mGoogleApiClient!!.disconnect()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {/* ... */
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {/* ... */
                    }
                }).check()
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)


        //GPS
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    }



    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        i_actividadajustes()
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_poliza -> {

            }

            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.dualvideo_menu -> {
                i_llamadavideo()

            }
            R.id.oneway_menu -> {
                i_onewayvideo()

            }
            R.id.audio_only -> {
                i_llamadaaudio()

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun i_actividadajustes() {
        val intent = Intent(this, AjustesActivity::class.java)
        startActivity(intent)
    }

    fun i_llamadavideo() {
        actualizargps()
        val intent = Intent(this, LlamadaVideo::class.java)
        // Pasar Valores entre Actividades
        intent.putExtra("gpslat", mLocation?.latitude.toString())
        intent.putExtra("gpslong", mLocation?.longitude.toString())
        startActivity(intent)
    }

    fun i_onewayvideo() {
        actualizargps()
        val intent = Intent(this, LlamadaOneWay::class.java)
        // Pasar Valores entre Actividades
        intent.putExtra("gpslat", mLocation?.latitude.toString())
        intent.putExtra("gpslong", mLocation?.longitude.toString())
        startActivity(intent)
    }
    fun i_llamadaaudio() {
        actualizargps()
        val intent = Intent(this, LlamadaAudio::class.java)
        // Pasar Valores entre Actividades
        intent.putExtra("gpslat", mLocation?.latitude.toString())
        intent.putExtra("gpslong", mLocation?.longitude.toString())
        startActivity(intent)
    }
}
