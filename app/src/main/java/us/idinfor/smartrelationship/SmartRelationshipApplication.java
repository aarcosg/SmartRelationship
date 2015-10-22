package us.idinfor.smartrelationship;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class SmartRelationshipApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.getSharedPreferences(this).edit().putBoolean(Constants.PROPERTY_RECORD_AUDIO_ENABLED, true).commit();
        Fabric.with(this, new Crashlytics());
    }
}
