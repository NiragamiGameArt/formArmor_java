/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controleur;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import modele.Client;
import modele.GestionSql;
import modele.Session;
import sql.GestionBdd;

/**
 * FXML Controller class
 *
 * @author Philippe
 */
public class FenFXML_InscriptionControleur implements Initializable
{
    // Composants manipulés dans ce controleur
    @FXML
    ComboBox cmb_ChoixMatricule;
    @FXML
    Label lblNomClient;
    @FXML
    Button btnInscription;
    @FXML
    private TableView<Session> tableSessionsAutorisees;
    @FXML
    private TableColumn<Session, String> colonneId;
    @FXML
    private TableColumn<Session, String> colonneFormation_id;
    @FXML
    private TableColumn<Session, String> colonneDate_debut;
    @FXML
    private TableColumn<Session, String> colonneNb_places;
    @FXML
    private TableColumn<Session, String> colonneNb_inscrits;
    @FXML
    private TableColumn<Session, String> colonneClose;
    
    Stage secondaryStage;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //Initialisation du ComboBox cmb_ChoixMatricule
        ObservableList<Client> lesClients = GestionSql.getLesClients();
        cmb_ChoixMatricule.setItems(lesClients);
        
        
        // Ecoute sur le changement d'item du TableView
        tableSessionsAutorisees.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Session>()
        {
            @Override
            public void changed(ObservableValue<? extends Session> observable, Session oldValue, Session newValue)
            {
                // Si une ligne sélectionnée alors
                if (newValue == null)
                {
                    btnInscription.setVisible(false);
                }
                else
                {
                    btnInscription.setVisible(true);
                }
            }
        });
        
    }    
    
    @FXML
    public void handleChoixMatricule()
    {
        if (cmb_ChoixMatricule.getSelectionModel().getSelectedItem() != null)
        {
            lblNomClient.setText("Session(s) autorisée(s) pour " + ((Client)cmb_ChoixMatricule.getSelectionModel().getSelectedItem()).getNom());
            
            // Ajoute les données de la collection de données au TableView
            tableSessionsAutorisees.setItems(GestionSql.getLesSessions(((Client)(cmb_ChoixMatricule.getSelectionModel().getSelectedItem())).getId()));

            // Initialise le TableView tableSessionsAutorisees
            colonneId.setCellValueFactory(new PropertyValueFactory<Session, String>("id"));
            colonneFormation_id.setCellValueFactory(new PropertyValueFactory<Session, String>("libFormation"));
            colonneDate_debut.setCellValueFactory(new PropertyValueFactory<Session, String>("date_debut"));
            colonneNb_places.setCellValueFactory(new PropertyValueFactory<Session, String>("nb_places"));
            colonneNb_inscrits.setCellValueFactory(new PropertyValueFactory<Session, String>("nb_inscrits"));

            // Pour redimensionner les colonnes automatiquement
            tableSessionsAutorisees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }
    @FXML
    public void handleInscription()
    {
        // Conservation de l'item selectionné dans le TableView dans MainApp
        MainApp.setMaSessionSelectionnee(tableSessionsAutorisees.getSelectionModel().getSelectedItem());
        // Conservation de l'item selectionné dans le ComboBox dans MainApp
        MainApp.setMonClientSelectionne(((Client)cmb_ChoixMatricule.getSelectionModel().getSelectedItem()));
        
        // Création de la fenêtre de confirmation
        try
        {
            secondaryStage = new Stage();
            secondaryStage.setTitle("Confirmation de l'inscription à la session de formation");
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/vue/FenFXML_ConfirmationInscription.fxml"));
            //Session maSession = (Session)tableSessionsAutorisees.getSelectionModel().getSelectedItem();
            AnchorPane rootLayout = (AnchorPane) loader.load();
            Scene scene = new Scene(rootLayout);
            secondaryStage.setScene(scene);
            secondaryStage.show();
        }
        catch (IOException e)
        {
            System.out.println("Erreur chargement seconde fenetre : " + e.getMessage());
        }
    }
}
