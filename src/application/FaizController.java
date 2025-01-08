package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class FaizController {

    // Vadeli Hesap Tablosu
    @FXML private TableView<Hesap> hesapTablosu;
    @FXML private TableColumn<Hesap, String> hesapNoColumn;
    @FXML private TableColumn<Hesap, Double> bakiyeColumn;

    // Faize Yatırılan Tutarlar Tablosu
    @FXML private TableView<Faiz> faizTablosu;
    @FXML private TableColumn<Faiz, String> faizHesapNoColumn;
    @FXML private TableColumn<Faiz, Double> faizTutarColumn;
    @FXML private TableColumn<Faiz, Double> faizOraniColumn;
    @FXML private TableColumn<Faiz, Integer> faizSureColumn;
    @FXML private TableColumn<Faiz, Double> faizToplamColumn;

    // Giriş Alanları
    @FXML private TextField yatirTutarField;
    @FXML private TextField faizOraniField;
    @FXML private TextField sureField;
    @FXML private TextField toplamTutarField;

    // Veri listeleri
    private ObservableList<Hesap> hesapListesi = FXCollections.observableArrayList();
    private ObservableList<Faiz> faizListesi = FXCollections.observableArrayList();

    private int kullaniciId;

    @FXML
    public void initialize() {
        // Kullanıcı ID'yi al
        kullaniciId = AnaEkranController.kullaniciId;

        // Vadeli Hesap Tablosu
        hesapNoColumn.setCellValueFactory(new PropertyValueFactory<>("hesapNo"));
        bakiyeColumn.setCellValueFactory(new PropertyValueFactory<>("bakiye"));
        hesapTablosu.setItems(hesapListesi);

        // Faiz Tablosu
        faizHesapNoColumn.setCellValueFactory(new PropertyValueFactory<>("hesapNo"));
        faizTutarColumn.setCellValueFactory(new PropertyValueFactory<>("tutar"));
        faizOraniColumn.setCellValueFactory(new PropertyValueFactory<>("faizOrani"));
        faizSureColumn.setCellValueFactory(new PropertyValueFactory<>("sure"));
        faizToplamColumn.setCellValueFactory(new PropertyValueFactory<>("toplamTutar"));
        faizTablosu.setItems(faizListesi);

        // Vadeli hesapları yükle
        vadeliHesaplariGetir();
    }

    public void vadeliHesaplariGetir() {
        String query = "SELECT hesap_no, bakiye FROM hesaplar WHERE kullanici_id = ? AND hesap_turu = 'Vadeli'";
        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, kullaniciId);
            ResultSet rs = stmt.executeQuery();

            hesapListesi.clear();
            while (rs.next()) {
                hesapListesi.add(new Hesap(
                        rs.getString("hesap_no"),
                        rs.getDouble("bakiye")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void hesapla(ActionEvent event) {
        try {
            double yatirilanTutar = Double.parseDouble(yatirTutarField.getText());
            double faizOrani = Double.parseDouble(faizOraniField.getText());
            int sure = Integer.parseInt(sureField.getText());

            double toplamTutar = yatirilanTutar * Math.pow(1 + (faizOrani / 100), sure);
            toplamTutarField.setText(String.format("%.2f", toplamTutar));

        } catch (NumberFormatException e) {
            showError("Geçerli değerler giriniz!");
        }
    }

    @FXML
    private void yatir(ActionEvent event) {
        Hesap secilenHesap = hesapTablosu.getSelectionModel().getSelectedItem();

        if (secilenHesap != null) {
            try {
                double yatirilanTutar = Double.parseDouble(yatirTutarField.getText());
                double faizOrani = Double.parseDouble(faizOraniField.getText());
                int sure = Integer.parseInt(sureField.getText());

                if (secilenHesap.getBakiye() >= yatirilanTutar) {
                    double yeniBakiye = secilenHesap.getBakiye() - yatirilanTutar;
                    secilenHesap.setBakiye(yeniBakiye);

                    double toplamTutar = yatirilanTutar * Math.pow(1 + (faizOrani / 100), sure);

                    faizListesi.add(new Faiz(
                            secilenHesap.getHesapNo(),
                            yatirilanTutar,
                            faizOrani,
                            sure,
                            toplamTutar
                    ));

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                double bakiyeSon = secilenHesap.getBakiye() + toplamTutar;
                                Connection conn = databaseconnection.connect();
                                String updateSql = "UPDATE hesaplar SET bakiye = ? WHERE hesap_no = ?";
                                PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
                                updatePstmt.setDouble(1, bakiyeSon);
                                updatePstmt.setString(2, secilenHesap.getHesapNo());
                                updatePstmt.executeUpdate();
                                vadeliHesaplariGetir();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }, sure * 60 * 1000);

                    showInfo("Faiz işlemi başlatıldı!");

                } else {
                    showError("Yetersiz bakiye!");
                }
            } catch (NumberFormatException e) {
                showError("Geçerli değerler giriniz!");
            }
        } else {
            showError("Hesap seçiniz!");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
