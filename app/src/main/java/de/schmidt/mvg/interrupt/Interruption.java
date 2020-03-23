package de.schmidt.mvg.interrupt;

import android.content.Context;
import de.schmidt.whatsnext.R;
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
	private final Date from;
	private final Date until;
	private final String durationAsText;
	private final String descriptionText;
	private final Date modificationDate;

	private Interruption(long id, String title, List<InterruptionLine> lines, Date from, Date until, String durationAsText, String descriptionText, Date modificationDate) {
		this.id = id;
		this.title = title;
		this.lines = lines;
		this.from = from;
		this.until = until;
		this.durationAsText = durationAsText;
		//interruption description is given as html style text, we need to replace all line breaks
		this.descriptionText = descriptionText.replaceAll("<br />", "\n");
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

	public String getLinesAsString() {
		StringBuilder sb = new StringBuilder(lines.get(0).getLine());
		for (int i = 1; i < lines.size(); i++) {
			sb.append(", ").append(lines.get(i).getLine());
		}
		return sb.toString();
	}

	public String getLinesAsHtmlColoredString() {
		StringBuilder sb = new StringBuilder(lines.get(0).getHtmlColoredLine());
		for (int i = 1; i < lines.size(); i++) {
			sb.append(", ").append(lines.get(i).getHtmlColoredLine());
		}
		return sb.toString();
	}

	public Date getFrom() {
		return from;
	}

	public Date getUntil() {
		return until;
	}

	public String getDurationAsText() {
		return durationAsText;
	}

	public String getDescriptionText() {
		return descriptionText;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public String getModificationDateAsString(Context context) {
		return context.getResources().getString(R.string.modification_date_prefix) + modificationDate.toString();
	}

	public static Interruption ofJSON(JSONObject json) throws JSONException {
		//collect interruption lines
		List<InterruptionLine> lines = new ArrayList<>();
		JSONArray rawLines = json.getJSONObject("lines").getJSONArray("line");
		for (int i = 0; i < rawLines.length(); i++) {
			JSONObject line = rawLines.getJSONObject(i);
			lines.add(InterruptionLine.ofJSON(line));
		}

		//get duration object
		JSONObject duration = json.getJSONObject("duration");

		//construct the interruption object
		return new Interruption(
				json.getLong("id"),
				json.getString("title"),
				lines,
				new Date(duration.getLong("from")),
				new Date(duration.getLong("until")),
				duration.getString("text"),
				json.getString("text"),
				new Date(json.getLong("modificationDate"))
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
