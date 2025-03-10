/**
 * Sample Skeleton for 'SerieA.fxml' Controller Class
 */

package it.polito.tdp.seriea;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import it.polito.tdp.seriea.model.Model;
import it.polito.tdp.seriea.model.Season;
import it.polito.tdp.seriea.model.Team;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

public class SerieAController {
	
	private Model model;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="boxSquadra"
    private ChoiceBox<Team> boxSquadra; // Value injected by FXMLLoader

    @FXML // fx:id="btnSelezionaSquadra"
    private Button btnSelezionaSquadra; // Value injected by FXMLLoader

    @FXML // fx:id="btnTrovaAnnataOro"
    private Button btnTrovaAnnataOro; // Value injected by FXMLLoader

    @FXML // fx:id="btnTrovaCamminoVirtuoso"
    private Button btnTrovaCamminoVirtuoso; // Value injected by FXMLLoader

    @FXML // fx:id="txtResult"
    private TextArea txtResult; // Value injected by FXMLLoader

    @FXML
    void doSelezionaSquadra(ActionEvent event) {
    	Team t = boxSquadra.getValue();
    	
    	if(t==null) {
    		txtResult.appendText("Devi selezionare una squadra\n");
    		return;
    	}
    	
    	Map<Season, Integer> punteggi = model.getPunteggi(t);
    	txtResult.clear();
    	for(Season s: punteggi.keySet()) {
    		txtResult.appendText(String.format("%s: %d\n", s.getDescription(), punteggi.get(s)) );
    	}
    	//fai in modo che l'utente non clicchi sul bottono sbagliato
    	btnTrovaAnnataOro.setDisable(false);
        btnTrovaCamminoVirtuoso.setDisable(true);

    }

    @FXML
    void doTrovaAnnataOro(ActionEvent event) {
    	Season annata = model.calcolaAnnataDOro();
    	int deltaPesi = model.getDeltaPesi() ;
    	txtResult.appendText(String.format("Annata d'oro: %s (differenza pesi %d)\n", annata.getDescription(), deltaPesi));

        btnTrovaAnnataOro.setDisable(false);
        btnTrovaCamminoVirtuoso.setDisable(false);
    }

    @FXML
    void doTrovaCamminoVirtuoso(ActionEvent event) {
    	List<Season> percorso = model.camminoVirtuoso() ;
//    	txtResult.appendText(percorso.toString());
    	for(Season s: percorso) {
    		txtResult.appendText(String.format("%s: %d\n", s.getDescription(), model.getPunteggio(s)));
    	}

    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert boxSquadra != null : "fx:id=\"boxSquadra\" was not injected: check your FXML file 'SerieA.fxml'.";
        assert btnSelezionaSquadra != null : "fx:id=\"btnSelezionaSquadra\" was not injected: check your FXML file 'SerieA.fxml'.";
        assert btnTrovaAnnataOro != null : "fx:id=\"btnTrovaAnnataOro\" was not injected: check your FXML file 'SerieA.fxml'.";
        assert btnTrovaCamminoVirtuoso != null : "fx:id=\"btnTrovaCamminoVirtuoso\" was not injected: check your FXML file 'SerieA.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'SerieA.fxml'.";
        
     // i bottoni Annata d'oro e Cammino Virtuoso si abilitano solo dopo avere scelto la squadra
        btnTrovaAnnataOro.setDisable(true);
        btnTrovaCamminoVirtuoso.setDisable(true);

        // disable buttons when team is changed
        // https://stackoverflow.com/a/35282753/986709
       
        boxSquadra.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Team>() {
			@Override
			public void changed(ObservableValue<? extends Team> observable, Team oldValue, Team newValue) {
		        btnTrovaAnnataOro.setDisable(true);
		        btnTrovaCamminoVirtuoso.setDisable(true);				
			}
		});
       
    }
   
    
    public void setModel(Model model) {
    	this.model = model;
    	this.boxSquadra.getItems().clear();
    	this.boxSquadra.getItems().addAll(model.listTeams());
    }
    
    
}
