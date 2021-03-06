package app.com.example.android.popularmovies;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DFetchData extends AsyncTask<String, Void, Void> {

    private DetailActivityFragment detailActivityFragment;
    private final String LOG_TAG = DFetchData.class.getSimpleName();
    public static String original_title;
    public static String plot_synopsis;
    public static String user_rating;
    public static String release_date;
    public static String icon;
    public static String trailer_l;
    public static String userReviewString = "";
    public static byte[] bytes;
    Call<JsonWork> call;
    Call<TrailerFetch> trailerFetchCall;
    Call<UserReview> userReviewCall;
    static String mForecast;

    public DFetchData(DetailActivityFragment detailActivityFragment) {
        this.detailActivityFragment = detailActivityFragment;

    }


    @Override
    protected Void doInBackground(String... params) {
        try {
            String baseUrl = "http://api.themoviedb.org/3/movie/" + params[0] + "/";

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            PopularMovieUrl service = retrofit.create(PopularMovieUrl.class);

            call = service.getUser(BuildConfig.OPEN_MOVIE_DB_API_KEY);
            Response<JsonWork> response = call.execute();
            for (int i = 0; i < response.body().getResults().size(); i++) {
                if (detailActivityFragment.img_string.contains(response.body().getResults().get(i).getPosterPath())) {
                    original_title = response.body().getResults().get(i).getOriginalTitle();
                    plot_synopsis = response.body().getResults().get(i).getOverview();
                    String user_rat = response.body().getResults().get(i).getVoteAverage();
                    String trailer_id = String.valueOf(response.body().getResults().get(i).getId());
                    String trailer_baseUrl = "http://api.themoviedb.org/3/movie/" + trailer_id + "/";
                    Retrofit trailer_retrofit = new Retrofit.Builder()
                            .baseUrl(trailer_baseUrl)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    PopularMovieUrl_Trailer trailer_service = trailer_retrofit.create(PopularMovieUrl_Trailer.class);
                    UserReviewUrl user_review_url = trailer_retrofit.create(UserReviewUrl.class);

                    trailerFetchCall = trailer_service.getUser(BuildConfig.OPEN_MOVIE_DB_API_KEY);
                    userReviewCall = user_review_url.getUser(BuildConfig.OPEN_MOVIE_DB_API_KEY);

                    Response<TrailerFetch> trailer_fetch = trailerFetchCall.execute();
                    Response<UserReview> userReviewResponse = userReviewCall.execute();

                    trailer_l = trailer_fetch.body().getResults().get(0).getKey();
                    for (int content = 0; content < userReviewResponse.body().getResults().size(); content++) {
                        userReviewString += "* " + userReviewResponse.body().getResults().get(content).getContent() + "\n" + "\n";
                    }
                    user_rating = user_rat;
                    release_date = response.body().getResults().get(i).getReleaseDate();
                    icon = response.body().getResults().get(i).getBackdropPath();
                    URL url = new URL("http://image.tmdb.org/t/p/w500/" + icon);
                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 0, stream);
                    bytes = stream.toByteArray();
                    break;
                }

            }
            return null;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);

        } finally {

            call.cancel();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {

            Config.YOUTUBE_VIDEO_CODE = trailer_l;
            detailActivityFragment.showYouTubeImageButton();
            detailActivityFragment.original_t.setText(original_title);
            detailActivityFragment.plot_s.setText(plot_synopsis);
            detailActivityFragment.user_r.setText(user_rating);
            detailActivityFragment.release_d.setText(release_date);
            detailActivityFragment.user_review.setText(userReviewString);
            detailActivityFragment.user_review_title.setText("Review");
            mForecast = String.format("%s - %s", original_title, trailer_l);

            Picasso.with(detailActivityFragment.getContext())
                    .load("http://image.tmdb.org/t/p/w185/" + icon)
                    .into(detailActivityFragment.movie_p);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error on post execute", e);
        }
    }
}
