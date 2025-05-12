package uu.ocr;

import java.nio.file.Path;

public class Test02 {
    public static void main(String[] args) {
        String tmp = Path.of("tmp").normalize().toAbsolutePath().toString();
        System.out.println(tmp);

    }
}
