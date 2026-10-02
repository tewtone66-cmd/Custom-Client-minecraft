package tewtone66.customclient;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class DownloadManager {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final Set<String> TRUSTED_HOSTS = Set.of(
            "cdn.modrinth.com",
            "github.com",
            "raw.githubusercontent.com"
    );

    private DownloadManager() {}

    public static void downloadMod(String url, String fileName) {
        download(url, Minecraft.getInstance().gameDirectory.toPath().resolve("mods"), fileName, true);
    }

    public static void downloadResourcePack(String url, String fileName) {
        download(url, Minecraft.getInstance().gameDirectory.toPath().resolve("resourcepacks"), fileName, false);
    }

    public static void downloadSchematic(String url, String fileName) {
        download(url, Minecraft.getInstance().gameDirectory.toPath().resolve("schematics"), fileName, false);
    }

    public static void downloadCustom(String url, String fileName, String type) {
        if ("mod".equals(type)) downloadMod(url, fileName);
        else if ("resourcepack".equals(type)) downloadResourcePack(url, fileName);
        else if ("schematic".equals(type)) downloadSchematic(url, fileName);
    }

    private static void download(String url, Path directory, String fileName, boolean restartRequired) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null || !TRUSTED_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
                notifyUser("برای امنیت، فقط دانلود از منابع مورداعتماد فعال است.");
                return;
            }

            String safeName = fileName.replaceAll("[^a-zA-Z0-9._+()\- ]", "_");
            if (safeName.isBlank()) {
                notifyUser("نام فایل معتبر نیست.");
                return;
            }

            Files.createDirectories(directory);
            Path target = directory.resolve(safeName).normalize();
            if (!target.getParent().equals(directory.normalize())) {
                notifyUser("مسیر فایل نامعتبر است.");
                return;
            }

            notifyUser("دانلود شروع شد: " + safeName);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMinutes(5))
                    .header("User-Agent", "TewPvP-Custom-Client/1.0")
                    .GET()
                    .build();

            CompletableFuture.runAsync(() -> {
                try {
                    HttpResponse<Path> response = HTTP.send(
                            request,
                            HttpResponse.BodyHandlers.ofFile(target)
                    );

                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        Minecraft.getInstance().execute(() -> {
                            notifyUser("دانلود شد: " + safeName);
                            if (!restartRequired) {
                                Minecraft.getInstance().reloadResourcePacks();
                            } else {
                                notifyUser("برای فعال شدن مود جدید، بازی را دوباره اجرا کن.");
                            }
                        });
                    } else {
                        Files.deleteIfExists(target);
                        notifyUser("دانلود ناموفق بود. کد: " + response.statusCode());
                    }
                } catch (Exception e) {
                    try { Files.deleteIfExists(target); } catch (IOException ignored) {}
                    notifyUser("خطا در دانلود: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            notifyUser("لینک دانلود معتبر نیست.");
        }
    }

    private static void notifyUser(String message) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> ClientCore.notify(mc, message));
    }
}
