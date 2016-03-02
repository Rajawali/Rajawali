package org.rajawali3d.examples.examples.about;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.examples.examples.about.CommunityFeedFragment.Activity.FeedItem;
import org.rajawali3d.examples.examples.about.dialogs.ExceptionDialog;
import org.rajawali3d.examples.views.GithubLogoView;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommunityFeedFragment extends AExampleFragment implements
		OnItemClickListener {

	private static final String BUNDLE_QUERY_RESPONSE = "CommunityFeedFragment.BUNDLE_QUERY_RESPONSE";
	private static final String RAJAWALI_COMMUNITY_STREAM_URL = "https://www.googleapis.com/plus/v1/people/116529974266844528013/activities/public?fields=items(actor(displayName%2Cimage)%2Cpublished%2Ctitle%2Curl)&key=";
	private static final String TAG = "RajawaliCommunityStream";

	private final ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();

	private GridView mGridView;
	private CommunityAdapter mCommunityAdapter;
	private String mQueryResponse;
	private String mQueryURL;

	@Override
	public AExampleRenderer createRenderer() {
		return null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Look for the classing containing the API key
		mQueryURL = RAJAWALI_COMMUNITY_STREAM_URL;
		try {
			Class<?> mApiClass = Class.forName("dennis.ApiKey");
			Object obj = mApiClass.newInstance();
			try {
				Method[] methods = obj.getClass().getDeclaredMethods();
				for (int i = 0; i < methods.length; i++) {
					if (methods[i].getName().equals("getKey")) {
						mQueryURL += (String) methods[i].invoke(null,
								(Object[]) null);
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Enable logging in ION
		Ion.getDefault(getActivity())
                .configure()
                .setLogging(TAG, Log.DEBUG);

		// TODO ION should cache the JSON responses, likely for about an hour or so. No real reason
		// to pull the feed more than that.
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mLayout = (FrameLayout) inflater.inflate(R.layout.community_activity,
												 null, false);

		mLayout.findViewById(R.id.relative_layout_loader_container)
				.bringToFront();

		// Create the loader
		mProgressBarLoader = (ProgressBar) mLayout
				.findViewById(R.id.progress_bar_loader);

		mGridView = (GridView) mLayout.findViewById(R.id.base_gridview);
		mCommunityAdapter = new CommunityAdapter(getActivity()
				.getApplicationContext(), feedItems);

		mGridView.setAdapter(mCommunityAdapter);
		mGridView.setOnItemClickListener(this);

		mImageViewExampleLink = (GithubLogoView) mLayout.findViewById(R.id.image_view_example_link);
		mImageViewExampleLink.setVisibility(View.GONE);

		// Avoid the network call if the a response is cached.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(BUNDLE_QUERY_RESPONSE)) {
			try {
				JsonObject jsonObject = (JsonObject) new JsonParser()
						.parse(savedInstanceState
								.getString(BUNDLE_QUERY_RESPONSE));
				updateActivityData(jsonObject);
				hideLoader();
				return mLayout;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// No cached data, request a feed from the network
        Ion.with(getActivity())
                .load(mQueryURL)
                .asJsonObject()
				.setCallback(new FutureCallback<JsonObject>() {
					@Override
					public void onCompleted(Exception e, JsonObject jsonObject) {
						hideLoader();

						// Handle errors and return
						if (e != null) {
							e.printStackTrace();
							showExceptionDialog(
									"Network Error",
									"An error occured reading the Google+ Rajawali Community Stream. Please check your network settings.");
							return;
						}

						updateActivityData(jsonObject);
					}
				});

		return mLayout;
	}

	private void updateActivityData(JsonObject jsonObject) {
		feedItems.clear();

		// Map the result
		Activity activity = new Gson()
				.fromJson(jsonObject, Activity.class);

		// Check for results
		if (activity.items == null) {
			showExceptionDialog("API Error",
					"The Google+ API returned an unexpected result, please try again later.");
			return;
		}

		// Save the results
		mQueryResponse = jsonObject.toString();

		// Add the feed items to the feed items list
		for (int i = 0, j = activity.items.length; i < j; ++i)
			feedItems.add(activity.items[i]);

		// Notify the adapter
		mCommunityAdapter.notifyDataSetChanged();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mQueryResponse != null)
			outState.putString(BUNDLE_QUERY_RESPONSE, mQueryResponse);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Cancel any currently running ION tasks
		Ion.getDefault(getActivity()).cancelAll();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(mCommunityAdapter.getItem(position).url));
		startActivity(intent);
	}

	private void showExceptionDialog(String title, String message) {
		FragmentManager fragmentManager = getActivity().getFragmentManager();
		Fragment existingDialog = fragmentManager
				.findFragmentByTag(ExceptionDialog.TAG);

		if (existingDialog != null)
			fragmentManager.beginTransaction().remove(existingDialog).commit();

		Bundle bundle = new Bundle();
		bundle.putString(ExceptionDialog.BUNDLE_KEY_TITLE, title);
		bundle.putString(ExceptionDialog.BUNDLE_KEY_MESSAGE, message);

		ExceptionDialog exceptionDialog = new ExceptionDialog();
		exceptionDialog.setArguments(bundle);

		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(exceptionDialog, ExceptionDialog.TAG);
		ft.commit();
	}

	private static final class CommunityAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;
		private final List<FeedItem> mFeedItems;
		private final DateFormat mDateFormatter;

		public CommunityAdapter(Context context, List<FeedItem> feedItems) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mFeedItems = feedItems;
			mDateFormatter = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
			mDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		@Override
		public int getCount() {
			return mFeedItems.size();
		}

		@Override
		public FeedItem getItem(int position) {
			return mFeedItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			final FeedItem feedItem = mFeedItems.get(position);
			final ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.community_activity_item, null, false);
				convertView.setId(View.NO_ID);
				holder = new ViewHolder(convertView);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.textViewUserName.setText(feedItem.actor.displayName);
			holder.textViewUserPost.setText(feedItem.title);

			// Format the date (ugh)
			try {
				GregorianCalendar cal = new GregorianCalendar(
						TimeZone.getTimeZone("UTC"));
				cal.setTime(mDateFormatter.parse(feedItem.published));
				holder.textViewUserPostDate.setText(cal.getTime().toString());
			} catch (Exception e) {
				e.printStackTrace();
				holder.textViewUserPostDate.setText("N/A");
			}

			// Cancel the Ion task if running and not already for this image
			if (convertView.getId() == position)
				return convertView;

			Future<?> futureTask = ((Future<?>) holder.imageViewUserImage
					.getTag());
			if (futureTask != null)
				futureTask.cancel();

			// Load the image
			futureTask = Ion.with(holder.imageViewUserImage)
					.placeholder(R.drawable.photo_rajawali3dcommunity)
					.animateIn(android.R.anim.fade_in)
					.load(feedItem.actor.image.url);
			holder.imageViewUserImage.setTag(futureTask);

			convertView.setId(position);
			return convertView;
		}

	}

	private static final class ViewHolder {
		public ImageView imageViewUserImage;
		public TextView textViewUserName;
		public TextView textViewUserPostDate;
		public TextView textViewUserPost;

		public ViewHolder(View convertView) {
			imageViewUserImage = (ImageView) convertView
					.findViewById(R.id.imageview_user_image);
			textViewUserName = (TextView) convertView
					.findViewById(R.id.textview_user_name);
			textViewUserPost = (TextView) convertView
					.findViewById(R.id.textview_user_post);
			textViewUserPostDate = (TextView) convertView
					.findViewById(R.id.textview_user_post_date);
			convertView.setTag(this);
		}
	}

	public static final class Activity {

		public FeedItem[] items;

		public static final class FeedItem {
			public String title;
			public String published;
			public String url;
			public Actor actor;

			public static final class Actor {
				public String displayName;
				public Image image;

				public static final class Image {
					public String url;
				}
			}
		}
	}

}
