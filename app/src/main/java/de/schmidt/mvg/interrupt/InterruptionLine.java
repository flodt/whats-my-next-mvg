package de.schmidt.mvg.interrupt;

import org.json.JSONException;
import org.json.JSONObject;

public class InterruptionLine {
	private final long id;
	private final String product;
	private final String line;

	private InterruptionLine(long id, String product, String line) {
		this.id = id;
		this.product = product;
		this.line = line;
	}

	public static InterruptionLine ofJSON(JSONObject json) throws JSONException {
		return new InterruptionLine(
				json.getLong("id"),
				json.getString("product"),
				json.getString("line")
		);
	}

	public long getId() {
		return id;
	}

	public String getProduct() {
		return product;
	}

	public String getLine() {
		return line;
	}

	@Override
	public String toString() {
		return "InterruptionLine{" +
				"id=" + id +
				", product='" + product + '\'' +
				", line='" + line + '\'' +
				'}';
	}
}
