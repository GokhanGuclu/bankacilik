package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class AnaEkranController {

    @FXML
    private VBox sidebar;

    @FXML
    private Label subeLabel;

    @FXML
    private Label hesapNoLabel;

    @FXML
    private Label kullaniciadilabel;

    @FXML
    private Label ibanLabel;

    @FXML
    private Label bakiyeLabel;

    @FXML
    private Button solOkButton;

    @FXML
    private Button sagOkButton;

    @FXML
    private Button paraTransferButton;

    @FXML
    private Button hesapHareketButton;
    
    @FXML
    private Label vadesizLabel;
    
    @FXML
    private Label vadeliLabel;
    
    @FXML
    private Label tumHesaplarLabel;
    
    @FXML
    private PieChart pieChart;
    
    @FXML
    private VBox veriBox;
    
    private final String API_KEY = "1e695f9b72f13badc3cb6dec";
    private final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/TRY";


    private List<Hesap> hesapListesi = new ArrayList<>();
    private int mevcutHesapIndex = 0;

    private static Kullanici currentUser;

    public static void setCurrentUser(Kullanici kullanici) {
        currentUser = kullanici;
    }

    public static Kullanici getCurrentUser() {
        return currentUser;
    }

    private int kullaniciId;
   

    public void setKullaniciId(int id) {
        this.kullaniciId = id;
        hesaplariGetir();
        kullaniciAdiniGetir();        
        varliklariGetir();
    }

    private void kullaniciAdiniGetir() {
        String query = "SELECT id, ad, soyad, email, telefon FROM kullanicilar WHERE id = ?";
        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, kullaniciId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String ad = rs.getString("ad");
                String soyad = rs.getString("soyad");
                String mail = rs.getString("email");
                String telefon = rs.getString("telefon");
                Kullanici kullanici = new Kullanici(kullaniciId, ad, soyad, mail, telefon);
                Kullanici.setCurrentUser(kullanici); 
                String tamisim = ad + " " + soyad;
                kullaniciadilabel.setText(tamisim);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void hesaplariGetir() {
        String query = "SELECT hesap_subesi, hesap_no, iban, bakiye FROM hesaplar WHERE kullanici_id = ?";
        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, kullaniciId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                hesapListesi.add(new Hesap(
                        rs.getString("hesap_subesi"),
                        rs.getString("hesap_no"),
                        rs.getString("iban"),
                        rs.getString("bakiye")
                ));
            }
            if (!hesapListesi.isEmpty()) {
                hesapGoster(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hesapGoster(int index) {
        Hesap hesap = hesapListesi.get(index);
        subeLabel.setText(hesap.getSube());
        hesapNoLabel.setText(hesap.getHesapNo());
        ibanLabel.setText(hesap.getIban());
        bakiyeLabel.setText(hesap.getBakiye() + " TL");
    }

    @FXML
    public void initialize() {    	
        paraTransferButton.setOnAction(e -> {
            openParaTransferiWindow(kullaniciId);
        });
        
        hesapHareketButton.setOnAction(e -> {
            openHesapHareketleriWindow(ibanLabel.getText());
        });
            
        solOkButton.setOnAction(e -> {
            if (mevcutHesapIndex > 0) {
                mevcutHesapIndex--;
            } else {
                mevcutHesapIndex = hesapListesi.size() - 1;
            }
            hesapGoster(mevcutHesapIndex);
        });

        sagOkButton.setOnAction(e -> {
            if (mevcutHesapIndex < hesapListesi.size() - 1) {
                mevcutHesapIndex++;
            } else {
                mevcutHesapIndex = 0;
            }
            hesapGoster(mevcutHesapIndex);
        });
        verileriYukle();
            
    }
    
    private void openParaTransferiWindow(int kullaniciId) {
        try {
            deactivateMainButtons();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tasarim/paraTransferi.fxml"));
            Stage stage = new Stage();
            loader.setControllerFactory(c -> {
                ParaTransferiController controller = new ParaTransferiController();
                controller.setKullaniciId(kullaniciId);
                return controller;
            });
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Para Transferi");

            stage.setOnHiding(event -> activateMainButtons());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void openHesapHareketleriWindow(String iban) {
        try {
            Stage stage = new Stage();
            HesapHareketleriController controller = new HesapHareketleriController();
            controller.start(stage); 
            controller.setIban(iban);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void deactivateMainButtons() {
        paraTransferButton.setDisable(true);
        hesapHareketButton.setDisable(true);
        solOkButton.setDisable(true);
        sagOkButton.setDisable(true);
    }

    private void activateMainButtons() {
        paraTransferButton.setDisable(false);
        hesapHareketButton.setDisable(false);
        solOkButton.setDisable(false);
        sagOkButton.setDisable(false);
    }
    
    private void varliklariGetir() {
        String query = "SELECT hesap_turu, SUM(bakiye) AS toplam_bakiye FROM hesaplar WHERE kullanici_id = ? GROUP BY hesap_turu";

        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, kullaniciId);
            ResultSet rs = stmt.executeQuery();
            
            pieChartVerileriniYukle();	

            boolean veriVarMi = false; // Veri kontrolü

            while (rs.next()) {
                veriVarMi = true; // Veri bulunduğunu işaretle

                String hesapTuru = rs.getString("hesap_turu");
                double toplamBakiye = rs.getDouble("toplam_bakiye");

                if (hesapTuru.equals("Vadesiz")) {
                    vadesizLabel.setText(String.format("%.2f TL", toplamBakiye));
                } else if (hesapTuru.equals("Vadeli")) {
                    vadeliLabel.setText(String.format("%.2f TL", toplamBakiye));
                }
            }

            // Eğer veri yoksa varsayılan değer ata
            if (!veriVarMi) {
                vadesizLabel.setText("0.00 TL");
                vadeliLabel.setText("0.00 TL");
                tumHesaplarLabel.setText("0.00 TL");
            } else {
                // Tüm hesapların toplamını tekrar sorgulayıp hesapla
                String toplamQuery = "SELECT SUM(bakiye) AS genel_toplam FROM hesaplar WHERE kullanici_id = ?";
                try (PreparedStatement stmt2 = conn.prepareStatement(toplamQuery)) {
                    stmt2.setInt(1, kullaniciId);
                    ResultSet rs2 = stmt2.executeQuery();
                    if (rs2.next()) {
                        double genelToplam = rs2.getDouble("genel_toplam");
                        tumHesaplarLabel.setText(String.format("%.2f TL", genelToplam));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    private void pieChartVerileriniYukle() {
        String query = "SELECT hesap_turu, SUM(bakiye) AS toplam_bakiye FROM hesaplar WHERE kullanici_id = ? GROUP BY hesap_turu";

        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, kullaniciId);
            ResultSet rs = stmt.executeQuery();

            double vadeliToplam = 0.0;
            double vadesizToplam = 0.0;

            while (rs.next()) {
                String hesapTuru = rs.getString("hesap_turu");
                double toplamBakiye = rs.getDouble("toplam_bakiye");

                if (hesapTuru.equals("Vadeli")) {
                    vadeliToplam = toplamBakiye;
                } else if (hesapTuru.equals("Vadesiz")) {
                    vadesizToplam = toplamBakiye;
                }
            }

            // PieChart verilerini ekleyelim
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Vadeli", vadeliToplam),
                    new PieChart.Data("Vadesiz", vadesizToplam)
            );

            pieChart.setData(pieChartData);

            // Legend'ı kaldır
            pieChart.setLegendVisible(false);

            // Renkleri özelleştir
            for (PieChart.Data data : pieChart.getData()) {
                if (data.getName().equals("Vadeli")) {
                    data.getNode().setStyle("-fx-pie-color: #FFFF00;"); // Turuncu
                } else if (data.getName().equals("Vadesiz")) {
                    data.getNode().setStyle("-fx-pie-color: #007FFF;"); // Mavi
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void verileriYukle() {
        try {
            // --- DÖVİZ KURLARI İÇİN API BAĞLANTISI ---
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            // Yanıt kodunu kontrol et
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HTTP Hatası: " + responseCode);
            }

            // API yanıtını oku
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonResponse = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResponse.append(line);
            }
            reader.close();

            // JSON verisini işle
            JSONObject data = new JSONObject(jsonResponse.toString());
            JSONObject rates = data.getJSONObject("conversion_rates");

            // Döviz kurlarını al
            double dolarAlis = 1 / rates.getDouble("USD");
            double dolarSatis = dolarAlis * 1.05; // %5 kar oranı
            double euroAlis = 1 / rates.getDouble("EUR");
            double euroSatis = euroAlis * 1.05;
            double sterlinAlis = 1 / rates.getDouble("GBP");
            double sterlinSatis = sterlinAlis * 1.05;

            // --- ALTIN FİYATLARI İÇİN API BAĞLANTISI ---
            String goldApiUrl = "https://www.goldapi.io/api/XAU/USD";
            HttpURLConnection goldConn = (HttpURLConnection) new URL(goldApiUrl).openConnection();
            goldConn.setRequestMethod("GET");

            // API başlıklarını ekle
            goldConn.setRequestProperty("x-access-token", "goldapi-5hzhesm5jn34jj-io");
            goldConn.setRequestProperty("Content-Type", "application/json");
            goldConn.connect();

            // Altın API yanıtını oku
            BufferedReader goldReader = new BufferedReader(new InputStreamReader(goldConn.getInputStream()));
            StringBuilder goldResponse = new StringBuilder();
            while ((line = goldReader.readLine()) != null) {
                goldResponse.append(line);
            }
            goldReader.close();

            // Altın verilerini işle
            JSONObject goldData = new JSONObject(goldResponse.toString());
            double altinUsd = goldData.getDouble("price");

            double altinAlis = altinUsd * dolarAlis;
            double altinSatis = altinAlis * 1.05;

            // --- VBox içine verileri ekleyelim ---
            veriBox.getChildren().clear();
            veriBox.setSpacing(15);
            veriBox.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 20; -fx-border-color: #ddd; -fx-border-radius: 10;");

            veriBox.getChildren().add(createInfoBox("USD", dolarAlis, dolarSatis));
            veriBox.getChildren().add(createInfoBox("EUR", euroAlis, euroSatis));
            veriBox.getChildren().add(createInfoBox("GBP", sterlinAlis, sterlinSatis));
            veriBox.getChildren().add(createInfoBox("ALTIN", altinAlis, altinSatis));

        } catch (java.net.UnknownHostException e) {
            veriBox.getChildren().add(new Label("Bağlantı hatası: Lütfen internet bağlantınızı kontrol edin."));
        } catch (Exception e) {
            e.printStackTrace();
            veriBox.getChildren().add(new Label("Veriler yüklenemedi!"));
        }
    }

    private HBox createInfoBox(String label, double alis, double satis) {
        HBox box = new HBox();
        box.setSpacing(20);
        box.setStyle("-fx-padding: 20; -fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        VBox content = new VBox();
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        HBox alisBox = new HBox();
        Label alisLbl = new Label("Alış:");
        alisLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label alisVal = new Label("₺" + String.format("%.4f", alis));
        alisVal.setStyle("-fx-font-size: 14px;");
        alisBox.setSpacing(10);
        alisBox.getChildren().addAll(alisLbl, alisVal);

        HBox satisBox = new HBox();
        Label satisLbl = new Label("Satış:");
        satisLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label satisVal = new Label("₺" + String.format("%.4f", satis));
        satisVal.setStyle("-fx-font-size: 14px;");
        satisBox.setSpacing(10);
        satisBox.getChildren().addAll(satisLbl, satisVal);

        content.getChildren().addAll(lbl, alisBox, satisBox);
        box.getChildren().add(content);
        return box;
    }
}


