package it.polito.tdp.seriea.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {
	
	private SerieADAO dao; 
	private List<Team> squadre;
	private List<Season> stagioni;
	
	private Team squadraSelezionata;
	private Map<Season, Integer> punteggi;
	private List<Season> stagioniConsecutive;
	
	private Map<Integer, Season> stagioniIdMap; 
	private Map<String, Team> squadreIdMap; 
	
	private Graph<Season, DefaultWeightedEdge> grafo;
	
	private List<Season> percorsoBest;
	private int deltaPesi;
	
	public Model() {
		this.dao = new SerieADAO();
		this.squadre = dao.listTeams();
		this.squadreIdMap = new HashMap<String, Team>();
		for(Team t: this.squadre) {
			this.squadreIdMap.put(t.getTeam(), t);
		}
		
		this.stagioni = dao.listAllSeasons();
		this.stagioniIdMap = new HashMap<Integer, Season>();
		for(Season s: this.stagioni) {
			this.stagioniIdMap.put(s.getSeason(), s);
		}
	}
	
	public List<Team> listTeams(){
		return this.squadre;
	}
	
	public Map<Season, Integer> getPunteggi(Team squadra){
		this.squadraSelezionata = squadra; //squadra selezionata dall'utente
		this.punteggi = new HashMap<Season, Integer>();
		List<Match> partite = dao.listMatchesForteam(squadra, stagioniIdMap, squadreIdMap);
		for(Match m : partite) {
			Season stagione = m.getSeason();
			int punti = 0;
			if(m.getFtr().equals("D")) {
				punti = 1;
			} else {
				if((m.getHomeTeam().equals(squadra) && m.getFtr().equals("H")) ||
						(m.getAwayTeam().equals(squadra) && m.getFtr().equals("A"))) {
					punti = 3;
				}
			}
			Integer attuale = punteggi.get(stagione);
			if(attuale ==null)
				attuale = 0;
			punteggi.put(stagione, attuale+punti);
		}
		return punteggi;
	}
	
	public Season calcolaAnnataDOro() {
		//Costruisco il grafo
		this.grafo = new SimpleDirectedWeightedGraph<Season, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(this.grafo, punteggi.keySet());
		
		for(Season s1: punteggi.keySet()) {
			for(Season s2: punteggi.keySet()) {
				if(!s1.equals(s2)) {
					int punti1 = punteggi.get(s1);
					int punti2 = punteggi.get(s2);
					
					if(punti1>punti2) {
						Graphs.addEdge(this.grafo, s2, s1, punti1-punti2);
					} else {
						Graphs.addEdge(this.grafo, s1, s2, punti2-punti1);
					}
				}
			}
		}
		//Trovo l'annata d'oro
		
		if (grafo.vertexSet().isEmpty()) {
			// caso particolare: nessuna stagione
			this.deltaPesi = 0;
			return null;
		} else if (grafo.vertexSet().size() == 1) {
			// caso particolare: se c'è solo una stagione
			this.deltaPesi = 0;
			/*
			 * ugly trick to extract the only element in the set. See:
			 * <https://stackoverflow.com/questions/5229137/the-correct-way-to-return-the-only-element-from-a-set>
			 */
			Season unica = grafo.vertexSet().iterator().next();
			return unica;
		} else {

			// trovo l'annata migliore
			Season migliore = null;
			int max = 0;
			for (Season s : grafo.vertexSet()) {
				int valore = pesoStagione(s);
				if (valore > max) {
					max = valore;
					migliore = s;
				}
			}
			this.deltaPesi = max;
			return migliore;
		}
	}
	
	public int getDeltaPesi() {
		return this.deltaPesi;
	}

	
	private int pesoStagione(Season s) {
		int somma = 0;
		
		//per tutti gli archi che sono incidenti su s sommo il peso
		for(DefaultWeightedEdge e :grafo.incomingEdgesOf(s)) {
			somma = somma + (int)grafo.getEdgeWeight(e);
		}
		//per tutti gli archi uscenti da s sottraggo il peso
		for(DefaultWeightedEdge e :grafo.outgoingEdgesOf(s)) {
			somma = somma - (int)grafo.getEdgeWeight(e);
		}
		
		return somma;
	}
	
	public int getPunteggio(Season s) {
		return this.punteggi.get(s) ;
	}
	
	public List<Season> camminoVirtuoso() {
		// trova le stagioni consecutive
		this.stagioniConsecutive = new ArrayList<Season>(punteggi.keySet()); //Passo da un Set ad una List perchè in questo modo posso ordinarla
		Collections.sort(this.stagioniConsecutive);

		// prepara le variabili utili alla ricorsione (strutture dati necessarie)
		List<Season> parziale = new ArrayList<Season>();
		this.percorsoBest = new ArrayList<>();
		
		// devo dare al metodo ricorsivo un paziale con il livello 0 pieno
		for (Season s : grafo.vertexSet()) {
			parziale.add(s); //inserisco il primo elemento 
			cerca(1, parziale); //faccio la ricorsione
			parziale.remove(0); //rimuovo il livello 0 
		}
		return percorsoBest;
	}
	/*
	 * RICORSIONE
	 * 
	 * Soluzione parziale: Lista di Season (lista di vertici) 
	 * Livello ricorsione: lunghezza della lista Casi terminali: non trova altri vertici da aggiungere
	 * Casi terminali: non trova altri vertici da aggiungere
	 * 				-> verifica se il cammino ha lunghezza massima tra quelli visti finora
	 * Generazione delle soluzioni: vertici connessi all'ultimo vertice del percorso
	 * (con arco orientato nel verso giusto), non ancora parte del percorso, relativi a stagioni consecutive.
	 */
		
	private void cerca(int livello, List<Season> parziale) {
		boolean trovato = false;
		// genera nuove soluzioni
		Season ultimo = parziale.get(livello - 1);//prendo l'ultimo elemento
		
		//dall'ultimo vertice devo trovare i suoi vertici successori
		for (Season prossimo : Graphs.successorListOf(grafo, ultimo)) {
			//perchè sia il prossimo vertice nel cammino deve superare due condizioni
			//1. bob deve essere contenuto nella soluzione parziale 
			if (!parziale.contains(prossimo)) {
				//2. ultimo e prossimo devono essere consecutivi (controllo gli indici di questi due vertici
				//all'interno della lista ordinata stagioniConsecutive e controllo che siano consecutivi)
				if (stagioniConsecutive.indexOf(ultimo) + 1 == stagioniConsecutive.lastIndexOf(prossimo)) {
					// candidato accettabile -> fai ricorsione
					trovato = true;
					parziale.add(prossimo);
					cerca(livello + 1, parziale);
					parziale.remove(livello); //rimetto a posto la coda
				}
			}
		}
		// valuta caso terminale
		if (!trovato) { // caso in cui non abbia trovato alcun candidato possibile
			if (parziale.size() > percorsoBest.size()) {
				// nel caso terminale bisogna sempre clonare la struttura dati che in questo momento sta vincendo
				// altriemnti nel passo successivo si andrebbe a modificare essendo un metodo ricorsivo
				percorsoBest = new ArrayList<Season>(parziale); 
			}
		}
	}
			
}
