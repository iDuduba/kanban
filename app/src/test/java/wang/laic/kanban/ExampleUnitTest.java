package wang.laic.kanban;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wang.laic.kanban.models.Part;
import wang.laic.kanban.network.message.Question;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    Gson gson = new GsonBuilder()
            .serializeNulls()
            .excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd")
            .setPrettyPrinting()
            .create();

    @Test
    public void addition_isCorrect() throws Exception {
        List<Part> items = new ArrayList<>();

        Part part = new Part();
        part.setModel("asfasdfas");
        part.setOpType(3);
        part.setQuantity(123);

        items.add(part);

        part = new Part();
        part.setModel("23sdfas");
        part.setOpType(1);
        part.setQuantity(10);
        items.add(part);

        Map<String, Object> body = new HashMap<>();
        body.put("customerCode", "kiki");
        body.put("items", items);
        body.put("created", new Date());
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        String json = gson.toJson(msg);
        System.out.println(json);
    }
}