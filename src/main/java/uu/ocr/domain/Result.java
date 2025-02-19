package uu.ocr.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Result {
    private boolean success;
    private String msg;
    private Exception exception;
    private String width;
    private String height;
    private String text;
    @JsonAlias("ocr_response")
    private List<Item> data = new ArrayList<>();
}
