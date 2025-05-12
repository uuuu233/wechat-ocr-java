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
import uu.ocr.domain.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class WeChatOCR {
    private static String mmmojoDirectory;
    private static String wechatBinary;
    private static String tempDirectory;
    private static WeChatOCRLibrary dll;
    private static OkHttpClient okhttp = new OkHttpClient();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static boolean loaded = false;
    private static final String CURRENT_VERSION = "3";

    private WeChatOCR() {

    }

    @SneakyThrows
    public static Result apply(String path) {
        try {
            if (path.toLowerCase().startsWith("http")) {
                return apply(okhttp.newCall(new Request.Builder().get().url(path).build()).execute().body().bytes());
            }
            AtomicReference<String> reference = new AtomicReference<>();
            dll.wechat_ocr(new WString(wechatBinary), new WString(mmmojoDirectory), path, reference::set);
            JsonNode jsonNode = objectMapper.readTree(reference.get());
            Result result = objectMapper.treeToValue(jsonNode, Result.class);
            result.setSuccess(jsonNode.path("errcode").asInt(1) == 0);
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

    public static synchronized void load(String libDir, String tempDir) throws IOException {
        if (loaded) {
            return;
        }
        libDir = Path.of(libDir).toAbsolutePath().toString();
        tempDir = Path.of(tempDir).toAbsolutePath().toString();

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES , false);
        tempDirectory = tempDir;
        // 清空临时目录
        File tempFile = new File(tempDir);
        if (tempFile.isDirectory()) {
            FileUtils.deleteDirectory(tempFile);
        }
        tempFile.mkdirs();

        mmmojoDirectory = libDir;
        wechatBinary = Path.of(libDir, "lib", "WeChatOCR.exe").toString();

        dll = Native.load(Path.of(libDir, "wcocr.dll").toString(), WeChatOCRLibrary.class, Collections.singletonMap(Library.OPTION_STRING_ENCODING, "UTF-8"));
        Runtime.getRuntime().addShutdownHook(new Thread(dll::stop_ocr));
        loaded = true;
    }

    /*public static void load() throws IOException {
        load(Path.of(System.getenv("APPDATA"), "WeChatOCR", "temp").toFile().getCanonicalPath());
    }*/

    public interface WeChatOCRLibrary extends Library {
        // WeChatOCRLibrary dll = Native.load(wcocrDll, WeChatOCRLibrary.class, Collections.singletonMap(Library.OPTION_STRING_ENCODING, "UTF-8"));

        interface SetResCallback extends Callback {
            void callback(String arg);
        }

        boolean wechat_ocr(WString ocr_exe, WString wechat_dir, String imgfn, WeChatOCRLibrary.SetResCallback res);

        void stop_ocr();
    }
}
