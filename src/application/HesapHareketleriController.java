package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HesapHareketleriController {

    private VBox hareketContainer; // Dinamik VBox
    private String iban; // IBAN bilgisi

    public void start(Stage stage) {
        hareketContainer = new VBox(10);
        hareketContainer.setStyle("-fx-padding: 10;");

        ScrollPane scrollPane = new ScrollPane(hareketContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #d1d1d1;");

        VBox mainContainer = new VBox(10);
        mainContainer.setStyle("-fx-background-color: #909090; -fx-padding: 20; -fx-alignment: center;");

        Label header = new Label("HESAP HAREKETLERİ");
        header.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        mainContainer.getChildren().addAll(header, scrollPane);

        Scene scene = new Scene(mainContainer, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Hesap Hareketleri");
        stage.show();
    }

    // IBAN ayarlama metodu
    public void setIban(String iban) {
        this.iban = iban; // IBAN bilgisini ata
        hesapHareketleriniGetir(); // Hesap hareketlerini yükle
    }

    // Hesap hareketlerini veritabanından getir
    private void hesapHareketleriniGetir() {
        String query = "SELECT hi.tarih, hi.gonderen_iban, hi.alici_iban, hi.miktar, hi.aciklama, " +
                       "gonderen.ad AS gonderen_ad, gonderen.soyad AS gonderen_soyad, " +
                       "alici.ad AS alici_ad, alici.soyad AS alici_soyad " +
                       "FROM hesap_islemleri hi " +
                       "LEFT JOIN hesaplar hg ON hi.gonderen_iban = hg.iban " +
                       "LEFT JOIN hesaplar ha ON hi.alici_iban = ha.iban " +
                       "LEFT JOIN kullanicilar gonderen ON hg.kullanici_id = gonderen.id " +
                       "LEFT JOIN kullanicilar alici ON ha.kullanici_id = alici.id " +
                       "WHERE hi.gonderen_iban = ? OR hi.alici_iban = ? ORDER BY hi.tarih DESC";

        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, iban);
            stmt.setString(2, iban);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tarih = rs.getString("tarih");
                String gonderenIban = rs.getString("gonderen_iban");
                String aliciIban = rs.getString("alici_iban");
                double miktar = rs.getDouble("miktar");
                String aciklama = rs.getString("aciklama");
                String gonderenAd = rs.getString("gonderen_ad") + " " + rs.getString("gonderen_soyad");
                String aliciAd = rs.getString("alici_ad") + " " + rs.getString("alici_soyad");

                // Verileri arayüze ekle
                HBox hbox = ekranaEkle(tarih, gonderenIban, aliciIban, miktar, aciklama, gonderenAd, aliciAd);
                hareketContainer.getChildren().add(hbox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Arayüze işlem ekleme
    private HBox ekranaEkle(String tarih, String gonderenIban, String aliciIban, double miktar, String aciklama, String gonderenAd, String aliciAd) {
        // HBox ile satır oluştur
        HBox hbox = new HBox(20);
        hbox.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-spacing: 20; -fx-alignment: center-left;");

        // Tarih bilgisi
        VBox tarihBox = new VBox();
        Label gunLabel = new Label(tarih.substring(8, 10)); // Gün
        gunLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: center;");

        String[] aylar = {"OCA", "ŞUB", "MAR", "NİS", "MAY", "HAZ", "TEM", "AĞU", "EYL", "EKİ", "KAS", "ARA"};
        int ayIndex = Integer.parseInt(tarih.substring(5, 7)) - 1;
        Label ayYilLabel = new Label(aylar[ayIndex] + " " + tarih.substring(0, 4)); // Ay ve Yıl
        ayYilLabel.setStyle("-fx-font-size: 14px; -fx-alignment: center;");

        Label saatLabel = new Label(tarih.substring(11, 16)); // Saat ve Dakika
        saatLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray; -fx-alignment: center;");

        tarihBox.getChildren().addAll(gunLabel, ayYilLabel, saatLabel);
        tarihBox.setStyle("-fx-alignment: center;");

        // Bilgi alanları
        VBox bilgiBox = new VBox(5);
        Label bilgiLabel;
        if (iban.equals(gonderenIban)) { // Gönderen ise sadece alıcı adı
            bilgiLabel = new Label("Alıcı Adı: " + aliciAd);
        } else { // Alıcı ise sadece gönderen adı
            bilgiLabel = new Label("Gönderen Adı: " + gonderenAd);
        }
        bilgiLabel.setStyle("-fx-font-weight: bold;");

        Label aciklamaLabel = new Label("Açıklama: " + aciklama);
        aciklamaLabel.setStyle("-fx-font-weight: bold;");

        bilgiBox.getChildren().addAll(bilgiLabel, aciklamaLabel);

        Label miktarLabel = new Label(String.format("%.2f TL", miktar));
        miktarLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green; -fx-alignment: center-right;");
        HBox.setHgrow(miktarLabel, javafx.scene.layout.Priority.ALWAYS);

        if (iban.equals(gonderenIban)) {
            miktarLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: center-right;");
            miktarLabel.setText("-" + miktarLabel.getText());
        } else {
            miktarLabel.setText("+" + miktarLabel.getText());
        }

        hbox.getChildren().addAll(tarihBox, bilgiBox, miktarLabel);
        return hbox;
    }
}
