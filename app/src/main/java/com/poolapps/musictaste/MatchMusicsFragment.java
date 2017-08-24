package com.poolapps.musictaste;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;




public class MatchMusicsFragment extends Fragment {
    private static final String TAG = ItunesFetchr.class.getSimpleName();

    private RecyclerView mRecyclerView;

    public static MatchMusicsFragment newInstance() {
        return new MatchMusicsFragment();
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.match_fragment, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.products_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(new MusicAdapter(new ArrayList<MusicItem>()));
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkInternetConnection();
    }

    private void checkInternetConnection() {
        boolean isConnected = Utilities.checkInternetConnection(getContext());
        if (isConnected) {
            new FetchMusicsTask("californication").execute();
            //Toast.makeText(getContext(), "Está conectado", Toast.LENGTH_SHORT).show();

        } else {
           // Toast.makeText(getContext(), "Não está conectado", Toast.LENGTH_SHORT).show();
        }
    }




    private class MusicAdapter extends RecyclerView.Adapter<MusicHolder> {

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
            ((MusicAdapter) mRecyclerView.getAdapter()).swapList(musicItems);
        }
    }


}
