package it.polito.tdp.poweroutages.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polito.tdp.poweroutages.model.Nerc;
import it.polito.tdp.poweroutages.model.NercIdMap;

public class PowerOutagesDAO {
	
	public List<Nerc> loadAllNercs(NercIdMap nIdMap) {

		String sql = "SELECT id, value FROM nerc";
		List<Nerc> nercList = new ArrayList<>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				Nerc n = new Nerc(res.getInt("id"), res.getString("value"));
				nercList.add(nIdMap.get(n));
			}

			conn.close();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return nercList;
	}

	public Set<Nerc> getNeighbors(NercIdMap nIdMap, Nerc nerc) {
		String sql = "SELECT nerc_one FROM NercRelations WHERE nerc_two = ?";
		Set<Nerc> neighbors = new HashSet<Nerc>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, nerc.getId());
			ResultSet rs = st.executeQuery();

			while (rs.next()) {				
				Nerc neighbor = nIdMap.get(rs.getInt("nerc_one"));
				if(neighbor == null){
					System.out.println("Errore in getNeighbors!!");
				}else{
					neighbors.add(neighbor);
				}
				
			}

			conn.close();
			return neighbors;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public int getCorrelation(Nerc nerc, Nerc neighbor) {
		String sql = "select distinct year(p1.date_event_began), month(p1.date_event_began) "
				+ "from poweroutages p1, poweroutages p2 "
				+ "where p1.nerc_id=? and p2.nerc_id=? and month(p1.date_event_began)=month(p2.date_event_began) "
				+ "and year(p1.date_event_began)=year(p2.date_event_began)";
		int count = 0;

		try{
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, nerc.getId());
			st.setInt(2, neighbor.getId());
			
			ResultSet rs = st.executeQuery();
			while (rs.next()) {				
				count++;
			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
		return count;
	}

	
}