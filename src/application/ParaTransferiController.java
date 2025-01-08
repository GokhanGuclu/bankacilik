package application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ComboBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParaTransferiController {

    @FXML
    private VBox aliciListVBox;
    @FXML
    private Button kayıtlısec;
    @FXML
    private Button yenisec;
    @FXML
    private Button alicihesap;
    @FXML
    private Button gonderenhesap;
    @FXML
    private Button tutarbelirleme;
    @FXML
    private Button detaybelirleme;
    @FXML
    private HBox navigationBar;
    @FXML
    private HBox headerButtons;
    @FXML
    private ScrollPane hesapList;

    private int kullaniciId;
    private Button selectedAliciButton = null; 
    private Button selectedHesapButton = null;
    private String selectedIban = ""; 
    private String gonderenIban = ""; 
    private double tutar = 0; 
    private String aciklama = "";
    private String odemeTipi = "";

    public void setKullaniciId(int id) {
        this.kullaniciId = id;
    }
    
    private AnaEkranController anaSayfaController; 

	 public void setAnaSayfaController(AnaEkranController controller) {
	     this.anaSayfaController = controller;
	 }

    @FXML
    public void initialize() {
    	loadAliciList(kullaniciId);
    	
        yenisec.setOnAction(e -> {
            aliciListVBox.getChildren().clear(); 
            yeniAliciEkle();
            setupYeniBar();
        });

        kayıtlısec.setOnAction(e -> {
            aliciListVBox.getChildren().clear(); 
            loadAliciList(kullaniciId);
            setupKayıtlıBar();
        });
    }


    private void setupAliciNavigationBar() {
        alicihesap.setStyle("-fx-padding: 10px; -fx-background-color: #8B4513; -fx-text-fill: white; -fx-font-weight: bold;");
        gonderenhesap.setStyle("-fx-padding: 10px; -fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void setupGonderenNavigationBar() {
        gonderenhesap.setStyle("-fx-padding: 10px; -fx-background-color: #8B4513; -fx-text-fill: white; -fx-font-weight: bold;");
        tutarbelirleme.setStyle("-fx-padding: 10px; -fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");
    }   
    
    private void setupTutarNavigationBar() {
        tutarbelirleme.setStyle("-fx-padding: 10px; -fx-background-color: #8B4513; -fx-text-fill: white; -fx-font-weight: bold;");
        detaybelirleme.setStyle("-fx-padding: 10px; -fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");
    }
    
    private void setupDetayNavigationBar() {
        detaybelirleme.setStyle("-fx-padding: 10px; -fx-background-color: #8B4513; -fx-text-fill: white; -fx-font-weight: bold;");
    }
    
    private void setupKayıtlıBar() {
    	kayıtlısec.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-padding: 10px;");
    	yenisec.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-padding: 10px;");
    }

    private void setupYeniBar() {
    	yenisec.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-padding: 10px;");
    	kayıtlısec.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-padding: 10px;");    }

    private void loadAliciList(int kullaniciId) {
        String query = "SELECT kayitisim, iban FROM alicilar WHERE kullanici_id = ?";
        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, kullaniciId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String kayitisim = rs.getString("kayitisim");
                String iban = rs.getString("iban");

                Button aliciButton = new Button(kayitisim + " - " + iban);
                aliciButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-border-color: #cccccc;");
                aliciButton.setPrefWidth(500);
                aliciButton.setPrefHeight(60);

                aliciButton.setOnAction(e -> handleAliciSelect(aliciButton, iban));
                aliciListVBox.getChildren().add(aliciButton);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAliciSelect(Button aliciButton, String iban) {
        if (selectedAliciButton != null) {
            selectedAliciButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-border-color: #cccccc;");
        }
        selectedAliciButton = aliciButton;
        selectedIban = iban;
        aliciButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-border-color: #27ae60; -fx-border-width: 2px;");

        setupAliciNavigationBar();

        if (headerButtons != null) {
            headerButtons.getChildren().clear();
            hesapList.setTranslateY(hesapList.getTranslateY() - 130);
            hesapList.setPrefHeight(hesapList.getPrefHeight() + 130); 
        }

        loadHesapList(kullaniciId);
    }
    
    private void loadHesapList(int kullaniciId) {
        aliciListVBox.getChildren().clear();

        String query = "SELECT hesap_subesi, iban, bakiye FROM hesaplar WHERE kullanici_id = ?";
        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, kullaniciId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String hesapSubesi = rs.getString("hesap_subesi");
                String iban = rs.getString("iban");
                String bakiye = rs.getString("bakiye");

                Button hesapButton = new Button(hesapSubesi + " - Bakiye: " + bakiye + " TL");
                hesapButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-border-color: #cccccc;");
                hesapButton.setPrefWidth(500);
                hesapButton.setPrefHeight(60);

                hesapButton.setOnAction(e -> handleHesapSelect(hesapButton, iban));
                aliciListVBox.getChildren().add(hesapButton);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleHesapSelect(Button hesapButton, String iban) {
        if (selectedHesapButton != null) {
            selectedHesapButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: black; -fx-border-color: #cccccc;");
        }
        selectedHesapButton = hesapButton;
        gonderenIban = iban; 
        hesapButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-border-color: #27ae60; -fx-border-width: 2px;");
        
        setupGonderenNavigationBar();
        createTutarInput();
    }

    private void createTutarInput() {
        VBox tutarInputBox = new VBox();
        tutarInputBox.setSpacing(10);
        tutarInputBox.setStyle("-fx-padding: 20px 0px 0px 20px;");

        TextField tutarField = new TextField();
        tutarField.setPromptText("Tutar girin");
        tutarField.setPrefWidth(400);
        tutarField.setPrefHeight(40); 
        tutarField.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-radius: 5px;");

        Button onaylaButton = new Button("Onayla");
        onaylaButton.setPrefWidth(400); 
        onaylaButton.setPrefHeight(40); 
        onaylaButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px 20px; -fx-border-radius: 5px;");
        onaylaButton.setOnAction(e -> {
            try {
                tutar = Double.parseDouble(tutarField.getText());
                createDetayInput();
                setupTutarNavigationBar();
            } catch (NumberFormatException ex) {
            }
        });

        tutarInputBox.getChildren().addAll(tutarField, onaylaButton);
        aliciListVBox.getChildren().clear();
        aliciListVBox.getChildren().add(tutarInputBox);
    }
    
    private void createDetayInput() {
        VBox detayInputBox = new VBox();
        detayInputBox.setSpacing(10);
        detayInputBox.setStyle("-fx-padding: 20px 0px 0px 20px;");

        ComboBox<String> odemeTipiComboBox = new ComboBox<>();
        odemeTipiComboBox.getItems().addAll(
            "Konut Kirası",
            "İşyeri Kirası",
            "Diğer Kiralar",
            "Diğer Ödemeler"
        );
        odemeTipiComboBox.setValue("Diğer Ödemeler");
        odemeTipiComboBox.setPrefWidth(400);
        odemeTipiComboBox.setPrefHeight(40);
        odemeTipiComboBox.setStyle("-fx-font-size: 16px;");

        TextField aciklamaField = new TextField();
        aciklamaField.setPromptText("Açıklama");
        aciklamaField.setPrefWidth(400);
        aciklamaField.setPrefHeight(40);
        aciklamaField.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-radius: 5px;");

        Button devamButton = new Button("DEVAM");
        devamButton.setPrefWidth(400);
        devamButton.setPrefHeight(40);
        devamButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 16px; -fx-border-radius: 5px;");
        devamButton.setOnAction(e -> handleDetayOnay(odemeTipiComboBox.getValue(), aciklamaField.getText()));

        detayInputBox.getChildren().addAll(odemeTipiComboBox, aciklamaField, devamButton);
        aliciListVBox.getChildren().clear();
        aliciListVBox.getChildren().add(detayInputBox);
    }
    
    private void handleDetayOnay(String odemeTipiSecimi, String aciklamaMetni) {
        odemeTipi = odemeTipiSecimi;
        aciklama = aciklamaMetni;

        setupDetayNavigationBar();
        createOnayEkrani(); 
    }

    
    private void createOnayEkrani() {
        VBox onayEkraniBox = new VBox();
        onayEkraniBox.setSpacing(10);
        onayEkraniBox.setStyle("-fx-padding: 20px 0px 0px 20px;");

        String aliciAdi = getAliciAdi(selectedIban);
        String gonderenAdi = getGonderenAdi(gonderenIban);

        Label aliciLabel = new Label("Alıcı Hesap:");
        aliciLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label aliciIbanLabel = new Label("IBAN: " + selectedIban);
        Label aliciAdiLabel = new Label("Adı: " + aliciAdi);

        Label gonderenLabel = new Label("Gönderen Hesap:");
        gonderenLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label gonderenIbanLabel = new Label("IBAN: " + gonderenIban);
        Label gonderenAdiLabel = new Label("Adı: " + gonderenAdi);

        Label tutarLabel = new Label("Tutar:");
        tutarLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label tutarDegeriLabel = new Label(String.format("%.2f TL", tutar));

        Label detayLabel = new Label("Detaylar:");
        detayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label aciklamaLabel = new Label("Açıklama: " + aciklama);
        Label odemeTipiLabel = new Label("Gönderim Tipi: " + odemeTipi);

        Button gonderButton = new Button("GÖNDER");
        gonderButton.setPrefWidth(400);
        gonderButton.setPrefHeight(40);
        gonderButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 16px; -fx-border-radius: 5px;");
        gonderButton.setOnAction(e -> {
        });

        onayEkraniBox.getChildren().addAll(
            aliciLabel, aliciIbanLabel, aliciAdiLabel,
            gonderenLabel, gonderenIbanLabel, gonderenAdiLabel,
            tutarLabel, tutarDegeriLabel,
            detayLabel, aciklamaLabel, odemeTipiLabel,
            gonderButton
        );

        aliciListVBox.getChildren().clear();
        aliciListVBox.getChildren().add(onayEkraniBox);
        
        gonderButton.setOnAction(e -> {
            kaydetIslem();
        });

    }
    
    private void kaydetIslem() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseconnection.connect();
            conn.setAutoCommit(false);

            double gonderenBakiye = 0;
            String bakiyeSorgu = "SELECT bakiye FROM hesaplar WHERE iban = ?";
            stmt = conn.prepareStatement(bakiyeSorgu);
            stmt.setString(1, gonderenIban);
            rs = stmt.executeQuery();

            if (rs.next()) {
                gonderenBakiye = rs.getDouble("bakiye");
            } else {
                System.out.println("Gönderen IBAN bulunamadı!");
                conn.rollback(); 
                return;
            }

            if (gonderenBakiye < tutar) {
                System.out.println("Yetersiz bakiye!");
                conn.rollback();
                return;
            }

            double aliciBakiye = 0;
            stmt = conn.prepareStatement(bakiyeSorgu);
            stmt.setString(1, selectedIban);
            rs = stmt.executeQuery();

            if (rs.next()) {
                aliciBakiye = rs.getDouble("bakiye");
            } else {
                System.out.println("Alıcı IBAN bulunamadı!");
                conn.rollback();
                return;
            }

            String gonderenGuncelle = "UPDATE hesaplar SET bakiye = bakiye - ? WHERE iban = ?";
            stmt = conn.prepareStatement(gonderenGuncelle);
            stmt.setDouble(1, tutar);
            stmt.setString(2, gonderenIban);
            int gonderenUpdate = stmt.executeUpdate();

            if (gonderenUpdate <= 0) {
                System.out.println("Gönderen hesabın bakiyesi güncellenemedi!");
                conn.rollback();
                return;
            }

            String aliciGuncelle = "UPDATE hesaplar SET bakiye = bakiye + ? WHERE iban = ?";
            stmt = conn.prepareStatement(aliciGuncelle);
            stmt.setDouble(1, tutar);
            stmt.setString(2, selectedIban);
            int aliciUpdate = stmt.executeUpdate();

            if (aliciUpdate <= 0) {
                System.out.println("Alıcı hesabın bakiyesi güncellenemedi!");
                conn.rollback();
                return;
            }

            String islemEkle = "INSERT INTO hesap_islemleri (gonderen_iban, alici_iban, miktar, aciklama, odeme_turu) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(islemEkle);
            stmt.setString(1, gonderenIban);
            stmt.setString(2, selectedIban);
            stmt.setDouble(3, tutar);
            stmt.setString(4, aciklama);
            stmt.setString(5, odemeTipi);
            int islemEkleSonuc = stmt.executeUpdate();

            if (islemEkleSonuc <= 0) {
                System.out.println("İşlem kaydedilemedi!");
                conn.rollback();
                return;
            }

            conn.commit();
            System.out.println("İşlem başarıyla kaydedildi!");
            
            
            javafx.application.Platform.runLater(() -> {
                ((Stage) aliciListVBox.getScene().getWindow()).close();
            });

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback(); 
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.out.println("Veritabanına kaydedilirken hata oluştu.");
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }
    
    private String getAliciAdi(String iban) {
        String query = "SELECT k.ad, k.soyad FROM kullanicilar k " +
                       "JOIN hesaplar h ON k.id = h.kullanici_id " +
                       "WHERE h.iban = ?";
        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, iban);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ad") + " " + rs.getString("soyad");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Bilinmiyor";
    }

    private String getGonderenAdi(String iban) {
        String query = "SELECT k.ad, k.soyad FROM kullanicilar k " +
                       "JOIN hesaplar h ON k.id = h.kullanici_id " +
                       "WHERE h.iban = ?";
        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, iban);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ad") + " " + rs.getString("soyad");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Bilinmiyor";
    }
    
    private void yeniAliciEkle() {
        VBox yeniAliciBox = new VBox();
        yeniAliciBox.setSpacing(10);
        yeniAliciBox.setStyle("-fx-padding: 20px 0px 0px 20px;");

        TextField ibanField = new TextField();
        ibanField.setPromptText("Yeni IBAN girin");
        ibanField.setPrefWidth(400);
        ibanField.setPrefHeight(40);
        ibanField.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-radius: 5px;");

        Label sonucLabel = new Label();
        sonucLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        TextField isimField = new TextField();
        isimField.setPromptText("IBAN Sahibinin İsmini Girin");
        isimField.setPrefWidth(400);
        isimField.setPrefHeight(40);
        isimField.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-radius: 5px;");
        isimField.setVisible(false);

        CheckBox kaydetCheckbox = new CheckBox("Hesabı Kaydetmek İster misiniz?");
        kaydetCheckbox.setStyle("-fx-font-size: 14px;");
        kaydetCheckbox.setVisible(false);
        TextField baslikField = new TextField();
        baslikField.setPromptText("Hesap Başlığı Girin");
        baslikField.setPrefWidth(400);
        baslikField.setPrefHeight(40);
        baslikField.setStyle("-fx-font-size: 16px; -fx-padding: 10px; -fx-border-color: #cccccc; -fx-border-radius: 5px;");
        baslikField.setVisible(false);

        Button devamEtButton = new Button("Devam Et");
        devamEtButton.setPrefWidth(400);
        devamEtButton.setPrefHeight(40);
        devamEtButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 16px; -fx-border-radius: 5px;");
        devamEtButton.setVisible(false);

        Button kontrolButton = new Button("Kontrol Et");
        kontrolButton.setPrefWidth(400);
        kontrolButton.setPrefHeight(40);
        kontrolButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16px; -fx-border-radius: 5px;");

        kontrolButton.setOnAction(e -> {
            String girilenIban = ibanField.getText().trim();

            if (girilenIban.isEmpty()) {
                sonucLabel.setText("IBAN boş bırakılamaz!");
                sonucLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            String sahibi = getIbanSahibi(girilenIban);
            if (sahibi != null) {
                sonucLabel.setText("Sahibi: " + maskAdSoyad(sahibi));
                sonucLabel.setStyle("-fx-text-fill: green;");
                isimField.setVisible(true);
                devamEtButton.setVisible(true);
                kaydetCheckbox.setVisible(true);
            } else {
                sonucLabel.setText("Bu IBAN'a ait kullanıcı bulunamadı!");
                sonucLabel.setStyle("-fx-text-fill: red;");
                isimField.setVisible(false);
                devamEtButton.setVisible(false);
                kaydetCheckbox.setVisible(false);
            }
        });

        kaydetCheckbox.setOnAction(e -> {
            if (kaydetCheckbox.isSelected()) {
                baslikField.setVisible(true);
            } else {
                baslikField.setVisible(false);
            }
        });

        devamEtButton.setOnAction(e -> {
            String girilenIsim = isimField.getText().trim();
            String dogruIsim = getIbanSahibi(ibanField.getText().trim());

            if (girilenIsim.equalsIgnoreCase(dogruIsim)) {
                selectedIban = ibanField.getText().trim();

                if (kaydetCheckbox.isSelected()) {
                    String baslik = baslikField.getText().trim();
                    if (!baslik.isEmpty()) {
                        hesapKaydet(selectedIban, baslik);
                    }
                }

                loadHesapList(kullaniciId);
                setupAliciNavigationBar();

                if (headerButtons != null) {
                    headerButtons.getChildren().clear();
                    hesapList.setTranslateY(hesapList.getTranslateY() - 130);
                    hesapList.setPrefHeight(hesapList.getPrefHeight() + 130); 
                }
            } else {
                sonucLabel.setText("Girilen isim doğru değil!");
                sonucLabel.setStyle("-fx-text-fill: red;");
            }
        });

        yeniAliciBox.getChildren().addAll(
            ibanField, kontrolButton, sonucLabel, isimField,
            kaydetCheckbox, baslikField, devamEtButton
        );
        aliciListVBox.getChildren().clear();
        aliciListVBox.getChildren().add(yeniAliciBox);
    }


    
    private String getIbanSahibi(String iban) {
        String query = "SELECT k.ad, k.soyad FROM kullanicilar k " +
                       "JOIN hesaplar h ON k.id = h.kullanici_id " +
                       "WHERE h.iban = ?";
        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, iban);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ad") + " " + rs.getString("soyad");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private String maskAdSoyad(String adSoyad) {
        String[] parcalar = adSoyad.split(" ");
        if (parcalar.length == 2) {
            return parcalar[0].charAt(0) + "*".repeat(parcalar[0].length() - 1) + " " +
                   parcalar[1].charAt(0) + "*".repeat(parcalar[1].length() - 1);
        }
        return adSoyad;
    }

    private void hesapKaydet(String iban, String baslik) {
        String query = "INSERT INTO alicilar (kullanici_id, kayitisim, iban) VALUES (?, ?, ?)";
        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, kullaniciId);
            stmt.setString(2, baslik);
            stmt.setString(3, iban);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
            } else {
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
