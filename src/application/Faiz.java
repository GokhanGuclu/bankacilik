package application;

public class Faiz {
    private String hesapNo;
    private double tutar;
    private double faizOrani;
    private int sure;
    private double toplamTutar;

    // Constructor
    public Faiz(String hesapNo, double tutar, double faizOrani, int sure, double toplamTutar) {
        this.hesapNo = hesapNo;
        this.tutar = tutar;
        this.faizOrani = faizOrani;
        this.sure = sure;
        this.toplamTutar = toplamTutar;
    }

    // Getter ve Setter
    public String getHesapNo() {
        return hesapNo;
    }

    public void setHesapNo(String hesapNo) {
        this.hesapNo = hesapNo;
    }

    public double getTutar() {
        return tutar;
    }

    public void setTutar(double tutar) {
        this.tutar = tutar;
    }

    public double getFaizOrani() {
        return faizOrani;
    }

    public void setFaizOrani(double faizOrani) {
        this.faizOrani = faizOrani;
    }

    public int getSure() {
        return sure;
    }

    public void setSure(int sure) {
        this.sure = sure;
    }

    public double getToplamTutar() {
        return toplamTutar;
    }

    public void setToplamTutar(double toplamTutar) {
        this.toplamTutar = toplamTutar;
    }
}
