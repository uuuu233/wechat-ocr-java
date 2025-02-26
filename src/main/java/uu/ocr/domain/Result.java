package uu.ocr.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Result {
    private boolean success;
    private String msg;
    @JsonIgnore
    private Exception exception;
    private double width;
    private double height;
    @JsonAlias("ocr_response")
    private List<Item> data = new ArrayList<>();
    @JsonIgnore
    private String text;

    public String text() {
        if (text == null) {
            StringBuilder sb = new StringBuilder();
            int end = data.size() - 1;
            for (int i = 0; i < data.size(); i++) {
                sb.append(data.get(i).getText());
                if (i < end) {
                    sb.append("\n");
                }
            }
            text = sb.toString();
        }
        return text;
    }
}
