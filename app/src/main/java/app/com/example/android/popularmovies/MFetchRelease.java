package app.com.example.android.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import java.net.URI;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MFetchRelease extends AsyncTask<String, Void, String[]> {
    private MainActivityFragment mainActivityFragment;
    private final String LOG_TAG = MFetchRelease.class.getSimpleName();
    Call<JsonWork> call;

    public MFetchRelease(MainActivityFragment mainActivityFragment) {
        this.mainActivityFragment = mainActivityFragment;
    }


    @Override
    protected String[] doInBackground(String... params) {
        try {


            String baseUrl = "http://api.themoviedb.org/3/movie/" + params[0] + "/";

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            PopularMovieUrl service = retrofit.create(PopularMovieUrl.class);
            call = service.getUser(BuildConfig.OPEN_MOVIE_DB_API_KEY);
            Response<JsonWork> response = call.execute();
            String[] mStringArray = new String[response.body().getResults().size()];
            for (int i = 0; i < response.body().getResults().size(); i++) {
                String imageUrl = "http://image.tmdb.org/t/p/w500/" + response.body().getResults().get(i).getPosterPath();
                mStringArray[i] = imageUrl;
            }
            return mStringArray;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            call.cancel();
        }
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
        GridView gridView = (GridView) mainActivityFragment.getActivity().findViewById(R.id.grid_view);

        try {

            if (result != null) {
                mainActivityFragment.movieUpdate.clear();
            }

            for (String strs : result) {
                mainActivityFragment.movieUpdate.add(strs);
            }
            mainActivityFragment.imageAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error on post execute", e);

        }
        if (mainActivityFragment.mPosition != GridView.INVALID_POSITION) {
            gridView.smoothScrollToPosition(mainActivityFragment.mPosition);
        }
    }
}