package uu.ocr;

import uu.ocr.domain.Result;

import java.io.IOException;
import java.nio.file.Path;

public class Test01 {
    public static void main(String[] args) throws IOException {
        WeChatOCR.load("WeChatOCR", "tmp");
        Result result = WeChatOCR.apply(Path.of(System.getProperty("user.dir"), "images", "test.png").toString());
        System.out.println(result.toString());
        System.out.println(result.text());
    }
}
