package uu.ocr;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class WriteFileList {
    public static void main(String[] args) throws URISyntaxException, IOException {
        Path path = Paths.get(WriteFileList.class.getResource("/WeChatOCR").toURI());
        int prefixLength = path.toString().length() + 1;
        StringBuilder sb = new StringBuilder();
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                sb.append(file.toString().substring(prefixLength).replace("\\", "/"));
                sb.append("\n");
                return FileVisitResult.CONTINUE;
            }
        });
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        FileUtils.write(Path.of("src/main/resources/lib-file-list").toFile(), sb.toString(), StandardCharsets.UTF_8);
    }



}
