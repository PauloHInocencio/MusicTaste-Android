package com.poolapps.musictaste;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;




public class MatchMusicsFragment extends Fragment {
    private static final String TAG = ItunesFetchr.class.getSimpleName();

    //private RecyclerView mRecyclerView;


    private List<MusicItem> mMusicItems;
    private CardView mMusicInfoCardView;
    private LinearLayout mDefaultMessageContainer;
    private ProgressBar mSpinner;

    private TextView mArtistName;
    private TextView mMusicName;
    private TextView mAlbumName;

    public static MatchMusicsFragment newInstance() {
        return new MatchMusicsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.match_fragment, container, false);

        /*
        mRecyclerView = (RecyclerView) v.findViewById(R.id.products_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(new MusicAdapter(new ArrayList<MusicItem>()));
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

       */

        mMusicInfoCardView = (CardView) v.findViewById(R.id.music_information_cardview);
        mDefaultMessageContainer = (LinearLayout) v.findViewById(R.id.default_message_container);
        mSpinner = (ProgressBar) v.findViewById(R.id.progressbar);
        mSpinner.setVisibility(View.GONE);

        mMusicName = (TextView) v.findViewById(R.id.music_name_text);
        mArtistName = (TextView) v.findViewById(R.id.artist_name_text);
        mAlbumName = (TextView) v.findViewById(R.id.album_name_text);

        ImageButton likeButton = (ImageButton) v.findViewById(R.id.like_button);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextMusic();
            }
        });


        ImageButton dislikeButton = (ImageButton) v.findViewById(R.id.dislike_button);
        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextMusic();
            }
        });


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkInternetConnectionBeforeSearch();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MusicTastePreferences.setSearchQuery(getContext(), query);
                checkInternetConnectionBeforeSearch();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = MusicTastePreferences.getSearchQuery(getContext());
                searchView.setQuery(query, false);
            }
        });



    }

    private void updateSearch(){
        String query = MusicTastePreferences.getSearchQuery(getContext());
        if (query != null) {
            showSpinner();
            new FetchMusicsTask(query).execute();
        } else {
            hideCardView("Pesquise por alguma música.");
        }
    }

    private void showNextMusic() {
        if (mMusicItems.size() > 0) {
            showCardView();
            MusicItem item = mMusicItems.remove(0);
            mMusicName.setText(item.musicName);
            mArtistName.setText(item.artistName);
            mAlbumName.setText(item.albumName);
        } else {
            checkInternetConnectionBeforeSearch();
        }
    }


    private void checkInternetConnectionBeforeSearch() {
        boolean isConnected = Utilities.checkInternetConnection(getContext());
        if (isConnected) {
            updateSearch();
        } else {
            hideCardView("Verifique sua conexão com a internet.");
        }
    }

    private void hideCardView(String message) {
        mMusicInfoCardView.setVisibility(View.GONE);
        mDefaultMessageContainer.setVisibility(View.VISIBLE);
        TextView mMessageTV = (TextView) mDefaultMessageContainer.findViewById(R.id.default_message_text);
        mMessageTV.setText(message);
    }

    private void showCardView() {
        mMusicInfoCardView.setVisibility(View.VISIBLE);
        mDefaultMessageContainer.setVisibility(View.GONE);
    }

    private void showSpinner() {
        mSpinner.setVisibility(View.VISIBLE);
        mMusicInfoCardView.setVisibility(View.GONE);
        mDefaultMessageContainer.setVisibility(View.GONE);
    }

    private void hideSpinner() {
        mSpinner.setVisibility(View.GONE);
    }


    /*private class MusicAdapter extends RecyclerView.Adapter<MusicHolder> {

        private List<MusicItem> items = new ArrayList<>();

        MusicAdapter(List<MusicItem> musicItems) {
            items = musicItems;
        }

        @Override
        public MusicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater li = LayoutInflater.from(getActivity());
            View v = li.inflate(R.layout.match_fragment_list_item, parent, false);
            return new MusicHolder(v);
        }

        @Override
        public void onBindViewHolder(MusicHolder holder, int position) {
            holder.bindItem(items.get(position));
        }

        @Override
        public int getItemCount() {
            return  items.size();
        }


        void swapList(List<MusicItem> musicItems) {
            items = musicItems;
            notifyDataSetChanged();
        }
    }


    private class MusicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MusicItem mItem;
        private TextView mMusicName;
        private TextView mArtistName;

        MusicHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mMusicName = (TextView) v.findViewById(R.id.music_name);
            mArtistName = (TextView) v.findViewById(R.id.artist_name);
        }

        void bindItem(MusicItem item) {
            mItem = item;
            mMusicName.setText(item.name);
            mArtistName.setText(item.artistName);
        }

        @Override
        public void onClick(View view) {
            //Intent intent = ProductDetailActivity.newIntent(getContext(), mProduct.id, getAdapterPosition());
            //startActivityForResult(intent, 0);
        }
    } */



    private class FetchMusicsTask extends AsyncTask<Void, Void, List<MusicItem>> {

        String mMusicName;

        FetchMusicsTask(String musicName) {
            mMusicName = musicName;
        }

        @Override
        protected List<MusicItem> doInBackground(Void... voids) {
            return new ItunesFetchr().searchMusic(mMusicName);
        }

        @Override
        protected void onPostExecute(List<MusicItem> musicItems) {
            mMusicItems = musicItems;
            hideSpinner();
            showNextMusic();
            //((MusicAdapter) mRecyclerView.getAdapter()).swapList(musicItems);
        }
    }


}
