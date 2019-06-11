package it.polito.tdp.poweroutages.model;

public class Nerc {
	private int id;
	private String value;
	//Aggiungiamo questo booleano che ci permette di sapere se il nerc sta prestando energia o no
	private boolean staPrestando;

	public Nerc(int id, String value) {
		this.id = id;
		this.value = value;
		this.staPrestando=false;//di default non sta prestandp
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isStaPrestando() {
		return staPrestando;
	}

	public void setStaPrestando(boolean staPrestando) {
		this.staPrestando = staPrestando;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nerc other = (Nerc) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(value);
		return builder.toString();
	}
}
