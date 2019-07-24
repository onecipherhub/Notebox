package in.cipherhub.notebox.models;

public class ItemDataBranchSelector {

    private String branchName, totalUploads, branchAbbColor, branchAbb;

    public ItemDataBranchSelector(String branchName, String branchAbbColor, String totalUploads, String branchAbb) {
        this.branchName = branchName;
        this.branchAbbColor = branchAbbColor;
        this.totalUploads = totalUploads;
        this.branchAbb = branchAbb;
    }

    public String getBranchAbb() {
        return branchAbb;
    }

    public void setBranchAbb(String branchAbb) {
        this.branchAbb = branchAbb;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getTotalUploads() {
        return totalUploads;
    }

    public void setTotalUploads(String totalUploads) {
        this.totalUploads = totalUploads;
    }

    public String getBranchAbbColor() {
        return branchAbbColor;
    }

    public void setBranchAbbColor(String branchAbbColor) {
        this.branchAbbColor = branchAbbColor;
    }
}
