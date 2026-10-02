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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ModrinthCatalog {
    public enum Type { MOD, RESOURCE_PACK }
    public record Project(String id,String title,String description,String iconUrl,long downloads,String latestVersion,String projectType){}
    public record Result(List<Project> projects,int total,String error){}
    private static final HttpClient HTTP=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(12)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final String API="https://api.modrinth.com/v2";
    private static final Map<String,Identifier> ICONS=new ConcurrentHashMap<>();
    private static final Map<String,Boolean> LOADING=new ConcurrentHashMap<>();
    private ModrinthCatalog(){}
    public static void search(Type type,String query,int page,Consumer<Result> callback){
        int offset=Math.max(0,page)*8;
        CompletableFuture.runAsync(()->{try{
            String facets=type==Type.MOD?"[[\"project_type:mod\"],[\"versions:1.21.11\"],[\"categories:fabric\"]]":"[[\"project_type:resourcepack\"],[\"versions:1.21.11\"]]";
            String url=API+"/search?query="+enc(query==null?"":query)+"&facets="+enc(facets)+"&index=downloads&offset="+offset+"&limit=8";
            JsonObject root=getJson(url);List<Project> list=new ArrayList<>();
            for(var e:root.getAsJsonArray("hits")){JsonObject o=e.getAsJsonObject();list.add(new Project(text(o,"project_id"),text(o,"title"),text(o,"description"),text(o,"icon_url"),o.has("downloads")?o.get("downloads").getAsLong():0,text(o,"latest_version"),type==Type.MOD?"mod":"resourcepack"));}
            int total=root.has("total_hits")?root.get("total_hits").getAsInt():0;Minecraft.getInstance().execute(()->callback.accept(new Result(list,total,null)));
        }catch(Exception e){Minecraft.getInstance().execute(()->callback.accept(new Result(Collections.emptyList(),0,"Modrinth: "+e.getMessage())));}});
    }
    public static void download(Project project,Type type){CompletableFuture.runAsync(()->{try{
        String loaders=type==Type.MOD?"[\"fabric\"]":"[\"minecraft\"]";String versions="[\"1.21.11\"]";
        JsonArray versionsJson=getArray(API+"/project/"+enc(project.id())+"/version?loaders="+enc(loaders)+"&game_versions="+enc(versions));
        if(versionsJson.isEmpty())throw new IllegalStateException("نسخه سازگار 1.21.11 پیدا نشد.");
        JsonArray files=versionsJson.get(0).getAsJsonObject().getAsJsonArray("files");if(files==null||files.isEmpty())throw new IllegalStateException("فایل دانلودی پیدا نشد.");
        JsonObject selected=files.get(0).getAsJsonObject();for(var e:files){JsonObject c=e.getAsJsonObject();if(c.has("primary")&&c.get("primary").getAsBoolean()){selected=c;break;}}
        String u=text(selected,"url"),n=text(selected,"filename");if(u.isBlank()||n.isBlank())throw new IllegalStateException("اطلاعات فایل ناقص است.");
        if(type==Type.MOD)DownloadManager.downloadMod(u,n);else DownloadManager.downloadResourcePack(u,n);
    }catch(Exception e){Minecraft.getInstance().execute(()->ClientCore.notify(Minecraft.getInstance(),"دانلود نشد: "+e.getMessage()));}});}
    public static Identifier icon(String url){
        if(url==null||url.isBlank())return null;Identifier ready=ICONS.get(url);if(ready!=null)return ready;if(LOADING.putIfAbsent(url,true)!=null)return null;
        CompletableFuture.runAsync(()->{try{HttpRequest req=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).header("User-Agent","TewPvP-Nova/1.2").GET().build();
            HttpResponse<byte[]> res=HTTP.send(req,HttpResponse.BodyHandlers.ofByteArray());if(res.statusCode()<200||res.statusCode()>=300)throw new IllegalStateException();
            NativeImage image=NativeImage.read(res.body());Minecraft.getInstance().execute(()->{try{Identifier id=Identifier.fromNamespaceAndPath(ClientCore.MOD_ID,"remote/"+Integer.toHexString(url.hashCode()));DynamicTexture texture=new DynamicTexture(()->"tewpvp_icon",image);Minecraft.getInstance().getTextureManager().register(id,texture);ICONS.put(url,id);}catch(Exception ignored){image.close();}finally{LOADING.remove(url);}});
        }catch(Exception ignored){LOADING.remove(url);}});
        return null;
    }
    private static JsonObject getJson(String url)throws Exception{HttpRequest req=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).header("User-Agent","TewPvP-Nova/1.2").header("Accept","application/json").GET().build();HttpResponse<String> res=HTTP.send(req,HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));if(res.statusCode()<200||res.statusCode()>=300)throw new IllegalStateException("HTTP "+res.statusCode());return JsonParser.parseString(res.body()).getAsJsonObject();}
    private static JsonArray getArray(String url)throws Exception{HttpRequest req=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).header("User-Agent","TewPvP-Nova/1.2").header("Accept","application/json").GET().build();HttpResponse<String> res=HTTP.send(req,HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));if(res.statusCode()<200||res.statusCode()>=300)throw new IllegalStateException("HTTP "+res.statusCode());return JsonParser.parseString(res.body()).getAsJsonArray();}
    private static String text(JsonObject o,String k){return o.has(k)&&!o.get(k).isJsonNull()?o.get(k).getAsString():"";}
    private static String enc(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8);}
}