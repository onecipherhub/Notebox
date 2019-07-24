package in.cipherhub.notebox.models;

public class ItemDataHomeSubjects {

    public String subAbb, subName, lastUpdate;
//    public Boolean subBookmark;

    public ItemDataHomeSubjects(String subAbb, String subName, String lastUpdate
//            , Boolean subBookmark
    ) {
        this.subAbb = subAbb;
        this.subName = subName;
        this.lastUpdate = lastUpdate;
//        this.subBookmark = subBookmark;
    }
}
