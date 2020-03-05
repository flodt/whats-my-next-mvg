package de.schmidt.mvg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Interruption {
	private final long id;
	private final String title;
	private final List<InterruptionLine> lines;
	private final LocalDateTime from;
	private final LocalDateTime until;
	private final String durationAsText;
	private final String descriptionText;
	private final LocalDateTime modificationDate;

	private Interruption(long id, String title, List<InterruptionLine> lines, LocalDateTime from, LocalDateTime until, String durationAsText, String descriptionText, LocalDateTime modificationDate) {
		this.id = id;
		this.title = title;
		this.lines = lines;
		this.from = from;
		this.until = until;
		this.durationAsText = durationAsText;
		this.descriptionText = descriptionText;
		this.modificationDate = modificationDate;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public List<InterruptionLine> getLines() {
		return lines;
	}

	public LocalDateTime getFrom() {
		return from;
	}

	public LocalDateTime getUntil() {
		return until;
	}

	public String getDurationAsText() {
		return durationAsText;
	}

	public String getDescriptionText() {
		return descriptionText;
	}

	public LocalDateTime getModificationDate() {
		return modificationDate;
	}

	public static Interruption ofJSON(JSONObject json) throws JSONException {
		//collect interruption lines
		List<InterruptionLine> lines = new ArrayList<>();
		JSONArray rawLines = json.getJSONObject("lines").getJSONArray("line");
		for (int i = 0; i < rawLines.length(); i++) {
			JSONObject line = rawLines.getJSONObject(i);
			lines.add(InterruptionLine.ofJSON(line));
		}
		JSONObject duration = json.getJSONObject("duration");

		return new Interruption(
				json.getLong("id"),
				json.getString("title"),
				lines,
				LocalDateTime.ofInstant(new Date(duration.getLong("from")).toInstant(), ZoneId.systemDefault()),
				LocalDateTime.ofInstant(new Date(duration.getLong("until")).toInstant(), ZoneId.systemDefault()),
				duration.getString("text"),
				json.getString("text"),
				LocalDateTime.ofInstant(new Date(json.getLong("modificationDate")).toInstant(), ZoneId.systemDefault())
		);
	}

	@Override
	public String toString() {
		return "Interruption{" +
				"id=" + id +
				", title='" + title + '\'' +
				", lines=" + lines +
				", from=" + from +
				", until=" + until +
				", durationAsText='" + durationAsText + '\'' +
				", descriptionText='" + descriptionText + '\'' +
				", modificationDate=" + modificationDate +
				'}';
	}
}
