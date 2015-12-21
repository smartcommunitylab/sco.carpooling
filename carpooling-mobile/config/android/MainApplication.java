package it.smartcommunitylab.carpooling;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParsePush;
import com.parse.ParseInstallation;
import com.phonegap.parsepushplugin.ParsePushPlugin;

public class MainApplication extends Application {

    public static final String LOGTAG = "ParsePushPlugin";

    @Override
    public void onCreate() {
        super.onCreate();
        String appId = "";
        String clientKey = "";

        Parse.initialize(this, appId, clientKey);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
