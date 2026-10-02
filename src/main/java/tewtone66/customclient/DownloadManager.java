package tewtone66.customclient;

import net.minecraft.client.Minecraft;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.concurrent.ConcurrentHashMap;

public final class DownloadManager {
    private static final HttpClient HTTP=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final Set<String> TRUSTED_HOSTS=Set.of("cdn.modrinth.com","api.modrinth.com","github.com","raw.githubusercontent.com");
    private static final ConcurrentHashMap<String,Task> TASKS=new ConcurrentHashMap<>();
    private DownloadManager(){}
    public record Snapshot(String key,long received,long total,long bytesPerSecond,String phase,boolean finished,boolean success,String fileName){
        public int percent(){return total>0?(int)Math.max(0,Math.min(100,(received*100L)/total)):-1;}
        public double receivedMb(){return received/1048576d;}
        public double totalMb(){return total>0?total/1048576d:-1;}
        public double speedMb(){return bytesPerSecond/1048576d;}
    }
    private static final class Task{
        final String key,fileName; volatile long received,total,speed; volatile String phase="STARTING"; volatile boolean finished,success;
        Task(String key,String fileName){this.key=key;this.fileName=fileName;}
        Snapshot snapshot(){return new Snapshot(key,received,total,speed,phase,finished,success,fileName);}
    }
    public static Snapshot snapshot(String key){Task t=TASKS.get(key);return t==null?null:t.snapshot();}
    public static void downloadMod(String url,String fileName){downloadMod(url,fileName,fileName);}
    public static void downloadMod(String url,String fileName,String key){download(url,Minecraft.getInstance().gameDirectory.toPath().resolve("mods"),fileName,true,key);}
    public static void downloadResourcePack(String url,String fileName){downloadResourcePack(url,fileName,fileName);}
    public static void downloadResourcePack(String url,String fileName,String key){download(url,Minecraft.getInstance().gameDirectory.toPath().resolve("resourcepacks"),fileName,false,key);}
    public static void downloadSchematic(String url,String fileName){download(url,Minecraft.getInstance().gameDirectory.toPath().resolve(".tewpvp-nova").resolve("schematics"),fileName,false,fileName);}
    public static void downloadCustom(String url,String fileName,String type){if("mod".equals(type))downloadMod(url,fileName);else if("resourcepack".equals(type))downloadResourcePack(url,fileName);else if("schematic".equals(type))downloadSchematic(url,fileName);}
    public static boolean installed(String type,String fileName){
        Path dir=switch(type){case "mod"->Minecraft.getInstance().gameDirectory.toPath().resolve("mods");case "resourcepack"->Minecraft.getInstance().gameDirectory.toPath().resolve("resourcepacks");case "schematic"->Minecraft.getInstance().gameDirectory.toPath().resolve(".tewpvp-nova").resolve("schematics");default->Minecraft.getInstance().gameDirectory.toPath();};
        return Files.isRegularFile(dir.resolve(fileName).normalize());
    }
    private static void download(String url,Path directory,String fileName,boolean restartRequired,String key){
        try{
            URI uri=URI.create(url);String host=uri.getHost();
            if(host==null||!TRUSTED_HOSTS.contains(host.toLowerCase(Locale.ROOT))){notifyUser("برای امنیت، فقط منابع مورداعتماد مجاز هستند.");return;}
            String safeName=fileName.replaceAll("[^a-zA-Z0-9._+()\\- ]","_");if(safeName.isBlank()){notifyUser("نام فایل معتبر نیست.");return;}
            Files.createDirectories(directory);Path target=directory.resolve(safeName).normalize();if(!target.getParent().equals(directory.normalize())){notifyUser("مسیر فایل نامعتبر است.");return;}
            Task task=new Task(key,safeName);TASKS.put(key,task);task.phase="CONNECTING";
            HttpRequest request=HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(10)).header("User-Agent","tewtone66/TewPvP-Nova-Client/1.2.0").GET().build();
            CompletableFuture.runAsync(()->{
                try{
                    HttpResponse<InputStream> response=HTTP.send(request,HttpResponse.BodyHandlers.ofInputStream());
                    if(response.statusCode()<200||response.statusCode()>=300)throw new IllegalStateException("HTTP "+response.statusCode());
                    task.total=response.headers().firstValueAsLong("Content-Length").orElse(-1);task.phase="DOWNLOADING";
                    long started=System.nanoTime(),lastBytes=0,lastTime=started;
                    try(InputStream in=response.body();OutputStream out=Files.newOutputStream(target)){
                        byte[] buffer=new byte[64*1024];int n;
                        while((n=in.read(buffer))!=-1){out.write(buffer,0,n);task.received+=n;long now=System.nanoTime();if(now-lastTime>=250_000_000L){task.speed=(long)((task.received-lastBytes)*1_000_000_000d/(now-lastTime));lastBytes=task.received;lastTime=now;}}
                    }
                    task.speed=0;task.phase="COMPLETED";task.finished=true;task.success=true;
                    Minecraft.getInstance().execute(()->{notifyUser("دانلود کامل شد: "+safeName);if(!restartRequired)Minecraft.getInstance().reloadResourcePacks();else notifyUser("فایل داخل mods ذخیره شد؛ برای فعال شدن مود بازی را دوباره اجرا کن.");});
                }catch(Exception e){try{Files.deleteIfExists(target);}catch(Exception ignored){}task.phase="FAILED";task.finished=true;task.success=false;Minecraft.getInstance().execute(()->notifyUser("دانلود ناموفق: "+safeMessage(e)));}
            });
        }catch(Exception e){notifyUser("لینک دانلود معتبر نیست.");}
    }
    private static String safeMessage(Exception e){String s=e.getMessage();return s==null?e.getClass().getSimpleName():s;}
    private static void notifyUser(String message){Minecraft mc=Minecraft.getInstance();mc.execute(()->ClientCore.notify(mc,message));}
}