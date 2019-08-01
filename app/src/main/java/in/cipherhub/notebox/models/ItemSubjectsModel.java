package in.cipherhub.notebox.models;

class ItemSubjectsModel {

    private String fullSubjectName;
    private String shortSubjectName;

    public ItemSubjectsModel(String fullSubjectName, String shortSubjectName) {
        this.fullSubjectName = fullSubjectName;
        this.shortSubjectName = shortSubjectName;
    }

    public String getFullSubjectName() {
        return fullSubjectName;
    }

    public void setFullSubjectName(String fullSubjectName) {
        this.fullSubjectName = fullSubjectName;
    }

    public String getShortSubjectName() {
        return shortSubjectName;
    }

    public void setShortSubjectName(String shortSubjectName) {
        this.shortSubjectName = shortSubjectName;
    }
}
