package org.rajawali3d.examples.examples.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.rajawali3d.examples.ExamplesApplication;
import org.rajawali3d.examples.ExamplesApplication.TeamMember;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MeetTheTeamFragment extends AExampleFragment {

	private GridView mGridView;
	private TeamAdapter mTeamAdapter;

	@Override
    public AExampleRenderer createRenderer() {
		return null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mGridView = (GridView) inflater.inflate(R.layout.base_gridview, null,
												false);
		mTeamAdapter = new TeamAdapter(getActivity().getApplicationContext(),
									   ExamplesApplication.TEAM_MEMBERS);

		mGridView.setAdapter(mTeamAdapter);
		mGridView.setOnItemClickListener(mTeamAdapter);

		return mGridView;
	}

	private static final class TeamAdapter extends BaseAdapter implements
			OnItemClickListener {

		private final ArrayList<TeamMember> mTeamMembers;
		private final Context mContext;
		private final LayoutInflater mInflater;

		public TeamAdapter(Context context, ArrayList<TeamMember> teamMembers) {
			mContext = context;
			mTeamMembers = teamMembers;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mTeamMembers.size();
		}

		@Override
		public TeamMember getItem(int position) {
			return mTeamMembers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			final TeamMember member = getItem(position);
			final ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.meet_the_team_list_item, container, false);
				holder = new ViewHolder(convertView);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String favoriteBeer = member.getFavoriteBeer();
			holder.imageViewPhoto.setImageResource(member.getPhoto());
			holder.textViewFavoriteBeer.setText(favoriteBeer);
			holder.textViewFavoriteBeerTitle.setVisibility(favoriteBeer == null ? GONE : VISIBLE);
			holder.textViewName.setText(member.getName());

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public void onItemClick(AdapterView<?> adapter, View view,
				int position, long itemID) {
			final TeamMember member = getItem(position);
			final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(member.getLink()));

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			mContext.startActivity(intent);
		}

	}

	private static final class ViewHolder {
		public final ImageView imageViewPhoto;
		public final TextView textViewName;
		public final TextView textViewFavoriteBeer;
		public final TextView textViewFavoriteBeerTitle;

		public ViewHolder(View convertView) {
			imageViewPhoto = (ImageView) convertView
					.findViewById(R.id.imageViewPhoto);
			textViewFavoriteBeer = (TextView) convertView
					.findViewById(R.id.textViewFavoriteBeer);
			textViewFavoriteBeerTitle = (TextView) convertView
					.findViewById(R.id.textViewFavoriteBeerTitle);
			textViewName = (TextView) convertView
					.findViewById(R.id.textViewName);
			convertView.setTag(this);
		}
	}

}
