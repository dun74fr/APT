package fr.areastudio.jwterritorio;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import fr.areastudio.jwterritorio.common.UUIDGenerator;
import fr.areastudio.jwterritorio.model.News;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        News news1 = new News();
        news1.date = new Date();
        news1.title = "Title news";
        news1.content = "<p>news content message very important</p>";
        news1.alert = true;
        news1.uuid = UUIDGenerator.uuidToBase64();

        News news2 = new News();
        news2.date = new Date();
        news2.title = "Title news 2";
        news2.content = "<p>news content message very important 2</p>";
        news2.alert = false;
        news2.uuid = UUIDGenerator.uuidToBase64();
        ArrayList<News> newsList = new ArrayList<>();
        newsList.add(news1);
        newsList.add(news2);

        Gson gson = new Gson();
        System.out.println(gson.toJson(newsList));
    }
}