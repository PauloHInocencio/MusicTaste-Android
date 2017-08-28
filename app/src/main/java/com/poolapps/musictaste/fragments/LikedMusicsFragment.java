package com.poolapps.musictaste.fragments;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.poolapps.musictaste.R;
import com.poolapps.musictaste.database.MusicContract;
import com.poolapps.musictaste.model.MusicItem;
import com.poolapps.musictaste.server.ImageLoader;

import java.util.ArrayList;
import java.util.List;




public class LikedMusicsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_MUSIC_ITEMS = 0;

    private RecyclerView mRecyclerView;
    private LinearLayout mDefaultMessageContainer;
    private ProgressBar mSpinner;

    private ImageLoader<MusicHolder> mImageLoader;

    public static LikedMusicsFragment newInstance() {
        return new LikedMusicsFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Handler responseHandler = new Handler();
        mImageLoader = new ImageLoader<>(responseHandler);
        mImageLoader.setImageLoaderListener(new ImageLoader.ImageLoaderListener<MusicHolder>() {


            @Override
            public void imageHasBeenDownloaded(MusicHolder targetWaitingForBitmap, Bitmap bitmapDownloaded) {
                if(targetWaitingForBitmap != null) {
                    targetWaitingForBitmap.mAlbumImage.setImageBitmap(bitmapDownloaded);
                }
            }

            @Override
            public void imageWasNotDownloaded(MusicHolder targetWaitingForBitmap, String errorMessage) {

            }
        });
        mImageLoader.start();
        mImageLoader.getLooper();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.liked_fragment, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.liked_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(new MusicAdapter(new ArrayList<MusicItem>()));
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        mDefaultMessageContainer = (LinearLayout) v.findViewById(R.id.default_message_container);
        mSpinner = (ProgressBar) v.findViewById(R.id.circular_progress_bar);
        mSpinner.setVisibility(View.GONE);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        showSpinner();
        getActivity().getSupportLoaderManager().restartLoader(LOADER_MUSIC_ITEMS, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Loader<Cursor> loader = null;

        switch (id){

            case LOADER_MUSIC_ITEMS:
                String selection = MusicContract.Music.COLUMN_IS_ON_LIKED + " = ? ";
                String[] selectionArg = new String[]{"1"};
                loader = new CursorLoader(getContext(),
                        MusicContract.Music.CONTENT_URI,
                        null,
                        selection,
                        selectionArg,
                        null);
                break;
        }


        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<MusicItem> items = new ArrayList<>();
        hideSpinner();
        if (cursor != null){
            while (cursor.moveToNext()){
                MusicItem item = MusicItem.create(cursor);
                items.add(item);
            }
            cursor.close();
        }

        if (items.size() > 0){
            ((MusicAdapter) mRecyclerView.getAdapter()).swapList(items);
            showRecycler();
        } else {
            hideRecycler();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((MusicAdapter) mRecyclerView.getAdapter()).swapList(null);
    }


    private void hideRecycler() {
        mRecyclerView.setVisibility(View.GONE);
        mDefaultMessageContainer.setVisibility(View.VISIBLE);
    }

    private void showRecycler() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mDefaultMessageContainer.setVisibility(View.GONE);
    }

    private void showSpinner() {
        mSpinner.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mDefaultMessageContainer.setVisibility(View.GONE);
    }

    private void hideSpinner() {
        mSpinner.setVisibility(View.GONE);
    }

    private class MusicAdapter extends RecyclerView.Adapter<MusicHolder> {

        private List<MusicItem> items = new ArrayList<>();

        MusicAdapter(List<MusicItem> musicItems) {
            items = musicItems;
        }

        @Override
        public MusicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater li = LayoutInflater.from(getActivity());
            View v = li.inflate(R.layout.liked_fragment_list_item, parent, false);
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
        private TextView mAlbumName;
        private ImageView mAlbumImage;

        MusicHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mMusicName = (TextView) v.findViewById(R.id.music_name_text);
            mArtistName = (TextView) v.findViewById(R.id.artist_name_text);
            mAlbumName = (TextView) v.findViewById(R.id.album_name_text);
            mAlbumImage = (ImageView) v.findViewById(R.id.album_image);

        }

        void bindItem(MusicItem item) {
            mItem = item;
            mMusicName.setText(mItem.musicName);
            mArtistName.setText(mItem.artistName);
            mAlbumName.setText(mItem.albumName);
            mAlbumImage.setImageResource(R.drawable.placeholder);
            mImageLoader.getBitmapFromPhone(this, mItem.albumImageUri);
        }

        @Override
        public void onClick(View view) {
            //TODO: create detail view for items.
            //Intent intent = MusicDetailActivity.newIntent(getContext(), mItem.id, getAdapterPosition());
            //startActivityForResult(intent, 0);
        }
    }

}
