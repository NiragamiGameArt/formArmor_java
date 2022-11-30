/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import javafx.collections.FXCollections;
import sql.GestionBdd;

import javafx.collections.ObservableList;

public class GestionSql
{
    
    //Requete permettant de retourner l'ensemble des clients
    public static ObservableList<Client> getLesClients()
    {
        Connection conn;
        Statement stmt1;
        Client monClient;
        ObservableList<Client> lesClients = FXCollections.observableArrayList();
        try
        {
            // On prévoit 2 connexions à la base
            stmt1 = GestionBdd.connexionBdd(GestionBdd.TYPE_MYSQL, "symfony5_formarmor","localhost", "root","");
            
            // Liste des clients qui "ont un plan de formation"
            String req = "select distinct c.id, statut_id, nom, password, adresse, cp, ville, email, nbhcpta, nbhbur from client c, plan_formation p "
            + "where c.id = p.id order by c.id";
            ResultSet rs = GestionBdd.envoiRequeteLMD(stmt1,req);
            while (rs.next())
            {
                monClient = new Client(rs.getInt("id"), rs.getInt("statut_id"), rs.getInt("nbhcpta"), rs.getInt("nbhbur"), rs.getString("nom"), rs.getString("password"), rs.getString("adresse"), rs.getString("cp"), rs.getString("ville"), rs.getString("email"));
                lesClients.add(monClient);
            }
        }
        catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
        return lesClients;
    }
    
    //Requête permettant de  retourner les sessions autorisées pour le client sélectionné
    public static ObservableList<Session> getLesSessions(int client_id)
    {
        Connection conn;
        Statement stmt1;
        Session maSession;
        ObservableList<Session> lesSessions = FXCollections.observableArrayList();
        try
        {
            // On prévoit 2 connexions à la base
            stmt1 = GestionBdd.connexionBdd(GestionBdd.TYPE_MYSQL, "symfony5_formarmor","localhost", "root","");
            
            // Sélection des sessions autorisées pour le client choisi
            String req = "select c.nom, s.id, f.libelle, f.niveau, date_debut, duree, nb_places, nb_inscrits, coutrevient ";
            req += "from session_formation s, client c, plan_formation p, formation f ";
            req += "where c.id = '" + client_id + "' ";
            req += "and p.client_id = c.id and nb_places > nb_inscrits ";
            req += "and p.formation_id = f.id ";
            req += "and s.formation_id = f.id ";
            // et date supérieure à la date du jour
            req += "and close = 0 and effectue = 0 and s.id Not In ";
            req += "(Select session_formation_id From inscription Where id = '" + client_id + "')";
            ResultSet rs = GestionBdd.envoiRequeteLMD(stmt1,req);
            while (rs.next())
            {
                // A MODIFIER
                maSession = new Session(rs.getInt("id"), rs.getString("libelle"), rs.getDate("date_debut"), rs.getInt("nb_places"), rs.getInt("nb_inscrits"));
                lesSessions.add(maSession);
            }
        }
        catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesSessions : " + se.getMessage());
        }
        return lesSessions;
    }
    
    //Requête permettant l'insertion de l'inscription dans la table inscription et
    //la mise à jour de la table session_formation (+1 inscrit) et
    //la mise à jour de la table plan_formation (effectue passe à 1)
    public static void insereInscription(int matricule, int session_formation_id)
    {
        Statement stmt1;
        
        GregorianCalendar dateJour = new GregorianCalendar();
        String ddate = dateJour.get(GregorianCalendar.YEAR) + "-" + (dateJour.get(GregorianCalendar.MONTH) + 1) + "-" + dateJour.get(GregorianCalendar.DATE);
        // Insertion dans la table inscription
        String req = "Insert into inscription(client_id, session_formation_id, date_inscription) values (" + matricule;
        req += ", " + session_formation_id + ",'" + ddate + "')";
        System.out.println("SQL:" + req);
        // M.A.J de la table session_formation (un inscrit de plus)
        String req2 = "Update session_formation set nb_inscrits = nb_inscrits +1 Where id = " + session_formation_id;
        // Récupération du numéro de la session concernée
        System.out.println("SQL2:" + req2);
        String req3 = "Select formation_id from session_formation where id = " + session_formation_id;
        System.out.println("SQL3:" + req3);
        stmt1 = GestionBdd.connexionBdd(GestionBdd.TYPE_MYSQL, "symfony5_formarmor", "localhost", "root", "");
        ResultSet rs = GestionBdd.envoiRequeteLMD(stmt1, req3);
        int numForm=0;
        try
        {
            rs.first();
            numForm = rs.getInt(1);
        }
        catch(Exception e)
        {
            System.out.println("Erreur requete3 " + e.getMessage());
        }
        // M.A.J de la table plan_formation (effectue passe à 1 pour le client et la session concernés)
        String req4 = "Update plan_formation set effectue = 1 Where client_id = " + matricule;
        req4 += " And formation_id = " + numForm;
        int nb1 = GestionBdd.envoiRequeteLID(stmt1, req);
        int nb2 = GestionBdd.envoiRequeteLID(stmt1, req2);
        int nb3 = GestionBdd.envoiRequeteLID(stmt1, req4);
    }
}
