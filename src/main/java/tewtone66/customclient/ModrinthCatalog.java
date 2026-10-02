package tewtone66.customclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ModrinthCatalog {
    public enum Type { MOD, RESOURCE_PACK }

    public record Project(String id, String title, String description, String iconUrl, long downloads, String latestVersion, String projectType) {}
    public record Result(List<Project> projects, int total, String error) {}

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final String API = "https://api.modrinth.com/v2";
    private static final String USER_AGENT = "tewtone66/TewPvP-Nova-Client/1.1.0";
    private static final Map<String, Identifier> ICONS = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> LOADING = new ConcurrentHashMap<>();

    private ModrinthCatalog() {}

    public static void search(Type type, String query, int page, Consumer<Result> callback) {
        int offset = Math.max(0, page) * 6;
        CompletableFuture.runAsync(() -> {
            try {
                String project = type == Type.MOD ? "mod" : "resourcepack";
                String facets = type == Type.MOD
                        ? "[["project_type:mod"],["versions:1.21.11"],["categories:fabric"]]"
                        : "[["project_type:resourcepack"],["versions:1.21.11"]]";
                String url = API + "/search?query=" + enc(query == null ? "" : query)
                        + "&facets=" + enc(facets) + "&index=downloads&offset=" + offset + "&limit=6";
                JsonObject root = getJson(url);
                List<Project> projects = new ArrayList<>();
                for (var e : root.getAsJsonArray("hits")) {
                    JsonObject o = e.getAsJsonObject();
                    projects.add(new Project(
                            text(o,"project_id"), text(o,"title"), text(o,"description"),
                            text(o,"icon_url"), o.has("downloads") ? o.get("downloads").getAsLong() : 0,
                            text(o,"latest_version"), project));
                }
                int total = root.has("total_hits") ? root.get("total_hits").getAsInt() : 0;
                Minecraft.getInstance().execute(() -> callback.accept(new Result(projects, total, null)));
            } catch (Exception e) {
                Minecraft.getInstance().execute(() -> callback.accept(new Result(Collections.emptyList(), 0, "Modrinth: " + e.getMessage())));
            }
        });
    }

    public static void download(Project project, Type type) {
        CompletableFuture.runAsync(() -> {
            try {
                String loaders = type == Type.MOD ? "["fabric"]" : "["minecraft"]";
                String url = API + "/project/" + enc(project.id()) + "/version?loaders=" + enc(loaders)
                        + "&game_versions=" + enc("["1.21.11"]") + "&include_changelog=false";
                JsonArray versions = getArray(url);
                if (versions.isEmpty()) throw new IllegalStateException("نسخه سازگار 1.21.11 پیدا نشد.");
                JsonArray files = versions.get(0).getAsJsonObject().getAsJsonArray("files");
                if (files == null || files.isEmpty()) throw new IllegalStateException("فایل دانلودی پیدا نشد.");
                JsonObject selected = files.get(0).getAsJsonObject();
                for (var e : files) {
                    JsonObject candidate = e.getAsJsonObject();
                    if (candidate.has("primary") && candidate.get("primary").getAsBoolean()) {
                        selected = candidate;
                        break;
                    }
                }
                String fileUrl = text(selected,"url");
                String fileName = text(selected,"filename");
                if (fileUrl.isBlank() || fileName.isBlank()) throw new IllegalStateException("اطلاعات فایل ناقص است.");
                if (type == Type.MOD) DownloadManager.downloadMod(fileUrl, fileName);
                else DownloadManager.downloadResourcePack(fileUrl, fileName);
            } catch (Exception e) {
                Minecraft.getInstance().execute(() -> ClientCore.notify(Minecraft.getInstance(), "دانلود نشد: " + e.getMessage()));
            }
        });
    }

    public static Identifier icon(String url) {
        if (url == null || url.isBlank()) return null;
        Identifier ready = ICONS.get(url);
        if (ready != null) return ready;
        if (LOADING.putIfAbsent(url, true) != null) return null;
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                        .header("User-Agent", USER_AGENT).GET().build();
                HttpResponse<byte[]> response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IllegalStateException();
                NativeImage image = NativeImage.read(response.body());
                Minecraft.getInstance().execute(() -> {
                    try {
                        Identifier id = Identifier.fromNamespaceAndPath(ClientCore.MOD_ID, "remote/" + Integer.toHexString(url.hashCode()));
                        DynamicTexture texture = new DynamicTexture(() -> "tewpvp_icon", image);
                        Minecraft.getInstance().getTextureManager().register(id, texture);
                        ICONS.put(url, id);
                    } catch (Exception ignored) {
                        image.close();
                    } finally {
                        LOADING.remove(url);
                    }
                });
            } catch (Exception ignored) {
                LOADING.remove(url);
            }
        });
        return null;
    }

    private static JsonObject getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                .header("User-Agent", USER_AGENT).header("Accept","application/json").GET().build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IllegalStateException("HTTP " + response.statusCode());
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private static JsonArray getArray(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                .header("User-Agent", USER_AGENT).header("Accept","application/json").GET().build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IllegalStateException("HTTP " + response.statusCode());
        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    private static String text(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}