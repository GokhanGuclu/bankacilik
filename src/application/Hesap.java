package application;

public class Hesap {
    private String sube;
    private String hesapNo;
    private String iban;
    private double bakiye;

    // Constructor
    public Hesap(String sube, String hesapNo, String iban, double bakiye) { 
        this.sube = sube;
        this.hesapNo = hesapNo;
        this.iban = iban;
        this.bakiye = bakiye; 
    }
    
   public Hesap(String hesapNo, double bakiye) {
	   this.hesapNo = hesapNo;
	   this.bakiye = bakiye;
   }

    // Getter ve Setter
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

    public double getBakiye() { 
        return bakiye;
    }

    public void setBakiye(double bakiye) { 
        this.bakiye = bakiye;
    }
}
