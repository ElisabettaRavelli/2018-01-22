package it.polito.tdp.seriea.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {
	
	private SerieADAO dao; 
	private List<Team> squadre;
	private List<Season> stagioni;
	
	private Team squadraSelezionata;
	private Map<Season, Integer> punteggi;
	
	private Map<Integer, Season> stagioniIdMap; 
	private Map<String, Team> squadreIdMap; 
	
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

}
