package net.osmand.turn_screen_on.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import net.osmand.aidl.IOsmAndAidlCallback;
import net.osmand.aidl.IOsmAndAidlInterface;
import net.osmand.aidl.gpx.AGpxBitmap;
import net.osmand.aidl.map.ALatLon;
import net.osmand.aidl.maplayer.point.AMapPoint;
import net.osmand.aidl.maplayer.point.ShowMapPointParams;
import net.osmand.aidl.navigation.ADirectionInfo;
import net.osmand.aidl.search.SearchResult;
import net.osmand.turn_screen_on.PluginSettings;
import net.osmand.turn_screen_on.app.TurnScreenOnApplication;

import java.util.List;

public class OsmAndAidlHelper {
    //todo change singleton type
    private final static OsmAndAidlHelper INSTANCE = new OsmAndAidlHelper();

    private final static String AIDL_SERVICE_PATH = "net.osmand.aidl.OsmandAidlService";

    private LockHelper lockHelper;
    private PluginSettings settings;

    private IOsmAndAidlInterface mIOsmAndAidlInterface;
    private IOsmAndAidlCallback mIOsmAndAidlCallbackInterface = new IOsmAndAidlCallback.Stub() {

        @Override
        public void onSearchComplete(List<SearchResult> resultSet) throws RemoteException {

        }

        @Override
        public void onUpdate() throws RemoteException {

        }

        @Override
        public void onAppInitialized() throws RemoteException {

        }

        @Override
        public void onGpxBitmapCreated(AGpxBitmap bitmap) throws RemoteException {

        }

        @Override
        public void updateNavigationInfo(ADirectionInfo directionInfo) throws RemoteException {

        }

        @Override
        public void onContextMenuButtonClicked(int buttonId, String pointId, String layerId) throws RemoteException {

        }

        @Override
        public void onVoiceRouterNotify() throws RemoteException {
            Log.d("ttpl", "take message from vr");
            if (settings.isPluginEnabled()) {
                lockHelper.timedUnlock(PluginSettings.getInstance().getTimeLikeSeconds() * 1000L);
            }
        }
    };

    private boolean isConnected = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIOsmAndAidlInterface = IOsmAndAidlInterface.Stub.asInterface(service);
            if (mIOsmAndAidlInterface != null) {
                isConnected = true;
                try {
                    Log.d("ttpl", "onServiceConnected: connected to " + service.getInterfaceDescriptor());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("ttpl", "no connection set up");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIOsmAndAidlInterface = null;
            isConnected = false;
            Log.d("ttpl", "onServiceDisconnected: disconnect");
        }
    };

    private OsmAndAidlHelper() {
        //todo change
        settings = PluginSettings.getInstance();
        lockHelper = new LockHelper(TurnScreenOnApplication.getAppContext());
    }

    public static OsmAndAidlHelper getInstance(){
        return INSTANCE;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void show() {
        ALatLon ll = new ALatLon(50.43859, 30.50534);
        AMapPoint mp = new AMapPoint("mapPointId", "mapPoint", "Destination location", "point", null,
                1, ll, null, null);
        ShowMapPointParams m = new ShowMapPointParams("ShowMapPointParams", mp);
        try {
            if (mIOsmAndAidlInterface != null) {
                Log.d("ttpl", "show point");
                mIOsmAndAidlInterface.showMapPoint(m);
            } else {
                Log.d("ttpl", "mIOsmAndAidlInterface is null");
            }
        } catch (RemoteException e) {
            Log.d("ttpl", "some exception");
            e.printStackTrace();
        }
    }

    public void register() {
        try {
            if (mIOsmAndAidlInterface != null) {
                Log.d("ttpl", "register for voice router messages");
                mIOsmAndAidlInterface.registerForVoiceRouterMessages(mIOsmAndAidlCallbackInterface);
            } else {
                Log.d("ttpl", "not registered for messages");
            }
        } catch (RemoteException e) {
            Log.d("ttpl", "some exception");
            e.printStackTrace();
        }
    }

    private boolean bindService(String appToConnectPackage, Context context) {
        Intent intent = new Intent(AIDL_SERVICE_PATH);
        intent.setPackage(appToConnectPackage);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        return true;
    }

    public boolean connect(String appToConnectPackage){
        bindService(appToConnectPackage, TurnScreenOnApplication.getAppContext());
        Log.d("ttpl", "connecting...");
        return true;
    }
}
