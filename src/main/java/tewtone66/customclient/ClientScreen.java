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
    private static final int BG=0xFF070B14,PANEL=0xF5101726,CARD=0xFF131F31,HOVER=0xFF203451;
    private static final int WHITE=0xFFF5F8FF,MUTED=0xFF8E9BB2,BLUE=0xFF39A7FF,CYAN=0xFF55E8FF;
    private static final int GREEN=0xFF35D69B,RED=0xFFFF5F78,PURPLE=0xFF9B7BFF;
    private enum Tab{HOME,MENU,HUD,PERFORMANCE,MODS,PACKS,SCHEMATICS,COSMETICS,SUPPORT}
    private Tab tab=Tab.HOME;
    private EditBox searchBox;
    private int left,top,sidebarW,contentX,contentY,contentW,contentH,ticks;
    private boolean compact;
    private ModrinthCatalog.Type catalogType=ModrinthCatalog.Type.MOD;
    private List<ModrinthCatalog.Project> projects=List.of();
    private int catalogPage,catalogTotal;
    private String catalogError="";
    private boolean catalogLoading;

    public ClientScreen(){super(Component.literal("TewPvP Nova Client"));}

    @Override protected void init(){
        layout();
        searchBox=new EditBox(font,contentX+18,contentY-2,Math.max(120,Math.min(320,contentW-170)),22,Component.literal("Search"));
        searchBox.setMaxLength(100);
        searchBox.setSuggestion("Search Modrinth...");
        searchBox.setVisible(tab==Tab.MODS||tab==Tab.PACKS);
        addRenderableWidget(searchBox);
        if(tab==Tab.MODS||tab==Tab.PACKS)loadCatalog();
    }
    private void layout(){
        compact=width<760||height<520;
        left=compact?8:18;top=compact?8:18;sidebarW=compact?118:176;
        contentX=left+sidebarW+12;contentY=compact?54:66;
        contentW=Math.max(250,width-contentX-left);contentH=Math.max(260,height-contentY-top);
    }
    @Override public void tick(){ticks++;}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        renderAnimatedLobby(g);drawShell(g);drawSidebar(g,mx,my);drawContent(g,mx,my);
        if(searchBox!=null&&searchBox.visible)searchBox.render(g,mx,my,delta);
    }
    private void renderAnimatedLobby(GuiGraphics g){
        g.fill(0,0,width,height,BG);
        if(!ClientCore.CONFIG.animatedBackground||!ClientCore.CONFIG.lobbyParticles)return;
        double t=ticks*0.025;
        for(int i=0;i<14;i++){
            int px=(int)((Math.sin(t*(0.6+i*0.07)+i)*.5+.5)*Math.max(1,width-40));
            int py=(int)((Math.cos(t*(0.45+i*0.04)+i*1.7)*.5+.5)*Math.max(1,height-40));
            int s=2+i%4;g.fill(px,py,px+s,py+s,0x1839A7FF);
            if(i%3==0)g.fill(px-18,py,px+18,py+1,0x1235D6FF);
        }
        int sweep=(int)((Math.sin(t*.7)+1)*.5*Math.max(1,width));
        g.fill(sweep-110,0,sweep+110,height,0x071B55FF);
    }
    private void drawShell(GuiGraphics g){
        g.fill(left,top,width-left,height-top,PANEL);g.fill(left,top,width-left,top+3,CYAN);
        g.fill(left+sidebarW,top+3,left+sidebarW+1,height-top,0xFF24324A);
        int x=left+14;g.drawString(font,"TEW",x,top+12,CYAN,true);g.drawString(font,"PVP",x+32,top+12,WHITE,true);
        g.drawString(font,"NOVA CLIENT",x,top+28,MUTED,false);
        String s=Minecraft.getInstance().getFps()+" FPS  •  1.21.11";
        g.drawString(font,s,width-left-font.width(s)-14,top+16,WHITE,false);
    }
    private void drawSidebar(GuiGraphics g,int mx,int my){
        String[] a=compact?new String[]{"Lobby","Menu","HUD","Perf","Mods","Packs","Schematics","Cosmetics","Support"}:
                new String[]{"Lobby","Client Menu","HUD","Performance","Mods","Resource Packs","Schematics","Cosmetics","Support"};
        Tab[] tabs=Tab.values();int y=top+48,row=compact?28:30,gap=compact?3:4;
        for(int i=0;i<a.length;i++){
            boolean sel=tab==tabs[i],hov=inside(mx,my,left+7,y,sidebarW-14,row);
            g.fill(left+7,y,left+sidebarW-7,y+row,sel?0xFF1C3657:(hov?HOVER:0x00131C2B));
            if(sel)g.fill(left+7,y,left+10,y+row,BLUE);
            g.drawString(font,a[i],left+17,y+(row-8)/2,sel?WHITE:MUTED,false);y+=row+gap;
        }
        if(!compact){g.drawString(font,"TEWPVP STATUS",left+14,height-58,MUTED,false);g.drawString(font,"● ONLINE",left+14,height-42,GREEN,true);}
    }
    private void drawContent(GuiGraphics g,int mx,int my){
        String title=switch(tab){
            case HOME->"NOVA LOBBY";case MENU->"CLIENT MENU";case HUD->"HUD STUDIO";case PERFORMANCE->"PERFORMANCE";
            case MODS->"MOD CENTER";case PACKS->"RESOURCE PACKS";case SCHEMATICS->"SCHEMATIC LIBRARY";
            case COSMETICS->"COSMETICS LAB";case SUPPORT->"SUPPORT & DIAGNOSTICS";};
        g.drawString(font,title,contentX+18,top+14,WHITE,true);
        switch(tab){case HOME->home(g,mx,my);case MENU->menu(g,mx,my);case HUD->hud(g,mx,my);case PERFORMANCE->performance(g,mx,my);
            case MODS,PACKS->catalog(g,mx,my);case SCHEMATICS->schematics(g,mx,my);case COSMETICS->cosmetics(g,mx,my);case SUPPORT->support(g,mx,my);}
    }
    private void home(GuiGraphics g,int mx,int my){
        int y=contentY+4,h=compact?122:148;card(g,contentX+18,y,contentW-36,h,mx,my);
        g.fill(contentX+34,y+24,contentX+40,y+32,CYAN);
        g.drawString(font,"TEW",contentX+52,y+18,CYAN,true);g.drawString(font,"PVP NOVA",contentX+83,y+18,WHITE,true);
        g.drawString(font,"Custom PvP client lobby • 1.21.11",contentX+52,y+38,MUTED,false);
        g.drawString(font,"Animated • compact • original • mobile friendly",contentX+52,y+54,MUTED,false);
        int by=y+(compact?78:92);
        button(g,"CLIENT MENU",contentX+34,by,132,30,mx,my,BLUE);
        button(g,"HUD STUDIO",contentX+174,by,112,30,mx,my,PURPLE);
        button(g,"MOD CENTER",contentX+294,by,112,30,mx,my,GREEN);
        int sy=y+(compact?132:162);
        if(sy+54<height-12){mini(g,"FPS",String.valueOf(Minecraft.getInstance().getFps()),contentX+18,sy,92,CYAN);
            mini(g,"HUD",ClientCore.CONFIG.hudEnabled?"ON":"OFF",contentX+118,sy,92,GREEN);
            mini(g,"UI",ClientCore.CONFIG.animatedBackground?"LIVE":"STATIC",contentX+218,sy,110,BLUE);
            mini(g,"BUILD","NOVA",contentX+336,sy,110,PURPLE);}
        if(!compact){g.drawString(font,"Right Shift",contentX+18,height-28,CYAN,false);g.drawString(font,"opens the lobby from gameplay.",contentX+86,height-28,MUTED,false);}
    }
    private void menu(GuiGraphics g,int mx,int my){
        g.drawString(font,"All client controls in one compact menu",contentX+18,contentY-4,MUTED,false);
        menuCard(g,"HUD STUDIO","PvP overlays, counters, keystrokes and crosshair",Tab.HUD,contentY+24,mx,my,BLUE);
        menuCard(g,"PERFORMANCE","FPS, particles, weather and visual load",Tab.PERFORMANCE,contentY+94,mx,my,GREEN);
        menuCard(g,"MOD CENTER","Search compatible Fabric mods with thumbnails",Tab.MODS,contentY+164,mx,my,PURPLE);
        menuCard(g,"RESOURCE PACKS","Search 1.21.11 packs with thumbnails",Tab.PACKS,contentY+234,mx,my,CYAN);
        menuCard(g,"COSMETICS","Test client-only visual styles",Tab.COSMETICS,contentY+304,mx,my,RED);
    }
    private void menuCard(GuiGraphics g,String title,String desc,Tab target,int y,int mx,int my,int accent){
        int h=compact?56:62;card(g,contentX+18,y,contentW-36,h,mx,my);g.fill(contentX+30,y+14,contentX+34,y+h-14,accent);
        g.drawString(font,title,contentX+46,y+12,WHITE,true);g.drawString(font,trim(desc,Math.max(120,contentW-180)),contentX+46,y+30,MUTED,false);
        button(g,"OPEN",contentX+contentW-88,y+18,58,25,mx,my,accent);
    }
    private void hud(GuiGraphics g,int mx,int my){
        String[] k={"hud","fps","keys","armor","counter","coords","cps","ping","potion","target","hit","crosshair","sprint"};
        String[] l={"Master HUD","FPS","Keystrokes","Armor HUD","Crystal/Totem/Obsidian Counters","Coordinates","CPS","Ping","Potion Status","Target HUD","Hit Indicator","Custom Crosshair","Toggle Sprint"};
        for(int i=0;i<k.length;i++)option(g,l[i],k[i],contentY+i*(compact?30:32),mx,my);
    }
    private void performance(GuiGraphics g,int mx,int my){
        String[] k={"perf","particles","weather","compact","animated","glass","menuAnimations","lobbyParticles","itemAnimations","lowFire","cleanF3"};
        String[] l={"Performance Mode","Particles","Weather","Compact HUD","Animated Lobby","Glass UI","Menu Animations","Lobby Particles","Item Animations","Low Fire","Clean F3"};
        for(int i=0;i<k.length;i++)option(g,l[i],k[i],contentY+i*(compact?29:31),mx,my);
        int by=contentY+(compact?190:195);card(g,contentX+18,by,contentW-36,92,mx,my);
        g.drawString(font,"FPS LIMIT",contentX+32,by+13,WHITE,true);g.drawString(font,ClientCore.CONFIG.fpsLimit>=1000?"UNLIMITED":ClientCore.CONFIG.fpsLimit+" FPS",contentX+32,by+32,CYAN,true);
        int bx=contentX+130;button(g,"60",bx,by+10,48,25,mx,my,BLUE);button(g,"120",bx+54,by+10,52,25,mx,my,BLUE);
        button(g,"240",bx+112,by+10,52,25,mx,my,BLUE);button(g,"∞",bx+170,by+10,40,25,mx,my,BLUE);
        g.drawString(font,"UI Scale "+ClientCore.CONFIG.uiScale+"%",contentX+32,by+64,MUTED,false);
        button(g,"-",contentX+142,by+54,28,24,mx,my,BLUE);button(g,"+",contentX+176,by+54,28,24,mx,my,BLUE);
    }
    private void catalog(GuiGraphics g,int mx,int my){
        if(searchBox!=null)searchBox.setVisible(true);
        button(g,catalogType==ModrinthCatalog.Type.MOD?"MODS":"PACKS",contentX+contentW-134,contentY-2,56,22,mx,my,BLUE);
        button(g,"REFRESH",contentX+contentW-72,contentY-2,58,22,mx,my,GREEN);
        g.drawString(font,catalogLoading?"Loading Modrinth...":(catalogError.isBlank()?"Modrinth • Minecraft 1.21.11":catalogError),contentX+18,contentY+28,catalogError.isBlank()?MUTED:RED,false);
        int start=contentY+48,cols=compact?1:2,gap=8,w=Math.max(170,(contentW-36-gap*(cols-1))/cols),h=compact?70:78;
        for(int i=0;i<projects.size();i++){int col=i%cols,row=i/cols;projectCard(g,projects.get(i),contentX+18+col*(w+gap),start+row*(h+gap),w,h,mx,my);}
        if(projects.isEmpty()&&!catalogLoading){card(g,contentX+18,start,contentW-36,76,mx,my);g.drawString(font,"No compatible results yet.",contentX+34,start+18,WHITE,true);g.drawString(font,"Try another search or press REFRESH.",contentX+34,start+39,MUTED,false);}
        int ny=height-38;button(g,"‹",contentX+18,ny,28,24,mx,my,BLUE);
        String page="Page "+(catalogPage+1)+" / "+Math.max(1,(catalogTotal+5)/6);g.drawString(font,page,contentX+54,ny+8,MUTED,false);
        button(g,"›",contentX+132,ny,28,24,mx,my,BLUE);
    }
    private void projectCard(GuiGraphics g,ModrinthCatalog.Project p,int x,int y,int w,int h,int mx,int my){
        boolean hov=inside(mx,my,x,y,w,h);g.fill(x,y,x+w,y+h,hov?HOVER:CARD);g.fill(x,y,x+3,y+h,p.projectType().equals("mod")?BLUE:GREEN);
        Identifier icon=ModrinthCatalog.icon(p.iconUrl());
        if(icon!=null)g.blit(RenderPipelines.GUI_TEXTURED,icon,x+10,y+10,0f,0f,40,40,256,256);
        else{g.fill(x+10,y+10,x+50,y+50,0xFF253A58);String s=p.title().isBlank()?"?":p.title().substring(0,1);g.drawString(font,s,x+24,y+23,WHITE,true);}
        g.drawString(font,trim(p.title(),Math.max(70,w-132)),x+60,y+10,WHITE,true);
        g.drawString(font,trim(p.description(),Math.max(80,w-132)),x+60,y+28,MUTED,false);
        g.drawString(font,formatDownloads(p.downloads())+" downloads",x+60,y+45,0xFF71809A,false);
        button(g,"GET",x+w-62,y+h-31,48,23,mx,my,GREEN);
    }
    private void schematics(GuiGraphics g,int mx,int my){
        Path dir=Minecraft.getInstance().gameDirectory.toPath().resolve(".tewpvp-nova").resolve("schematics");long count=0;
        try{Files.createDirectories(dir);try(var s=Files.list(dir)){count=s.filter(p->{String n=p.getFileName().toString().toLowerCase(Locale.ROOT);return n.endsWith(".litematic")||n.endsWith(".schem")||n.endsWith(".schematic");}).count();}}catch(Exception ignored){}
        card(g,contentX+18,contentY+4,contentW-36,104,mx,my);g.drawString(font,"LOCAL SCHEMATICS",contentX+34,contentY+20,WHITE,true);
        g.drawString(font,count+" files in .tewpvp-nova/schematics",contentX+34,contentY+42,CYAN,false);g.drawString(font,"Supports .litematic • .schem • .schematic",contentX+34,contentY+61,MUTED,false);
        button(g,"OPEN FOLDER",contentX+34,contentY+72,104,25,mx,my,BLUE);
        card(g,contentX+18,contentY+122,contentW-36,86,mx,my);g.drawString(font,"DIRECT DOWNLOAD",contentX+34,contentY+140,WHITE,true);
        g.drawString(font,"Schematics stay local; downloads never execute as code.",contentX+34,contentY+160,MUTED,false);
        g.drawString(font,"Use the project's URL downloader when needed.",contentX+34,contentY+179,GREEN,false);
    }
    private void cosmetics(GuiGraphics g,int mx,int my){
        card(g,contentX+18,contentY+4,contentW-36,88,mx,my);g.drawString(font,"COSMETICS LAB",contentX+34,contentY+20,WHITE,true);
        g.drawString(font,"Client-only visual tests. No gameplay advantage.",contentX+34,contentY+40,MUTED,false);
        button(g,ClientCore.CONFIG.cosmetics?"ENABLED":"DISABLED",contentX+34,contentY+53,88,24,mx,my,ClientCore.CONFIG.cosmetics?GREEN:RED);
        String[] a={"Neon Crosshair","Orbit Ring","Hit Flash","Lobby Glow","Nameplate","Motion Trail"};int cols=compact?1:2,gap=8,w=Math.max(170,(contentW-36-gap*(cols-1))/cols);
        for(int i=0;i<a.length;i++){int x=contentX+18+(i%cols)*(w+gap),y=contentY+104+(i/cols)*52;card(g,x,y,w,44,mx,my);g.fill(x+12,y+13,x+18+(int)((Math.sin(ticks*.08+i)+1)*3),y+31,i%2==0?BLUE:PURPLE);g.drawString(font,a[i],x+28,y+16,WHITE,false);}
    }
    private void support(GuiGraphics g,int mx,int my){
        card(g,contentX+18,contentY+4,contentW-36,128,mx,my);g.drawString(font,"SAFE CLIENT SETUP",contentX+34,contentY+20,WHITE,true);
        g.drawString(font,"Downloads are restricted to Modrinth/GitHub sources.",contentX+34,contentY+40,MUTED,false);
        g.drawString(font,"Fabric mods need a restart; resource packs can reload.",contentX+34,contentY+58,MUTED,false);
        button(g,"CREATE DIAGNOSTICS",contentX+34,contentY+91,136,25,mx,my,BLUE);
        card(g,contentX+18,contentY+144,contentW-36,72,mx,my);g.drawString(font,"TewPvP Nova Client",contentX+34,contentY+162,CYAN,true);
        g.drawString(font,"Original UI • no proprietary client assets",contentX+34,contentY+183,MUTED,false);
    }
    private void option(GuiGraphics g,String label,String key,int y,int mx,int my){
        int x=contentX+18,w=Math.max(190,contentW-36),h=compact?26:28;if(y+h>height-8)return;
        boolean on=switch(key){
            case "hud"->ClientCore.CONFIG.hudEnabled;case "fps"->ClientCore.CONFIG.fpsHud;case "keys"->ClientCore.CONFIG.keystrokes;
            case "armor"->ClientCore.CONFIG.armorHud;case "counter"->ClientCore.CONFIG.itemCounters;case "coords"->ClientCore.CONFIG.coords;
            case "perf"->ClientCore.CONFIG.performanceMode;case "compact"->ClientCore.CONFIG.compactHud;case "particles"->ClientCore.CONFIG.particles;
            case "weather"->ClientCore.CONFIG.weather;case "hit"->ClientCore.CONFIG.hitIndicator;case "cps"->ClientCore.CONFIG.cpsHud;
            case "ping"->ClientCore.CONFIG.pingHud;case "potion"->ClientCore.CONFIG.potionHud;case "target"->ClientCore.CONFIG.targetHud;
            case "crosshair"->ClientCore.CONFIG.customCrosshair;case "sprint"->ClientCore.CONFIG.sprintToggle;case "animated"->ClientCore.CONFIG.animatedBackground;
            case "glass"->ClientCore.CONFIG.glassUi;case "menuAnimations"->ClientCore.CONFIG.menuAnimations;case "lobbyParticles"->ClientCore.CONFIG.lobbyParticles;
            case "itemAnimations"->ClientCore.CONFIG.itemAnimations;case "lowFire"->ClientCore.CONFIG.lowFire;case "cleanF3"->ClientCore.CONFIG.cleanF3;default->false;};
        g.fill(x,y,x+w,y+h,inside(mx,my,x,y,w,h)?HOVER:CARD);g.drawString(font,label,x+12,y+8,WHITE,false);g.drawString(font,on?"ON":"OFF",x+w-36,y+8,on?GREEN:RED,true);
    }
    private void mini(GuiGraphics g,String a,String b,int x,int y,int w,int accent){g.fill(x,y,x+w,y+46,CARD);g.drawString(font,a,x+9,y+8,MUTED,false);g.drawString(font,b,x+9,y+25,accent,true);}
    private void card(GuiGraphics g,int x,int y,int w,int h,int mx,int my){g.fill(x+2,y+2,x+w+2,y+h+2,0x50000000);g.fill(x,y,x+w,y+h,inside(mx,my,x,y,w,h)?HOVER:CARD);g.fill(x,y,x+w,y+1,0x221C9BFF);}
    private void button(GuiGraphics g,String s,int x,int y,int w,int h,int mx,int my,int accent){g.fill(x,y,x+w,y+h,inside(mx,my,x,y,w,h)?accent:0xFF1D2B40);g.drawString(font,s,x+(w-font.width(s))/2,y+(h-8)/2,WHITE,true);}
    private String trim(String v,int max){if(v==null)return "";if(font.width(v)<=max)return v;String o=v;while(o.length()>1&&font.width(o+"…")>max)o=o.substring(0,o.length()-1);return o+"…";}
    private String formatDownloads(long n){if(n>=1000000)return String.format(Locale.ROOT,"%.1fM",n/1000000.0);if(n>=1000)return String.format(Locale.ROOT,"%.1fK",n/1000.0);return String.valueOf(n);}

    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        if(super.mouseClicked(e,dbl))return true;if(e.button()!=0)return false;double mx=e.x(),my=e.y();
        Tab[] tabs=Tab.values();int y=top+48,row=compact?28:30,gap=compact?3:4;
        for(Tab t:tabs){if(inside(mx,my,left+7,y,sidebarW-14,row)){tab=t;if(searchBox!=null)searchBox.setVisible(tab==Tab.MODS||tab==Tab.PACKS);
                if(tab==Tab.MODS){catalogType=ModrinthCatalog.Type.MOD;catalogPage=0;loadCatalog();}else if(tab==Tab.PACKS){catalogType=ModrinthCatalog.Type.RESOURCE_PACK;catalogPage=0;loadCatalog();}return true;}y+=row+gap;}
        if(tab==Tab.HOME){int by=contentY+(compact?78:92);if(inside(mx,my,contentX+34,by,132,30))tab=Tab.MENU;else if(inside(mx,my,contentX+174,by,112,30))tab=Tab.HUD;else if(inside(mx,my,contentX+294,by,112,30))tab=Tab.MODS;if(searchBox!=null)searchBox.setVisible(tab==Tab.MODS||tab==Tab.PACKS);return true;}
        if(tab==Tab.MENU){if(inside(mx,my,contentX+contentW-88,contentY+24,58,62))tab=Tab.HUD;else if(inside(mx,my,contentX+contentW-88,contentY+94,58,62))tab=Tab.PERFORMANCE;
            else if(inside(mx,my,contentX+contentW-88,contentY+164,58,62)){tab=Tab.MODS;catalogType=ModrinthCatalog.Type.MOD;loadCatalog();}
            else if(inside(mx,my,contentX+contentW-88,contentY+234,58,62)){tab=Tab.PACKS;catalogType=ModrinthCatalog.Type.RESOURCE_PACK;loadCatalog();}
            else if(inside(mx,my,contentX+contentW-88,contentY+304,58,62))tab=Tab.COSMETICS;if(searchBox!=null)searchBox.setVisible(tab==Tab.MODS||tab==Tab.PACKS);return true;}
        if(tab==Tab.HUD)return clickOption(mx,my,new String[]{"hud","fps","keys","armor","counter","coords","cps","ping","potion","target","hit","crosshair","sprint"},contentY);
        if(tab==Tab.PERFORMANCE){if(clickOption(mx,my,new String[]{"perf","particles","weather","compact","animated","glass","menuAnimations","lobbyParticles","itemAnimations","lowFire","cleanF3"},contentY))return true;
            int by=contentY+(compact?190:195),bx=contentX+130;if(inside(mx,my,bx,by+10,48,25))ClientCore.CONFIG.fpsLimit=60;else if(inside(mx,my,bx+54,by+10,52,25))ClientCore.CONFIG.fpsLimit=120;else if(inside(mx,my,bx+112,by+10,52,25))ClientCore.CONFIG.fpsLimit=240;else if(inside(mx,my,bx+170,by+10,40,25))ClientCore.CONFIG.fpsLimit=1000;
            else if(inside(mx,my,contentX+142,by+54,28,24))ClientCore.CONFIG.uiScale=Math.max(75,ClientCore.CONFIG.uiScale-5);else if(inside(mx,my,contentX+176,by+54,28,24))ClientCore.CONFIG.uiScale=Math.min(110,ClientCore.CONFIG.uiScale+5);return true;}
        if(tab==Tab.MODS||tab==Tab.PACKS)return clickCatalog(mx,my);
        if(tab==Tab.COSMETICS&&inside(mx,my,contentX+34,contentY+53,88,24)){ClientCore.CONFIG.cosmetics=!ClientCore.CONFIG.cosmetics;return true;}
        if(tab==Tab.SUPPORT&&inside(mx,my,contentX+34,contentY+91,136,25)){try{Files.createDirectories(Minecraft.getInstance().gameDirectory.toPath().resolve(".tewpvp-nova").resolve("diagnostics"));ClientCore.notify(Minecraft.getInstance(),"پوشه diagnostics ساخته شد.");}catch(Exception ignored){}return true;}
        return true;
    }
    private boolean clickCatalog(double mx,double my){
        if(inside(mx,my,contentX+contentW-134,contentY-2,56,22)){catalogType=catalogType==ModrinthCatalog.Type.MOD?ModrinthCatalog.Type.RESOURCE_PACK:ModrinthCatalog.Type.MOD;catalogPage=0;loadCatalog();return true;}
        if(inside(mx,my,contentX+contentW-72,contentY-2,58,22)){loadCatalog();return true;}
        int cols=compact?1:2,gap=8,w=Math.max(170,(contentW-36-gap*(cols-1))/cols),h=compact?70:78,start=contentY+48;
        for(int i=0;i<projects.size();i++){int col=i%cols,row=i/cols,x=contentX+18+col*(w+gap),y=start+row*(h+gap);if(inside(mx,my,x+w-62,y+h-31,48,23)){ModrinthCatalog.download(projects.get(i),catalogType);return true;}}
        int ny=height-38;if(inside(mx,my,contentX+18,ny,28,24)&&catalogPage>0){catalogPage--;loadCatalog();return true;}
        if(inside(mx,my,contentX+132,ny,28,24)&&(catalogPage+1)*6<catalogTotal){catalogPage++;loadCatalog();return true;}return true;
    }
    private void loadCatalog(){catalogLoading=true;catalogError="";String q=searchBox==null?"":searchBox.getValue().trim();ModrinthCatalog.search(catalogType,q,catalogPage,r->{projects=r.projects();catalogTotal=r.total();catalogError=r.error()==null?"":r.error();catalogLoading=false;});}
    private boolean clickOption(double mx,double my,String[] keys,int sy){int y=sy,step=compact?30:32,h=compact?26:28;for(String k:keys){if(inside(mx,my,contentX+18,y,Math.max(190,contentW-36),h)){ClientCore.CONFIG.toggle(k);return true;}y+=step;}return false;}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent e){if((tab==Tab.MODS||tab==Tab.PACKS)&&e.key()==257){catalogPage=0;loadCatalog();return true;}return super.keyPressed(e);}
    private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<=x+w&&my>=y&&my<=y+h;}
    @Override public void onClose(){Minecraft.getInstance().setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
}