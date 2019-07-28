package in.cipherhub.notebox.models;

public class ItemPDFList {

    private String name, by, author, date, url;
    private int totalShares, totalDownloads, likes, dislikes;

    public ItemPDFList(String name, String by, String author
            , String date, int totalShares, int totalDownloads, int likes, int dislikes) {
        this.name = name;
        this.by = by;
        this.author = author;
        this.date = date;
        this.totalShares = totalShares;
        this.totalDownloads = totalDownloads;
        this.likes = likes;
        this.dislikes = dislikes;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public int getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(int totalShares) {
        this.totalShares = totalShares;
    }


    public int getTotalDownloads() {
        return totalDownloads;
    }

    public void setTotalDownloads(int totalDownloads) {
        this.totalDownloads = totalDownloads;
    }
}
