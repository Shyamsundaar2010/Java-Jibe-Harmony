package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.util.*;
import java.sql.SQLException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javafx.scene.Parent;

public class HelloController implements Initializable {
    @FXML
    private Button playbutton;
    @FXML
    private Button nextbutton;
    @FXML
    private ListView<String> songList;
    private File tempFile;
    private ArrayList<File> songs;
    private MediaPlayer mediaPlayer;
    private Media media;
    private int songnumber;

    public void play() {
        mediaPlayer.play();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            songs = new ArrayList<File>();
            String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
            String username = "postgres";
            String password = "1007";
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            String selectQuery = "SELECT audio_data, audio_name FROM audio_files ";
            //int audioId = 1; // Change to the appropriate audio file ID
            ObservableList<String> audioNames = FXCollections.observableArrayList();
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            //preparedStatement.setInt(1, audioId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                byte[] audioData = resultSet.getBytes("audio_data");
                String audioName = resultSet.getString("audio_name");
                ByteArrayInputStream bis = new ByteArrayInputStream(audioData);
                // Save the audio data to a temporary file
                tempFile = File.createTempFile("audio", ".mp3");
                songs.add(tempFile);
                //audioFiles.put(audioName, Arrays.toString(audioData));
                System.out.println(tempFile.getAbsolutePath());
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    audioNames.add(audioName);
                }
                //media = new Media(tempFile.toURI().toString());
                //mediaPlayer = new MediaPlayer(media);
            }
            songList.setItems(audioNames);
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        System.out.println("songs");
        System.out.println(songs);
        media = new Media(songs.get(songnumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        /*songList.setOnMouseClicked(event ->{
            ReadOnlyObjectProperty<String> selectedSong = songList.getSelectionModel().selectedItemProperty();
            String audioFilePath = audioFiles.get(selectedSong);
            playSelectedSong(audioFilePath);
        });*/
        mediaPlayer.setOnEndOfMedia(() -> {
            tempFile.delete();
        });
    }

    public void next() {
        if (songnumber < songs.size() - 1) {
            songnumber++;
            mediaPlayer.stop();
            media = new Media(songs.get(songnumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            //songname.setText(songs.get(songnumber).getName());
            play();
        } else {
            songnumber = 0;
            mediaPlayer.stop();
            media = new Media(songs.get(songnumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            //songname.setText(songs.get(songnumber).getName());
            play();
        }
    }
    /*public void playSelectedSong(String audioFilePath){
        System.out.println("Audio File Path" + audioFilePath);
        media = new Media(new File(audioFilePath).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
    }*/

    public void add(ActionEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("add-song.fxml"));
        Stage stage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        //((Node)(event.getSource())).getScene().getWindow().hide();
    }
}