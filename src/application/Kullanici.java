package application;

public class Kullanici {
    private int id;
    private String ad;
    private String soyad;
    private String mail;
    private String telefon;

    private static Kullanici currentUser; 
    
    public Kullanici(int id, String ad, String soyad, String mail, String telefon) {
        this.id = id;
        this.ad = ad;
        this.soyad = soyad;
        this.mail = mail;
        this.telefon = telefon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getSoyad() {
        return soyad;
    }

    public void setSoyad(String soyad) {
        this.soyad = soyad;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public static Kullanici getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Kullanici currentUser) {
        Kullanici.currentUser = currentUser;
    }
}
