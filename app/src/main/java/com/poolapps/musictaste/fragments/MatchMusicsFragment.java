package com.poolapps.musictaste.fragments;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.poolapps.musictaste.R;
import com.poolapps.musictaste.database.MusicContract;
import com.poolapps.musictaste.database.MusicsController;
import com.poolapps.musictaste.model.MusicItem;
import com.poolapps.musictaste.preferences.MusicTastePreferences;
import com.poolapps.musictaste.server.ImageLoader;
import com.poolapps.musictaste.server.ItunesFetchr;
import com.poolapps.musictaste.utils.Utilities;

import java.util.ArrayList;
import java.util.List;


public class MatchMusicsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ItunesFetchr.class.getSimpleName();

    private static final String STATE_CURRENT_MUSIC_ITEM = "current_item";
    private static final int LOADER_CURRENT_ITEMS_TO_MATCH = 1;

    private ArrayList<MusicItem> mMusicItems;
    private MusicItem mCurrentItem;

    private CardView mMusicInfoCardView;
    private LinearLayout mDefaultMessageContainer;
    private ProgressBar mSpinner;

    private TextView mArtistName;
    private TextView mMusicName;
    private TextView mAlbumName;
    private ImageView mAlbumImage;

    private ImageLoader<ImageView> mImageLoader;

    public static MatchMusicsFragment newInstance() {
        return new MatchMusicsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Handler responseHandler = new Handler();
        mImageLoader = new ImageLoader<>(responseHandler);
        mImageLoader.setImageLoaderListener(new ImageLoader.ImageLoaderListener<ImageView>() {
            @Override
            public void imageHasBeenDownloaded(ImageView targetWaitingForBitmap, Bitmap bitmapDownloaded) {
                if (targetWaitingForBitmap != null) {
                    hideSpinner();
                    showCardView();
                    targetWaitingForBitmap.setImageBitmap(bitmapDownloaded);
                }
            }

            @Override
            public void imageWasNotDownloaded(ImageView targetWaitingForBitmap, String errorMessage) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }

        });
        mImageLoader.start();
        mImageLoader.getLooper();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_CURRENT_MUSIC_ITEM, mCurrentItem);
        super.onSaveInstanceState(outState);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.match_fragment, container, false);

        mMusicInfoCardView = (CardView) v.findViewById(R.id.music_information_cardview);
        mDefaultMessageContainer = (LinearLayout) v.findViewById(R.id.default_message_container);
        mSpinner = (ProgressBar) v.findViewById(R.id.progressbar);
        mSpinner.setVisibility(View.GONE);

        mMusicName = (TextView) v.findViewById(R.id.music_name_text);
        mArtistName = (TextView) v.findViewById(R.id.artist_name_text);
        mAlbumName = (TextView) v.findViewById(R.id.album_name_text);
        mAlbumImage = (ImageView) v.findViewById(R.id.album_image);

        ImageButton likeButton = (ImageButton) v.findViewById(R.id.like_button);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = mImageLoader.saveBitmapOnPhone(mCurrentItem.albumImageUrl);
                if (uri != null) {
                    mCurrentItem.albumImageUri = uri.toString();
                    mCurrentItem.isOnLiked = true;
                    MusicsController.updateMusic(getActivity(), mCurrentItem);
                    showNextMusic();
                }
            }
        });

        ImageButton dislikeButton = (ImageButton) v.findViewById(R.id.dislike_button);
        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicsController.deleteMusic(getActivity(), mCurrentItem);
                showNextMusic();
            }
        });

        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getParcelable(STATE_CURRENT_MUSIC_ITEM);
            fillViews(mCurrentItem);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity()
                .getSupportLoaderManager()
                .restartLoader(LOADER_CURRENT_ITEMS_TO_MATCH, null, this);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageLoader.quit();
        mImageLoader.clearQueueOfWaitingTargets();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;
        switch (id) {
            case LOADER_CURRENT_ITEMS_TO_MATCH:
                String selection = MusicContract.Music.COLUMN_IS_ON_LIKED + " = ? ";
                String[] selectionArgs = new String[]{"0"};
                loader = new CursorLoader(getContext(),
                        MusicContract.Music.CONTENT_URI,
                        null,
                        selection,
                        selectionArgs,
                        null);
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMusicItems= new ArrayList<>();
        hideSpinner();
        if (cursor != null){
            while (cursor.moveToNext()){
                MusicItem item = MusicItem.create(cursor);
                mMusicItems.add(item);
            }
            cursor.close();
        }

        if (mMusicItems.size() > 0){
            mCurrentItem = mMusicItems.remove(0);
            fillViews(mCurrentItem);

            for (MusicItem item : mMusicItems)  {
                mImageLoader.getBitmapFromWeb(item.albumImageUrl);
            }
        } else {
            checkInternetConnectionBeforeSearch();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
            mCurrentItem  = mMusicItems.remove(0);
            fillViews(mCurrentItem);
            showCardView();

        } else {
            mCurrentItem = null;
            hideCardView("Sem resultados para essa pesquisa.");
        }
    }

    private void fillViews(MusicItem item) {
        mMusicName.setText(item.musicName);
        mArtistName.setText(item.artistName);
        mAlbumName.setText(item.albumName);
        mAlbumImage.setImageResource(R.drawable.placeholder);
        mImageLoader.getBitmapFromWeb(mAlbumImage, item.albumImageUrl);
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

            if (musicItems.size() > 0) {
                for (MusicItem item : musicItems)  {
                    MusicsController.insertMusic(getContext(), item);
                    mImageLoader.getBitmapFromWeb(item.albumImageUrl);
                }

                getActivity()
                        .getSupportLoaderManager()
                        .restartLoader(LOADER_CURRENT_ITEMS_TO_MATCH, null, MatchMusicsFragment.this);

            } else {

                hideSpinner();
                hideCardView("Verifique sua conexão com a internet.");

            }
        }
    }


}
