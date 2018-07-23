public class Test {

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mLogger.d("onResume()");

            Bundle extras = getIntent().getExtras();
            mContextId = extras.getString(Constants.KEY_CONTEXT).trim();
            mEnableVideo = extras.getBoolean(Constants.KEY_ENABLE_VIDEO);
            mStartAudioMuted = extras.getBoolean(Constants.KEY_START_MUTED_AUDIO);
            mStartVideoMuted = extras.getBoolean(Constants.KEY_START_MUTED_VIDEO);

            mCamera = CameraType.FRONT;

            // First instantiate a client platform interface
            // The factory is the top level object that provides access to the
            // services exposed by the SDK.
            mPlatform = ClientPlatformManager.getClientPlatform(this.getApplicationContext());
            mUser = mPlatform.getUser();

            // Create user object that is used to encapsulate the call session.
            String token = extras.getString(Constants.DATA_SESSION_KEY);

            boolean tokenAccepted = mUser.setSessionAuthorizationToken(token);

            if (tokenAccepted) {
                mUser.registerListener(this);
                mUser.acceptAnyCertificate(true);

                // Create a device object that this application
                mPlatform.getDevice();
                if (mDevice != null) {
                    mLogger.d("IsMediaAccessible : " + mDevice.couldMediaBeAccessible(getApplicationContext()));
                }

                if (mSession == null) {
                    if (mUser.isServiceAvailable()) {
                        mLogger.d("service available, make call now");
                        call();
                    } else {
                        mLogger.e("service not available");
                        String message = addNetworkConnectionMessage(getResources().getString(R.string.service_unavailable));
                        hangup();
                        displayMessage(message, true);
                    }
                } else {
                    mLogger.d("session state: " + mSession.getState() + ", not placing call now");
                }
            } else {
                mLogger.w("Invalid token used");
                displayMessage(getResources().getString(R.string.invalid_token), true);
            }
        } catch (Exception e) {
            mLogger.e("Exception in onResume(): " + e.getMessage(), e);
            displayMessage("Call activity resume exception: " + e.getMessage());
        }
    }

}
