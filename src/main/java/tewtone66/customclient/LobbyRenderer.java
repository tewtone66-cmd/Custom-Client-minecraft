package tewtone66.customclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public final class LobbyRenderer {
    private LobbyRenderer(){}
    public static void render(Screen screen, GuiGraphics g, float delta){
        int w=screen.width,h=screen.height;
        long now=System.currentTimeMillis();
        double t=now*0.0008;
        g.fill(0,0,w,h,0xFF090610);
        // Layered animated aurora bands.
        for(int i=0;i<8;i++){
            int x=(int)(Math.sin(t*(0.7+i*0.08)+i*1.7)*w*0.22+w*(0.12+i*0.11));
            int y=(int)(Math.cos(t*(0.5+i*0.05)+i)*h*0.18+h*0.18);
            int rw=180+i*55,rh=70+i*12;
            int col=(0x18<<24)|((i%3==0?0xFF:0x9A)<<16)|((i%3==1?0x6B:0x35)<<8)|(i%2==0?0xD6:0xFF);
            g.fill(Math.max(0,x-rw),Math.max(0,y-rh),Math.min(w,x+rw),Math.min(h,y+rh),col);
        }
        // Moving stars / particles.
        for(int i=0;i<42;i++){
            double a=t*(0.18+(i%7)*0.03)+i*0.91;
            int x=(int)((Math.sin(a*1.7+i)*0.5+0.5)*(w-1));
            int y=(int)((Math.cos(a*1.1+i*0.37)*0.5+0.5)*(h-1));
            int s=1+(i%3);
            g.fill(x,y,x+s,y+s,0xA0FFFFFF);
        }
        // Central Nova glass layer behind vanilla buttons.
        int panelW=Math.min(520,w-80), panelH=Math.min(250,h-120);
        int px=(w-panelW)/2,py=Math.max(50,(h-panelH)/2-10);
        g.fill(px-2,py-2,px+panelW+2,py+panelH+2,0x45000000);
        g.fill(px,py,px+panelW,py+panelH,0x6B100B18);
        g.fill(px,py,px+panelW,py+2,0xFFFF6BD6);
        g.fill(px,py+panelH-2,px+panelW,py+panelH,0x8B8B5CFF);
        g.drawString(net.minecraft.client.Minecraft.getInstance().font,"TEW PVP",px+18,py+16,0xFFFF6BD6,true);
        g.drawString(net.minecraft.client.Minecraft.getInstance().font,"NOVA",px+18,py+31,0xFFFFFFFF,true);
        String[] features={"ANIMATED LOBBY","PVP HUD","MOD CENTER","COSMETICS LAB"};
        for(int i=0;i<features.length;i++){
            int fx=px+18+i*((panelW-36)/4);
            g.fill(fx,py+58,fx+2,py+84,(i%2==0)?0xFFFF6BD6:0xFF8B5CFF);
            g.drawString(net.minecraft.client.Minecraft.getInstance().font,features[i],fx+8,py+62,0xFFD8CCEA,false);
        }
        g.drawString(net.minecraft.client.Minecraft.getInstance().font,"CUSTOM CLIENT  •  1.21.11",px+18,py+105,0xFF9B8EAA,false);
    }
}