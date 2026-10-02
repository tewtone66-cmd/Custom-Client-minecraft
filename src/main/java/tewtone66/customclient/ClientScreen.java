package tewtone66.customclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class ClientScreen extends Screen {
    private static final int BG=0xFF060A12,CARD=0xFF121C2C,CARD2=0xFF18263A,HOVER=0xFF213651;
    private static final int WHITE=0xFFF4F7FF,MUTED=0xFF8797B1,BLUE=0xFF35A7FF,CYAN=0xFF5CEBFF,GREEN=0xFF35D79B,RED=0xFFFF6078,PURPLE=0xFF9D7BFF;
    private enum Tab{HOME,MENU,HUD,PERFORMANCE,MODS,PACKS,SCHEMATICS,COSMETICS,SUPPORT}
    private Tab tab=Tab.HOME;
    private EditBox searchBox,urlBox;
    private int left,top,side,cx,cy,cw,ch,ticks; private boolean compact;
    private ModrinthCatalog.Type catalogType=ModrinthCatalog.Type.MOD;
    private List<ModrinthCatalog.Project> projects=List.of(); private int page,total; private boolean loading; private String error="";
    private final java.util.Set<String> queued=new java.util.HashSet<>();

    public ClientScreen(){super(Component.literal("TewPvP Nova Client"));}

    @Override protected void init(){
        layout();
        searchBox=new EditBox(font,cx+18,cy-2,Math.max(130,Math.min(300,cw-150)),22,Component.literal("Search"));
        searchBox.setSuggestion("Search Modrinth mods...");
        searchBox.setMaxLength(100); addRenderableWidget(searchBox);
        urlBox=new EditBox(font,cx+18,cy+ch-38,Math.max(130,cw-140),22,Component.literal("URL"));
        urlBox.setSuggestion("https://cdn.modrinth.com/...");
        urlBox.setMaxLength(500); addRenderableWidget(urlBox);
        syncFields();
        if(tab==Tab.MODS||tab==Tab.PACKS)loadCatalog();
    }

    private void layout(){
        compact=width<800||height<560;
        left=compact?7:14;top=compact?7:14;side=compact?118:168;
        cx=left+side+10;cy=compact?48:58;cw=Math.max(220,width-cx-left);ch=Math.max(250,height-cy-top);
    }
    private void syncFields(){
        if(searchBox!=null)searchBox.setVisible(tab==Tab.MODS||tab==Tab.PACKS);
        if(urlBox!=null)urlBox.setVisible(tab==Tab.SCHEMATICS);
    }
    @Override public void tick(){ticks++;}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        animate(g); shell(g,mx,my); sidebar(g,mx,my); content(g,mx,my);
        if(searchBox!=null&&searchBox.visible)searchBox.render(g,mx,my,delta);
        if(urlBox!=null&&urlBox.visible)urlBox.render(g,mx,my,delta);
    }

    private void animate(GuiGraphics g){
        g.fill(0,0,width,height,BG);
        if(!ClientCore.CONFIG.animatedBackground)return;
        double t=ticks*.025;
        for(int i=0;i<18;i++){
            int x=(int)((Math.sin(t*(.45+i*.035)+i)*.5+.5)*Math.max(1,width-4));
            int y=(int)((Math.cos(t*(.38+i*.027)+i*1.9)*.5+.5)*Math.max(1,height-4));
            int s=2+i%4; g.fill(x,y,x+s,y+s,0x1735A7FF);
        }
        if(ClientCore.CONFIG.lobbyGlow){
            int sweep=(int)((Math.sin(t*.55)+1)*.5*width);
            g.fill(sweep-130,0,sweep+130,height,0x081A4DFF);
        }
        if(ClientCore.CONFIG.orbitRing){
            int ox=width-72,oy=52;
            for(int i=0;i<12;i++){double a=t+i*Math.PI/6;int x=(int)(ox+35*Math.cos(a)),y=(int)(oy+20*Math.sin(a));g.fill(x,y,x+2,y+2,0xB05CEBFF);}
        }
    }

    private void shell(GuiGraphics g,int mx,int my){
        int panel=ClientCore.CONFIG.glassUi?0xEC101927:0xFF0E1622;
        g.fill(left,top,width-left,height-top,panel);
        g.fill(left,top,width-left,top+2,CYAN);
        g.fill(left+side,top+2,left+side+1,height-top,0xFF27364C);
        g.drawString(font,"TEW",left+15,top+11,CYAN,true);
        g.drawString(font,"PVP",left+45,top+11,WHITE,true);
        g.drawString(font,"NOVA",left+15,top+27,MUTED,true);
        String s=Minecraft.getInstance().getFps()+" FPS  •  1.21.11";
        g.drawString(font,s,width-left-font.width(s)-12,top+13,WHITE,false);
    }

    private void sidebar(GuiGraphics g,int mx,int my){
        String[] names=compact?new String[]{"Lobby","Menu","HUD","Perf","Mods","Packs","Schematics","Cosmetics","Support"}:
                new String[]{"Lobby","Client Menu","HUD Studio","Performance","Mod Center","Resource Packs","Schematics","Cosmetics","Support"};
        Tab[] tabs=Tab.values();int y=top+43,row=compact?27:29,gap=compact?3:4;
        for(int i=0;i<names.length;i++){
            boolean selected=tab==tabs[i],hover=inside(mx,my,left+7,y,side-14,row);
            g.fill(left+7,y,left+side-7,y+row,selected?0xFF1D3B60:(hover?HOVER:0x00121B2A));
            if(selected)g.fill(left+7,y,left+10,y+row,CYAN);
            g.drawString(font,names[i],left+16,y+(row-8)/2,selected?WHITE:MUTED,false); y+=row+gap;
        }
        if(!compact){
            g.drawString(font,"NOVA CLIENT",left+14,height-59,MUTED,false);
            g.drawString(font,"● READY",left+14,height-43,GREEN,true);
        }
    }

    private void content(GuiGraphics g,int mx,int my){
        String title=switch(tab){case HOME->"NOVA LOBBY";case MENU->"CLIENT MENU";case HUD->"HUD STUDIO";case PERFORMANCE->"PERFORMANCE";
            case MODS->"MOD CENTER";case PACKS->"RESOURCE PACKS";case SCHEMATICS->"SCHEMATIC LIBRARY";case COSMETICS->"COSMETICS LAB";case SUPPORT->"SUPPORT";};
        g.drawString(font,title,cx+16,top+11,WHITE,true);
        switch(tab){case HOME->home(g,mx,my);case MENU->menu(g,mx,my);case HUD->hud(g,mx,my);case PERFORMANCE->performance(g,mx,my);
            case MODS,PACKS->catalog(g,mx,my);case SCHEMATICS->schematics(g,mx,my);case COSMETICS->cosmetics(g,mx,my);case SUPPORT->support(g,mx,my);}
    }

    private void home(GuiGraphics g,int mx,int my){
        int y=cy+3,h=compact?126:142;card(g,cx+16,y,cw-32,h,mx,my);
        g.drawString(font,"TEW",cx+34,y+17,CYAN,true);g.drawString(font,"PVP NOVA CLIENT",cx+67,y+17,WHITE,true);
        g.drawString(font,"Custom animated PvP lobby for Minecraft 1.21.11",cx+34,y+38,MUTED,false);
        g.drawString(font,"Responsive UI • live Modrinth manager • test cosmetics",cx+34,y+55,MUTED,false);
        button(g,"CLIENT MENU",cx+34,y+78,128,31,mx,my,BLUE);
        button(g,"HUD STUDIO",cx+170,y+78,112,31,mx,my,PURPLE);
        button(g,"MOD CENTER",cx+290,y+78,112,31,mx,my,GREEN);
        button(g,"COSMETICS",cx+410,y+78,112,31,mx,my,RED);
        int sy=y+h+10;if(sy+45<height-10){
            mini(g,"FPS",""+Minecraft.getInstance().getFps(),cx+16,sy,82,CYAN);
            mini(g,"HUD",ClientCore.CONFIG.hudEnabled?"ON":"OFF",cx+106,sy,82,ClientCore.CONFIG.hudEnabled?GREEN:RED);
            mini(g,"ANIM",ClientCore.CONFIG.animatedBackground?"LIVE":"OFF",cx+196,sy,92,BLUE);
            mini(g,"BUILD","NOVA 1.2",cx+296,sy,105,PURPLE);
        }
        if(ClientCore.CONFIG.neonCrosshair){
            int ox=width/2,oy=height/2;g.fill(ox-6,oy,ox+7,oy+1,CYAN);g.fill(ox,oy-6,ox+1,oy+7,CYAN);
        }
    }

    private void menu(GuiGraphics g,int mx,int my){
        g.drawString(font,"Everything in one compact menu",cx+16,cy-4,MUTED,false);
        menuButton(g,"HUD STUDIO","PvP overlays and counters",Tab.HUD,cy+18,mx,my,BLUE);
        menuButton(g,"PERFORMANCE","FPS and visual load controls",Tab.PERFORMANCE,cy+78,mx,my,GREEN);
        menuButton(g,"MOD CENTER","Search hundreds of compatible mods",Tab.MODS,cy+138,mx,my,PURPLE);
        menuButton(g,"RESOURCE PACKS","Search packs with live thumbnails",Tab.PACKS,cy+198,mx,my,CYAN);
        menuButton(g,"COSMETICS LAB","Turn test cosmetics on/off",Tab.COSMETICS,cy+258,mx,my,RED);
        menuButton(g,"SCHEMATICS","Local library and downloads",Tab.SCHEMATICS,cy+318,mx,my,BLUE);
    }

    private void menuButton(GuiGraphics g,String title,String desc,Tab target,int y,int mx,int my,int accent){
        int h=compact?50:54;card(g,cx+16,y,cw-32,h,mx,my);g.fill(cx+28,y+12,cx+32,y+h-12,accent);
        g.drawString(font,title,cx+44,y+10,WHITE,true);g.drawString(font,desc,cx+44,y+28,MUTED,false);
        button(g,"OPEN",cx+cw-84,y+14,56,25,mx,my,accent);
    }

    private void hud(GuiGraphics g,int mx,int my){
        String[] k={"hud","fps","keys","armor","counter","coords","cps","ping","potion","target","hit","crosshair","sprint"};
        String[] l={"Master HUD","FPS","Keystrokes","Armor HUD","Crystal / Totem / Obsidian Counters","Coordinates","CPS","Ping","Potion Status","Target HUD","Hit Indicator","Custom Crosshair","Toggle Sprint"};
        for(int i=0;i<k.length;i++)toggleRow(g,l[i],k[i],cy+i*(compact?29:31),mx,my);
    }

    private void performance(GuiGraphics g,int mx,int my){
        String[] k={"perf","particles","weather","compact","animated","glass","menuAnimations","lobbyParticles","itemAnimations","lowFire","cleanF3"};
        String[] l={"Performance Mode","Particles","Weather","Compact HUD","Animated Background","Glass UI","Menu Animations","Lobby Particles","Item Animations","Low Fire","Clean F3"};
        for(int i=0;i<k.length;i++)toggleRow(g,l[i],k[i],cy+i*(compact?28:30),mx,my);
        int by=cy+(compact?180:188);card(g,cx+16,by,cw-32,88,mx,my);
        g.drawString(font,"FPS LIMIT",cx+30,by+13,MUTED,true);
        int lim=ClientCore.CONFIG.fpsLimit>=1000?999:ClientCore.CONFIG.fpsLimit;g.drawString(font,lim==999?"UNLIMITED":lim+" FPS",cx+30,by+31,CYAN,true);
        button(g,"60",cx+120,by+10,45,24,mx,my,BLUE);button(g,"120",cx+170,by+10,50,24,mx,my,BLUE);button(g,"240",cx+225,by+10,50,24,mx,my,BLUE);button(g,"∞",cx+280,by+10,35,24,mx,my,BLUE);
        g.drawString(font,"UI "+ClientCore.CONFIG.uiScale+"%",cx+30,by+58,MUTED,false);
        button(g,"−",cx+105,by+49,28,23,mx,my,BLUE);button(g,"+",cx+138,by+49,28,23,mx,my,BLUE);
    }

    private void catalog(GuiGraphics g,int mx,int my){
        button(g,catalogType==ModrinthCatalog.Type.MOD?"MODS":"PACKS",cx+cw-128,cy-2,58,22,mx,my,BLUE);
        button(g,"↻",cx+cw-65,cy-2,35,22,mx,my,GREEN);
        g.drawString(font,loading?"Loading Modrinth…":(error.isBlank()?"Minecraft 1.21.11 • Fabric • live catalog":error),cx+16,cy+27,error.isBlank()?MUTED:RED,false);
        int cols=compact?1:2,gap=8,start=cy+46,w=Math.max(170,(cw-32-gap*(cols-1))/cols),h=compact?76:82;
        for(int i=0;i<projects.size();i++){int col=i%cols,row=i/cols;projectCard(g,projects.get(i),cx+16+col*(w+gap),start+row*(h+gap),w,h,mx,my);}
        if(projects.isEmpty()&&!loading){card(g,cx+16,start,cw-32,76,mx,my);g.drawString(font,"No results. Try another search.",cx+30,start+21,WHITE,true);}
        int py=height-31;button(g,"‹",cx+16,py,27,23,mx,my,BLUE);g.drawString(font,"Page "+(page+1)+" / "+Math.max(1,(total+7)/8),cx+51,py+8,MUTED,false);button(g,"›",cx+132,py,27,23,mx,my,BLUE);
    }

    private void projectCard(GuiGraphics g,ModrinthCatalog.Project p,int x,int y,int w,int h,int mx,int my){
        boolean hover=inside(mx,my,x,y,w,h);
        int accent=p.projectType().equals("mod")?BLUE:GREEN;
        g.fill(x+2,y+2,x+w+2,y+h+2,0x70000000);
        g.fill(x,y,x+w,y+h,hover?0xFF213651:CARD2);
        g.fill(x,y,x+4,y+h,accent);
        Identifier icon=ModrinthCatalog.icon(p.iconUrl());
        if(icon!=null)g.blit(RenderPipelines.GUI_TEXTURED,icon,x+11,y+10,0,0,40,40,256,256);
        else{g.fill(x+11,y+10,x+51,y+50,0xFF29415F);String a=p.title().isBlank()?"?":p.title().substring(0,1);g.drawString(font,a,x+25,y+23,CYAN,true);}
        g.drawString(font,trim(p.title(),w-128),x+61,y+8,WHITE,true);
        g.drawString(font,trim(p.description(),w-128),x+61,y+25,MUTED,false);
        g.drawString(font,downloads(p.downloads())+" downloads",x+61,y+42,0xFF8A9AB2,false);
        DownloadManager.Snapshot ds=DownloadManager.snapshot(p.id());
        if(ds!=null&&!ds.finished()){
            String pct=ds.percent()>=0?ds.percent()+"%":"...";
            g.drawString(font,pct+"  "+String.format(Locale.ROOT,"%.1f MB",ds.receivedMb())+(ds.totalMb()>0?"/"+String.format(Locale.ROOT,"%.1f MB",ds.totalMb()):"")+"  "+String.format(Locale.ROOT,"%.1f MB/s",ds.speedMb()),x+61,y+57,CYAN,true);
            int bx=x+61,by=y+h-11,bw=Math.max(60,w-126);
            g.fill(bx,by,bx+bw,by+4,0xFF27364C);
            int fill=ds.percent()>=0?(int)(bw*ds.percent()/100.0):0;
            if(fill>0)g.fill(bx,by,bx+fill,by+4,accent);
            button(g,"...",x+w-51,y+h-29,41,22,mx,my,accent);
        }else{
            boolean done=ds!=null&&ds.finished()&&ds.success();
            button(g,done?"INSTALLED":"GET",x+w-70,y+h-30,58,22,mx,my,done?GREEN:accent);
            if(ds!=null&&ds.finished()&&!ds.success())g.drawString(font,"FAILED",x+61,y+57,RED,true);
        }
    }

    private void schematics(GuiGraphics g,int mx,int my){
        Path dir=Minecraft.getInstance().gameDirectory.toPath().resolve(".tewpvp-nova").resolve("schematics");long count=0;
        try{Files.createDirectories(dir);try(var s=Files.list(dir)){count=s.filter(p->{String n=p.getFileName().toString().toLowerCase(Locale.ROOT);return n.endsWith(".litematic")||n.endsWith(".schem")||n.endsWith(".schematic");}).count();}}catch(Exception ignored){}
        card(g,cx+16,cy+4,cw-32,110,mx,my);g.drawString(font,"LOCAL SCHEMATICS",cx+32,cy+20,WHITE,true);
        g.drawString(font,count+" files in .tewpvp-nova/schematics",cx+32,cy+42,CYAN,false);
        g.drawString(font,"Safe local library • .litematic / .schem / .schematic",cx+32,cy+61,MUTED,false);
        button(g,"DOWNLOAD",cx+32,cy+75,100,25,mx,my,GREEN);
    }

    private void cosmetics(GuiGraphics g,int mx,int my){
        card(g,cx+16,cy+4,cw-32,74,mx,my);g.drawString(font,"COSMETICS LAB",cx+32,cy+19,WHITE,true);
        g.drawString(font,"Test every visual style. Green = ON, red = OFF.",cx+32,cy+39,MUTED,false);
        button(g,ClientCore.CONFIG.cosmetics?"COSMETICS ON":"COSMETICS OFF",cx+cw-150,cy+22,116,26,mx,my,ClientCore.CONFIG.cosmetics?GREEN:RED);
        String[] k={"neonCrosshair","orbitRing","hitFlash","lobbyGlow","nameplate","motionTrail"};
        String[] n={"Neon Crosshair","Orbit Ring","Hit Flash","Lobby Glow","Nameplate","Motion Trail"};
        int cols=compact?1:2,gap=8,w=Math.max(170,(cw-32-gap*(cols-1))/cols);
        for(int i=0;i<k.length;i++){int x=cx+16+(i%cols)*(w+gap),y=cy+90+(i/cols)*52;toggleCard(g,n[i],k[i],x,y,w,44,mx,my);}
        if(ClientCore.CONFIG.cosmetics&&ClientCore.CONFIG.motionTrail){for(int i=0;i<10;i++){int x=(int)((ticks*4+i*37)%Math.max(1,width-20));g.fill(x,height/2+i%5,x+2,height/2+2+i%5,PURPLE);}}
    }

    private void support(GuiGraphics g,int mx,int my){
        card(g,cx+16,cy+4,cw-32,126,mx,my);g.drawString(font,"NOVA STATUS",cx+32,cy+20,WHITE,true);
        g.drawString(font,"● UI READY",cx+32,cy+41,GREEN,true);g.drawString(font,"Modrinth manager: live",cx+32,cy+59,MUTED,false);
        g.drawString(font,"Fabric mods: restart required • Resource packs: reload supported",cx+32,cy+77,MUTED,false);
        button(g,"CREATE DIAGNOSTICS",cx+32,cy+91,142,25,mx,my,BLUE);
    }

    private void toggleRow(GuiGraphics g,String label,String key,int y,int mx,int my){
        int x=cx+16,w=Math.max(190,cw-32),h=26;if(y+h>height-8)return;boolean on=state(key);
        g.fill(x,y,x+w,y+h,inside(mx,my,x,y,w,h)?HOVER:CARD);g.drawString(font,label,x+12,y+8,WHITE,false);
        g.fill(x+w-47,y+6,x+w-12,y+20,on?GREEN:0xFF293448);g.drawString(font,on?"ON":"OFF",x+w-42,y+8,on?0xFF06140F:RED,true);
    }

    private void toggleCard(GuiGraphics g,String label,String key,int x,int y,int w,int h,int mx,int my){
        boolean on=state(key);g.fill(x,y,x+w,y+h,inside(mx,my,x,y,w,h)?HOVER:CARD);
        g.fill(x+10,y+12,x+16,y+32,on?GREEN:RED);g.drawString(font,label,x+26,y+16,WHITE,false);
        g.drawString(font,on?"ON":"OFF",x+w-35,y+16,on?GREEN:RED,true);
    }

    private boolean state(String k){return switch(k){
        case "hud"->ClientCore.CONFIG.hudEnabled;case "fps"->ClientCore.CONFIG.fpsHud;case "keys"->ClientCore.CONFIG.keystrokes;case "armor"->ClientCore.CONFIG.armorHud;
        case "counter"->ClientCore.CONFIG.itemCounters;case "coords"->ClientCore.CONFIG.coords;case "cps"->ClientCore.CONFIG.cpsHud;case "ping"->ClientCore.CONFIG.pingHud;
        case "potion"->ClientCore.CONFIG.potionHud;case "target"->ClientCore.CONFIG.targetHud;case "hit"->ClientCore.CONFIG.hitIndicator;case "crosshair"->ClientCore.CONFIG.customCrosshair;
        case "sprint"->ClientCore.CONFIG.sprintToggle;case "perf"->ClientCore.CONFIG.performanceMode;case "particles"->ClientCore.CONFIG.particles;case "weather"->ClientCore.CONFIG.weather;
        case "compact"->ClientCore.CONFIG.compactHud;case "animated"->ClientCore.CONFIG.animatedBackground;case "glass"->ClientCore.CONFIG.glassUi;case "menuAnimations"->ClientCore.CONFIG.menuAnimations;
        case "lobbyParticles"->ClientCore.CONFIG.lobbyParticles;case "itemAnimations"->ClientCore.CONFIG.itemAnimations;case "lowFire"->ClientCore.CONFIG.lowFire;case "cleanF3"->ClientCore.CONFIG.cleanF3;
        case "cosmetics"->ClientCore.CONFIG.cosmetics;case "neonCrosshair"->ClientCore.CONFIG.neonCrosshair;case "orbitRing"->ClientCore.CONFIG.orbitRing;case "hitFlash"->ClientCore.CONFIG.hitFlash;
        case "lobbyGlow"->ClientCore.CONFIG.lobbyGlow;case "nameplate"->ClientCore.CONFIG.nameplate;case "motionTrail"->ClientCore.CONFIG.motionTrail;default->false;};}

    private void mini(GuiGraphics g,String a,String b,int x,int y,int w,int color){g.fill(x,y,x+w,y+42,CARD);g.drawString(font,a,x+8,y+7,MUTED,false);g.drawString(font,b,x+8,y+23,color,true);}
    private void card(GuiGraphics g,int x,int y,int w,int h,int mx,int my){boolean hover=inside(mx,my,x,y,w,h);g.fill(x+3,y+3,x+w+3,y+h+3,0x70000000);g.fill(x,y,x+w,y+h,hover?0xFF1C3048:CARD);g.fill(x,y,x+w,y+2,CYAN);g.fill(x,y,x+1,y+h,0xFF29435F);}
    private void button(GuiGraphics g,String s,int x,int y,int w,int h,int mx,int my,int accent){boolean hover=inside(mx,my,x,y,w,h);g.fill(x,y,x+w,y+h,hover?accent:0xFF1A2940);g.fill(x,y,x+w,y+2,accent);g.fill(x,y+h-2,x+w,y+h,hover?accent:0xFF263A55);g.drawString(font,s,x+(w-font.width(s))/2,y+(h-8)/2,WHITE,true);}
    private String trim(String s,int max){if(s==null)return "";if(font.width(s)<=max)return s;String o=s;while(o.length()>1&&font.width(o+"…")>max)o=o.substring(0,o.length()-1);return o+"…";}
    private String downloads(long n){return n>=1_000_000?String.format(Locale.ROOT,"%.1fM",n/1_000_000d):n>=1000?String.format(Locale.ROOT,"%.1fK",n/1000d):""+n;}

    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        if(super.mouseClicked(e,dbl))return true;if(e.button()!=0)return false;double mx=e.x(),my=e.y();
        Tab[] tabs=Tab.values();int y=top+43,row=compact?27:29,gap=compact?3:4;
        for(Tab t:tabs){if(inside(mx,my,left+7,y,side-14,row)){tab=t;syncFields();if(tab==Tab.MODS){catalogType=ModrinthCatalog.Type.MOD;page=0;loadCatalog();}if(tab==Tab.PACKS){catalogType=ModrinthCatalog.Type.RESOURCE_PACK;page=0;loadCatalog();}return true;}y+=row+gap;}
        if(tab==Tab.HOME){int y0=cy+81;if(inside(mx,my,cx+34,y0,128,31))tab=Tab.MENU;else if(inside(mx,my,cx+170,y0,112,31))tab=Tab.HUD;else if(inside(mx,my,cx+290,y0,112,31))tab=Tab.MODS;else if(inside(mx,my,cx+410,y0,112,31))tab=Tab.COSMETICS;syncFields();return true;}
        if(tab==Tab.MENU){int[] ys={cy+18,cy+78,cy+138,cy+198,cy+258,cy+318};Tab[] ts={Tab.HUD,Tab.PERFORMANCE,Tab.MODS,Tab.PACKS,Tab.COSMETICS,Tab.SCHEMATICS};for(int i=0;i<ys.length;i++)if(inside(mx,my,cx+cw-84,ys[i]+14,56,25)){tab=ts[i];syncFields();if(tab==Tab.MODS||tab==Tab.PACKS)loadCatalog();return true;}return true;}
        if(tab==Tab.HUD)return clickRows(mx,my,new String[]{"hud","fps","keys","armor","counter","coords","cps","ping","potion","target","hit","crosshair","sprint"},cy);
        if(tab==Tab.PERFORMANCE){if(clickRows(mx,my,new String[]{"perf","particles","weather","compact","animated","glass","menuAnimations","lobbyParticles","itemAnimations","lowFire","cleanF3"},cy))return true;
            int by=cy+(compact?180:188);if(inside(mx,my,cx+120,by+10,45,24))ClientCore.CONFIG.fpsLimit=60;else if(inside(mx,my,cx+170,by+10,50,24))ClientCore.CONFIG.fpsLimit=120;else if(inside(mx,my,cx+225,by+10,50,24))ClientCore.CONFIG.fpsLimit=240;else if(inside(mx,my,cx+280,by+10,35,24))ClientCore.CONFIG.fpsLimit=1000;else if(inside(mx,my,cx+105,by+49,28,23))ClientCore.CONFIG.uiScale=Math.max(75,ClientCore.CONFIG.uiScale-5);else if(inside(mx,my,cx+138,by+49,28,23))ClientCore.CONFIG.uiScale=Math.min(125,ClientCore.CONFIG.uiScale+5);return true;}
        if(tab==Tab.MODS||tab==Tab.PACKS)return clickCatalog(mx,my);
        if(tab==Tab.COSMETICS){if(inside(mx,my,cx+cw-150,cy+22,116,26)){ClientCore.CONFIG.cosmetics=!ClientCore.CONFIG.cosmetics;return true;}String[] k={"neonCrosshair","orbitRing","hitFlash","lobbyGlow","nameplate","motionTrail"};int cols=compact?1:2,cgap=8,w=Math.max(170,(cw-32-cgap*(cols-1))/cols);for(int i=0;i<k.length;i++){int x=cx+16+(i%cols)*(w+cgap),y0=cy+90+(i/cols)*52;if(inside(mx,my,x,y0,w,44)){ClientCore.CONFIG.toggle(k[i]);return true;}}return true;}
        if(tab==Tab.SCHEMATICS){if(inside(mx,my,cx+32,cy+75,100,25))downloadSchematic();return true;}
        if(tab==Tab.SUPPORT&&inside(mx,my,cx+32,cy+91,142,25)){try{Files.createDirectories(Minecraft.getInstance().gameDirectory.toPath().resolve(".tewpvp-nova").resolve("diagnostics"));ClientCore.notify(Minecraft.getInstance(),"diagnostics آماده شد.");}catch(Exception ignored){}return true;}
        return true;
    }

    private boolean clickRows(double mx,double my,String[] keys,int start){int step=compact?29:31;for(int i=0;i<keys.length;i++){int y=start+i*step;if(inside(mx,my,cx+16,y,Math.max(190,cw-32),26)){ClientCore.CONFIG.toggle(keys[i]);return true;}}return false;}
    private boolean clickCatalog(double mx,double my){
        if(inside(mx,my,cx+cw-128,cy-2,58,22)){catalogType=catalogType==ModrinthCatalog.Type.MOD?ModrinthCatalog.Type.RESOURCE_PACK:ModrinthCatalog.Type.MOD;page=0;loadCatalog();return true;}
        if(inside(mx,my,cx+cw-65,cy-2,35,22)){loadCatalog();return true;}
        int cols=compact?1:2,gap=8,start=cy+46,w=Math.max(170,(cw-32-gap*(cols-1))/cols),h=compact?72:78;
        for(int i=0;i<projects.size();i++){int col=i%cols,row=i/cols,x=cx+16+col*(w+gap),y=start+row*(h+gap);if(inside(mx,my,x+w-62,y+h-30,50,22)){queued.add(projects.get(i).id());ModrinthCatalog.download(projects.get(i),catalogType);return true;}}
        int py=height-31;if(inside(mx,my,cx+16,py,27,23)&&page>0){page--;loadCatalog();return true;}if(inside(mx,my,cx+132,py,27,23)&&(page+1)*8<total){page++;loadCatalog();return true;}return true;
    }
    private void loadCatalog(){loading=true;error="";String q=searchBox==null?"":searchBox.getValue().trim();ModrinthCatalog.search(catalogType,q,page,r->{projects=r.projects();total=r.total();error=r.error()==null?"":r.error();loading=false;});}
    private void downloadSchematic(){
        String url=urlBox.getValue().trim();if(url.isBlank()){ClientCore.notify(Minecraft.getInstance(),"اول لینک شماتیک را وارد کن.");return;}
        String name=url.substring(url.lastIndexOf('/')+1).split("\\?")[0];if(name.isBlank())name="schematic.litematic";
        if(!(name.endsWith(".litematic")||name.endsWith(".schem")||name.endsWith(".schematic"))){ClientCore.notify(Minecraft.getInstance(),"پسوند شماتیک معتبر نیست.");return;}
        DownloadManager.downloadSchematic(url,name);urlBox.setValue("");
    }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent e){if((tab==Tab.MODS||tab==Tab.PACKS)&&e.key()==257){page=0;loadCatalog();return true;}return super.keyPressed(e);}
    private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<=x+w&&my>=y&&my<=y+h;}
    @Override public void onClose(){Minecraft.getInstance().setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
}