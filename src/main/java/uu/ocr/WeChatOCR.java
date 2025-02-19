package uu.ocr;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.File;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.io.FileUtils;
import uu.ocr.domain.Item;
import uu.ocr.domain.Result;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class WeChatOCR {
    private static String mmmojoDirectory;
    private static String wxocrDll;
    private static String tempDirectory;
    private static WeChatOCRLibrary dll;
    private static OkHttpClient okhttp = new OkHttpClient();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static boolean loaded = false;

    @SneakyThrows
    public static Result apply(String path) {
        try {
            if (path.toLowerCase().startsWith("http")) {
                return apply(okhttp.newCall(new Request.Builder().get().url(path).build()).execute().body().bytes());
            }
            AtomicReference<String> reference = new AtomicReference<>();
            dll.wechat_ocr(new WString(wxocrDll), new WString(mmmojoDirectory), path, reference::set);
            JsonNode jsonNode = objectMapper.readTree(reference.get());
            Result result = objectMapper.treeToValue(jsonNode, Result.class);
            result.setSuccess(jsonNode.path("errcode").asInt(1) == 0);
            StringBuilder sb = new StringBuilder();
            List<Item> data = result.getData();
            int end = data.size() - 1;
            for (int i = 0; i < data.size(); i++) {
                sb.append(data.get(i).getText());
                if (i < end) {
                    sb.append(" ");
                }
            }
            result.setText(sb.toString());
            return result;
        } catch (Exception e) {
            Result result = new Result();
            result.setSuccess(false);
            result.setMsg(e.getMessage());
            result.setException(e);
            return result;
        }
    }

    @SneakyThrows
    public static Result apply(byte[] bytes) {
        String uuid = UUID.randomUUID().toString();
        Path path = Path.of(tempDirectory, uuid);
        try {
            FileUtils.writeByteArrayToFile(path.toFile(), bytes);
            return apply(path.toString());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    public static String tempDirectory() {
        return tempDirectory;
    }

    public static void load(String tempDir) throws IOException, URISyntaxException {
        if (loaded) {
            return;
        }
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES , false);
        tempDirectory = tempDir;
        File tempFile = new File(tempDir);
        if (tempFile.isDirectory()) {
            FileUtils.deleteDirectory(tempFile);
        }
        tempFile.mkdirs();
        File libsDir = Path.of(System.getenv("APPDATA"), "WeChatOCR", "libs").toFile();
        libsDir.mkdirs();

        File versionFile = new File(libsDir, "version.txt");
        if (!versionFile.isFile() || !"1".equals(new String(Files.readAllBytes(versionFile.toPath())))) {
            Path libsPath = Paths.get(WeChatOCR.class.getResource("/libs/").toURI());
            copyResourceFiles(libsPath, libsPath.toString().length(), libsDir);
        }

        mmmojoDirectory = Path.of(libsDir.getPath(), "mmmojo").toString();
        wxocrDll = Path.of(libsDir.getPath(), "wxocr.dll").toString();

        dll = Native.load(Path.of(libsDir.getPath(), "wcocr.dll").toString(), WeChatOCRLibrary.class, Collections.singletonMap(Library.OPTION_STRING_ENCODING, "UTF-8"));
        Runtime.getRuntime().addShutdownHook(new Thread(dll::stop_ocr));
        loaded = true;
    }

    public static void load() throws IOException, URISyntaxException {
        load(Path.of(System.getenv("APPDATA"), "WeChatOCR", "temp").toFile().getCanonicalPath());
    }

    private static void copyResourceFiles(Path path, int deletePrefixLength, File libsDir) throws IOException {
        String subpath = path.toString().substring(deletePrefixLength);
        if (Files.isDirectory(path)) {
            // 新建文件夹
            new File(libsDir, subpath).mkdirs();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path file : stream) {
                    copyResourceFiles(file, deletePrefixLength, libsDir);
                }
            }
        } else {
            try (FileOutputStream fos = new FileOutputStream(new File(libsDir, subpath))) {
                Files.copy(path, fos);
            }
        }
    }

    public interface WeChatOCRLibrary extends Library {
        // WeChatOCRLibrary dll = Native.load(wcocrDll, WeChatOCRLibrary.class, Collections.singletonMap(Library.OPTION_STRING_ENCODING, "UTF-8"));

        interface SetResCallback extends Callback {
            void callback(String arg);
        }

        boolean wechat_ocr(WString ocr_exe, WString wechat_dir, String imgfn, WeChatOCRLibrary.SetResCallback res);

        void stop_ocr();
    }
}
