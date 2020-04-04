package de.schmidt.util.managers;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteConnectionPart;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.util.DrawableUtils;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.activities.RoutingAlternativesActivity;
import de.schmidt.whatsnext.activities.RoutingItineraryDisplayActivity;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class NotificationManager {
	public static final String CHANNEL_ID = "WHATSMYNEXT_ITINERARY";
	private static NotificationManager instance = new NotificationManager();

	private NotificationManager() {
	}

	public static NotificationManager getInstance() {
		return instance;
	}

	/**
	 * Initializes the notification channel (> 8.0).
	 * @param context application context
	 */
	public void createNotificationChannel(Context context) {
		//see documentation: create notification channel above 8.0
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = context.getString(R.string.channel_name);
			String description = context.getString(R.string.channel_description);
			int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
			Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
		}
	}

	/**
	 * Send the itinerary to notifications.
	 * @param connection the route connection in question
	 * @param context context of the call
	 */
	public void sendItinerary(RouteConnection connection, Context context) {
		//create intent with saved route connection
		Intent launchIntent = new Intent(context, RoutingItineraryDisplayActivity.class);
		launchIntent.putExtra(context.getResources().getString(R.string.key_itinerary), connection);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);

		//formatting the info
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		final RouteConnectionPart firstPart = connection.getConnectionParts().get(0);
		final String title = connection.getFrom().getName() + " - " + connection.getTo().getName();
		final String content = dateFormat.format(connection.getDepartureTime())
				+ " via " + firstPart.getLine() + " ▸ " + firstPart.getDirection()
				+ " (" + firstPart.getDeparturePlatform() + ")";
		final int color = connection.getFirstColor();
		final int notificationID = (int) connection.getDepartureTime().getTime();

		//fire notification
		fireNotification(context, pendingIntent, title, content, color, notificationID);
	}

	public void sendAlternatives(RouteOptions options, Context context, String fromTo, int color) {
		//create intent
		Intent launchIntent = new Intent(context.getApplicationContext(), RoutingAlternativesActivity.class);
		launchIntent.putExtra(context.getString(R.string.key_route_options_from_shortcut), options.getParameterString());
		launchIntent.setAction(Intent.ACTION_MAIN);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);

		//content setup
		final int notificationID = options.hashCode();
		final String content = context.getResources().getString(R.string.tap_to_view_details);

		//fire notification
		fireNotification(context, pendingIntent, fromTo, content, color, notificationID);
	}

	private void fireNotification(Context context, PendingIntent pendingIntent, String title, String content, int color, int notificationID) {
		//send the notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_dark_train)
				.setLargeIcon(DrawableUtils.generateBitmap(context, R.mipmap.ic_launcher_round))
				.setColor(color)
				.setContentTitle(title)
				.setContentText(content)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(pendingIntent)
				.setAutoCancel(false);

		//fire notification
		NotificationManagerCompat manager = NotificationManagerCompat.from(context);
		manager.notify(notificationID, builder.build());
	}
}
