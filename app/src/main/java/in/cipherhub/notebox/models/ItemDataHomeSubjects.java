package in.cipherhub.notebox.models;

import com.google.android.gms.ads.AdView;

public class ItemDataHomeSubjects {

    public String subAbb, subName, lastUpdate;
    private AdView adView;

//    public Boolean subBookmark;

    public ItemDataHomeSubjects(AdView adView) {
        this.adView = adView;
    }

    public ItemDataHomeSubjects(String subAbb, String subName, String lastUpdate
//            , Boolean subBookmark
    ) {
        this.subAbb = subAbb;
        this.subName = subName;
        this.lastUpdate = lastUpdate;
//        this.subBookmark = subBookmark;
    }
}
