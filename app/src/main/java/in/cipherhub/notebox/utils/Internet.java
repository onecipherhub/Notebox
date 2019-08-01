package in.cipherhub.notebox.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class Internet {

    private Context context;

    public Internet(Context context) {
        this.context = context;
    }

    public boolean isAvailable() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        if (!haveConnectedWifi && !haveConnectedMobile)
            Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show();

        return haveConnectedWifi || haveConnectedMobile;
    }
}
