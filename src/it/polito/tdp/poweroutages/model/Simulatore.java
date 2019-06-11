package it.polito.tdp.poweroutages.model;

import it.polito.tdp.poweroutages.model.*;

import java.time.Duration;
import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Simulatore {

	//Stato del sistema
	private Graph <Nerc, DefaultWeightedEdge>grafo;
	private List<PowerOutage> powerOutages;
	//Mappa che mi aiuta a monitorare i prestiti che vanno da un nerc a un set di nerc
	private Map<Nerc, Set<Nerc>>prestiti;
	
	//Parametri della soluzione
	private int k;
	
	//Valori in output
	private int CATASTROFI;
	private Map<Nerc, Long> bonus;
	
	//Coda
	private PriorityQueue<Evento> queue;
	
	
	
	public void init (int k, List<PowerOutage>powerOutages, NercIdMap nercMap, Graph<Nerc, DefaultWeightedEdge>grafo) {
		
		//Conviene inizializzare qui queste cose, perche'  in questo modo posso  usare piu' volte lo stesso simulatore e lui ogni volta distrugge la struttura e la ricrea nuova
		
		this.queue=new PriorityQueue<Evento>();
		this.bonus=new HashMap<Nerc, Long>();
		this.prestiti=new HashMap<Nerc, Set<Nerc>>();
		
		for(Nerc n: nercMap.values()) {
			this.bonus.put(n, Long.valueOf(0));
			this.prestiti.put(n, new HashSet<Nerc>());
		}
		//All'inizio catastrofi=0
		this.CATASTROFI = 0;
		
		//Prendo i parametri e li salvo nei miei attributi interni
		this.k=k;
		this.powerOutages=powerOutages;
		this.grafo=grafo;
	
		//Riempiro' la coda con eventi di tipo INTERRUZIONE perche' allo scatenarsi di ogni evento inizio io devo vedere se c'e' un vicino disponibile
		//se c'e' un vicino che puo' darmi energia:se esiste aggiungo alla coda un evento FINE in cui andro' a premiare chi presta con un bonus
//													se no ci sara'  una catastrofe
	
	
	
	//Inserisco gli eventi iniziali
	for(PowerOutage po: this.powerOutages) {
		Evento e = new Evento(Evento.Tipo.INIZIO_INTERRUZIONE, po.getNerc(), null, po.getInizio(), po.getInizio(), po.getFine());
		queue.add(e);
	}
		
		
	}
	
	public void run() {
		Evento e;
		while((e=queue.poll())!=null){
			switch(e.getTipo()) {
			
			case INIZIO_INTERRUZIONE:
				//Il nerc in cui c'e' l'interruzione lo recupero dall'evento
				Nerc nerc = e.getNerc();
				System.out.println("INIZIO INTERRUZIONE NERC: "+nerc);
				//Cerco se c'e' un donatore, altrimenti catastrofe
				Nerc donatore=null;
				//Cerco prima tra i miei debitori
				if(this.prestiti.get(nerc).size()>0) {
				//scelgo tra i miei debitori
					double min=Long.MAX_VALUE;
					for(Nerc n: this.prestiti.get(nerc)) {
						DefaultWeightedEdge edge = this.grafo.getEdge(nerc, n);
						if(this.grafo.getEdgeWeight(edge)<min) {
							//La prima volta entra sicuramente
							//Salvo il nuovo donatore e il min
							if(!n.isStaPrestando()) {
								donatore=n;
								min=this.grafo.getEdgeWeight(edge);
							}
						}
					}
					//poi  cerco tra i vicini
				}else {
					//se non ce ne sono prendo quello con peso arco mminore
					double min=Long.MAX_VALUE;
					List<Nerc> neighbors=Graphs.neighborListOf(this.grafo, nerc);
					for(Nerc n: neighbors) {
						DefaultWeightedEdge edge = this.grafo.getEdge(nerc, n);
						if(this.grafo.getEdgeWeight(edge)<min) {
							//La prima volta entra sicuramente
							//Salvo il nuovo donatore e il min
							if(!n.isStaPrestando()) {
								donatore=n;
								min=this.grafo.getEdgeWeight(edge);
							}
						}
					}
				}
				//Se non entra in nessuno dei due if, allora CATASTROFE
				if(donatore!=null) {
					System.out.println("\nTROVATO DONATORE: "+donatore);
					//Ho trovato un donatore
					donatore.setStaPrestando(true);
					Evento fine = new Evento(Evento.Tipo.FINE_INTERRUZIONE, e.getNerc(), donatore, e.getDataFine(), e.getDataInizio(), e.getDataFine());
					queue.add(fine);
					this.prestiti.get(donatore).add(e.getNerc());
					Evento cancella = new Evento(Evento.Tipo.CANCELLA_PRESTITO, e.getNerc(), donatore, e.getData().plusMonths(k), e.getDataInizio(), e.getDataFine());
					this.queue.add(cancella);
				}else {
					//Non l'ho trovato quindi CATASTROFE
					this.CATASTROFI++;
				}
				break;
				
			case FINE_INTERRUZIONE:
				System.out.println("\nFINE INTERRUZIONE NERC: "+e.getNerc());
				//Quando l'interruzione e' finita dobbiamo assegnare un bonus al donatore e dire che il  donatore non sta piu' prestando
				//Assegnare un bonus al donatore
				if(e.getDonatore()!=null)
					this.bonus.put(e.getDonatore(), bonus.get(e.getDonatore()) +
					Duration.between(e.getDataInizio(), e.getDataFine()).toDays());
				//Il donatore non sta piu' donando
					e.getDonatore().setStaPrestando(false);
				
				break;
			case CANCELLA_PRESTITO:
				System.out.println("CANCELLAZIONE PRESTITO: "+e.getDonatore()+"-"+e.getNerc());
				this.prestiti.get(e.getDonatore()).remove(e.getNerc());
				break;
			}
		}
	}
}
