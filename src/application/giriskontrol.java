package application;

import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class giriskontrol {

    @FXML
    private TextField tcKimlikField;

    @FXML
    private PasswordField sifreField;

    @FXML
    private Button girisButton;

    @FXML
    public void handleGiris() {
        String tcKimlik = tcKimlikField.getText();
        String sifre = sifreField.getText();

        String[] userInfo = validateLogin(tcKimlik, sifre);

        if (userInfo != null) {
            int kullaniciId = Integer.parseInt(userInfo[0]);
            String adSoyad = userInfo[1] + " " + userInfo[2];

            showAlert("Başarılı", "Giriş Başarılı!\nHoş Geldiniz, " + adSoyad);
            openAnaEkran(kullaniciId);
        } else {
            showAlert("Hata", "Geçersiz TC Kimlik No veya Şifre!");
        }
    }



    private String[] validateLogin(String tcKimlik, String sifre) {
        String query = "SELECT id, ad, soyad FROM kullanicilar WHERE tc_no = ? AND sifre = ?";

        try (Connection conn = databaseconnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tcKimlik);
            stmt.setString(2, sifre);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                return new String[]{
                    String.valueOf(rs.getInt("id")), 
                    rs.getString("ad"),
                    rs.getString("soyad")
                };
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    private void openAnaEkran(int kullaniciId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tasarim/anaekran.fxml"));
            Parent root = loader.load();


            AnaEkranController controller = loader.getController();
            controller.setKullaniciId(kullaniciId); 

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setTitle("Ana Ekran");
            stage.setScene(scene);

            stage.setMaximized(true);

            stage.show();

            Stage currentStage = (Stage) girisButton.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
