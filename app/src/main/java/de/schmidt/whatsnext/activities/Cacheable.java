package de.schmidt.whatsnext.activities;

public interface Cacheable {
	default void updateFromCache() {}
}
