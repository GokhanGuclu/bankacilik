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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;	
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
    
    @FXML
    private Button faizIslemleriButton;
    
    @FXML
    private void faizIslemleriAc() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tasarim/faiz.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Faiz İşlemleri");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    
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

    public static int kullaniciId;
   

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
                        rs.getDouble("bakiye")
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
        
        faizIslemleriButton.setOnAction(e -> {
            faizIslemleriAc();
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

            boolean veriVarMi = false;

            while (rs.next()) {
                veriVarMi = true;

                String hesapTuru = rs.getString("hesap_turu");
                double toplamBakiye = rs.getDouble("toplam_bakiye");

                if (hesapTuru.equals("Vadesiz")) {
                    vadesizLabel.setText(String.format("%.2f TL", toplamBakiye));
                } else if (hesapTuru.equals("Vadeli")) {
                    vadeliLabel.setText(String.format("%.2f TL", toplamBakiye));
                }
            }

            if (!veriVarMi) {
                vadesizLabel.setText("0.00 TL");
                vadeliLabel.setText("0.00 TL");
                tumHesaplarLabel.setText("0.00 TL");
            } else {
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

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Vadeli", vadeliToplam),
                    new PieChart.Data("Vadesiz", vadesizToplam)
            );

            pieChart.setData(pieChartData);

            pieChart.setLegendVisible(false);

            for (PieChart.Data data : pieChart.getData()) {
                if (data.getName().equals("Vadeli")) {
                    data.getNode().setStyle("-fx-pie-color: #FFFF00;"); 
                } else if (data.getName().equals("Vadesiz")) {
                    data.getNode().setStyle("-fx-pie-color: #007FFF;");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void verileriYukle() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    URL url = new URL(API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        throw new RuntimeException("HTTP Hatası: " + responseCode);
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder jsonResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponse.append(line);
                    }
                    reader.close();

                    JSONObject data = new JSONObject(jsonResponse.toString());
                    JSONObject rates = data.getJSONObject("conversion_rates");

                    double dolarAlis = 1 / rates.getDouble("USD");
                    double dolarSatis = dolarAlis * 1.05;
                    double euroAlis = 1 / rates.getDouble("EUR");
                    double euroSatis = euroAlis * 1.05;
                    double sterlinAlis = 1 / rates.getDouble("GBP");
                    double sterlinSatis = sterlinAlis * 1.05;
                    double cadAlis = 1 / rates.getDouble("CAD");
                    double cadSatis = cadAlis * 1.05;
                    double chfAlis = 1 / rates.getDouble("CHF");
                    double chfSatis = chfAlis * 1.05;

                    Platform.runLater(() -> {
                        veriBox.getChildren().clear();
                        veriBox.getChildren().add(createTableHeader());
                        veriBox.getChildren().add(createTableRow("USD", "flags/us.png", dolarAlis, dolarSatis));
                        veriBox.getChildren().add(createTableRow("EUR", "flags/eu.png", euroAlis, euroSatis));
                        veriBox.getChildren().add(createTableRow("GBP", "flags/gb.png", sterlinAlis, sterlinSatis));
                        veriBox.getChildren().add(createTableRow("CHF", "flags/ch.png", chfAlis, chfSatis));
                        veriBox.getChildren().add(createTableRow("CAD", "flags/ca.png", cadAlis, cadSatis));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> veriBox.getChildren().add(new Label("Veriler yüklenemedi!")));
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private HBox createTableHeader() {
        HBox header = new HBox(15);
        header.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0; -fx-font-weight: bold;");
        header.getChildren().addAll(new Label("Para Birimi"), new Label("Alış"), new Label("Satış"));
        return header;
    }

    private HBox createTableRow(String label, String flagPath, double alis, double satis) {
        HBox row = new HBox(15);
        row.setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        ImageView flag = new ImageView(new Image(flagPath));
        flag.setFitHeight(20);
        flag.setFitWidth(30);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label alisLbl = new Label(String.format("₺%.4f", alis));
        alisLbl.setStyle("-fx-font-size: 14px;");

        Label satisLbl = new Label(String.format("₺%.4f", satis));
        satisLbl.setStyle("-fx-font-size: 14px;");

        row.getChildren().addAll(flag, lbl, alisLbl, satisLbl);
        return row;
    }

}


