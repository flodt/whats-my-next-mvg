package de.schmidt.util.caching;

import de.schmidt.mvg.interrupt.Interruption;

import java.util.Collections;
import java.util.List;

public class InterruptionsCache {
	private static final InterruptionsCache instance = new InterruptionsCache();
	private List<Interruption> interruptions;

	private InterruptionsCache() {
		interruptions = Collections.emptyList();
	}

	public static InterruptionsCache getInstance() {
		return instance;
	}

	public List<Interruption> getCache() {
		return interruptions;
	}

	public void setCache(List<Interruption> interruptions) {
		this.interruptions = interruptions;
	}
}
