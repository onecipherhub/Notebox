package in.cipherhub.notebox.models;

public class ItemPDFList {

    private String name, by, author, date;
    private int totalShares, totalDownloads;
    private double rating;

    public ItemPDFList(String name, String by, String author
            , String date, int totalShares, int totalDownloads, double rating) {
        this.name = name;
        this.by = by;
        this.author = author;
        this.date = date;
        this.totalShares = totalShares;
        this.totalDownloads = totalDownloads;
        this.rating = rating;
    }


    public String getName() {
        return name;
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


    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
