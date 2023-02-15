package co.geisyanne.netflixclone.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import co.geisyanne.netflixclone.model.Category;
import co.geisyanne.netflixclone.model.Movie;

public class CategoryTask extends AsyncTask<String, Void, List<Category>> {

    private final WeakReference<Context> context;
    private ProgressDialog dialog;
    private CategoryLoader categoryLoader;

    public CategoryTask(Context context) {   // [1.2] CONSTRUTOR PARA PEGAR REF DO CONTEXTO
        this.context = new WeakReference<>(context);
    }

    public void setCategoyLoader(CategoryLoader categoryLoader) {
        this.categoryLoader = categoryLoader;
    }

    // MAIN-THREAD
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context context = this.context.get();

        if (context != null) {
            dialog = ProgressDialog.show(context, "Carregando", "", true);
        }
    }

    // THREAD-BACKGROUND (EXECUTADO PARALELAMENTE)
    @Override
    protected List<Category> doInBackground(String... params) {
        String url = params[0];

        try {
            URL requestUrl = new URL(url);

            HttpsURLConnection urlConnection = (HttpsURLConnection) requestUrl.openConnection();
            urlConnection.setReadTimeout(2000);
            urlConnection.setConnectTimeout(2000);

            int responseCode = urlConnection.getResponseCode();
            if (responseCode > 400) {  // RESPOSTA ACIMA DE 400 = ALGO ERRADO
                throw new IOException("Error na comunicação do servidor");
            }

            // [2.3] CONVERSÃO DE STREAM PARA STRING
            InputStream inputStream = urlConnection.getInputStream();
            BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());

            String jsonAsString = toString(in);

            List<Category> categories = getCategories(new JSONObject(jsonAsString)); // [2.4] MÉTODO PARA RETORNAR LISTA DE CATEGORIAS // (CONVERTE A STRING NUM JSONOBJECT)
            in.close();

            return categories;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Category> getCategories(JSONObject json) throws JSONException {  // [2.4.1] MÉTODO CONVERSOR JASON > OBJ JAVA
        List<Category> categories = new ArrayList<>();

        JSONArray categoryArray = json.getJSONArray("category");
        for (int i = 0; i < categoryArray.length(); i++) {
            JSONObject category = categoryArray.getJSONObject(i);
            String title = category.getString("title");

            List<Movie> movies = new ArrayList<>();
            JSONArray movieArray = category.getJSONArray("movie");
            for (int j = 0; j < movieArray.length(); j++) {
                JSONObject movie = movieArray.getJSONObject(j);

                String coverUrl = movie.getString("cover_url");
                int id = movie.getInt("id");

                Movie movieObj = new Movie();
                movieObj.setCoverUrl(coverUrl);
                movieObj.setId(id);

                movies.add(movieObj);
            }

            Category categoryObj = new Category();
            categoryObj.setName(title);
            categoryObj.setMovies(movies);

            categories.add(categoryObj);
        }
        return categories;
    }

    // MAIN-THREAD
    @Override
    protected void onPostExecute(List<Category> categories) {
        super.onPostExecute(categories);
        dialog.dismiss();


        if (categoryLoader != null) {
            categoryLoader.onResult(categories);
        }
    }

    private String toString(InputStream is) throws IOException {
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int lidos;
        while ((lidos = is.read(bytes)) > 0) {
            baos.write(bytes, 0, lidos);
        }
        return new String(baos.toByteArray());
    }

    public interface CategoryLoader {
        public void onResult(List<Category> categories);
    }
}
