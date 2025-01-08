package application;

public class Hesap {
    private String sube;
    private String hesapNo;
    private String iban;
    private String bakiye;

    public Hesap(String sube, String hesapNo, String iban, String bakiye) {
        this.sube = sube;
        this.hesapNo = hesapNo;
        this.iban = iban;
        this.bakiye = bakiye;
    }

    public String getSube() {
        return sube;
    }

    public void setSube(String sube) {
        this.sube = sube;
    }

    public String getHesapNo() {
        return hesapNo;
    }

    public void setHesapNo(String hesapNo) {
        this.hesapNo = hesapNo;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBakiye() {
        return bakiye;
    }

    public void setBakiye(String bakiye) {
        this.bakiye = bakiye;
    }
}
