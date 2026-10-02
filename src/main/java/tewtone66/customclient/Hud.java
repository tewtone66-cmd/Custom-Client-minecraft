package tewtone66.customclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Hud {
    private static final Deque<Long> CLICKS=new ArrayDeque<>();
    private static boolean lastLeft;
    private static long lastClick;

    private Hud(){}

    public static void render(GuiGraphics g){
        Config c=ClientCore.CONFIG;
        if(!c.hudEnabled)return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.options.hideGui||mc.screen!=null)return;

        int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();
        int pad=c.compactHud?6:10;
        updateClicks(mc);

        if(c.fpsHud)fpsPanel(g,mc,pad,pad);
        if(c.pingHud)pingPanel(g,mc,w-pad-112,pad);
        if(c.itemCounters)counters(g,mc,w-pad-142,pad+52);
        if(c.armorHud)armor(g,mc,pad,h-78);
        if(c.keystrokes)keystrokes(g,mc,w-pad-122,h-92);
        if(c.coords)coords(g,mc,pad,h-28);
        if(c.potionHud)potions(g,mc,pad+104,pad+50);
        if(c.cpsHud)cps(g,mc,w/2-50,pad);
        if(c.targetHud)target(g,mc,w/2-86,h-80);
        if(c.customCrosshair)crosshair(g,mc,w/2,h/2);
        if(c.hitIndicator)hitIndicator(g,mc,w/2,h/2);
        handItem(g,mc,w-pad-176,h-54);
    }

    private static void updateClicks(Minecraft mc){
        boolean left=mc.mouseHandler.isLeftPressed();
        long now=System.currentTimeMillis();
        if(left&&!lastLeft&&now-lastClick>35){CLICKS.addLast(now);lastClick=now;}
        lastLeft=left;
        while(!CLICKS.isEmpty()&&now-CLICKS.peekFirst()>1000)CLICKS.removeFirst();
    }

    private static void fpsPanel(GuiGraphics g,Minecraft mc,int x,int y){
        panel(g,x,y,100,42,0xE50D1524,HIGHLIGHT());
        g.drawString(mc.font,"TEW PVP",x+9,y+7,0xFF5CEBFF,true);
        g.drawString(mc.font,"FPS  "+mc.getFps(),x+9,y+22,0xFFFFFFFF,false);
    }

    private static void pingPanel(GuiGraphics g,Minecraft mc,int x,int y){
        int ping=0;
        if(mc.getConnection()!=null&&mc.getConnection().getPlayerInfo(mc.player.getUUID())!=null)ping=mc.getConnection().getPlayerInfo(mc.player.getUUID()).getLatency();
        panel(g,x,y,112,42,0xE50D1524,0xFF7C5CFF);
        g.drawString(mc.font,"NETWORK",x+8,y+7,0xFF9D7BFF,true);
        g.drawString(mc.font,ping+" ms",x+8,y+22,0xFFFFFFFF,false);
    }

    private static void counters(GuiGraphics g,Minecraft mc,int x,int y){
        panel(g,x,y,142,70,0xE50D1524,0xFF35A7FF);
        g.drawString(mc.font,"PVP INVENTORY",x+9,y+7,0xFF35A7FF,true);
        g.drawString(mc.font,"Crystal",x+9,y+23,0xFFFFFFFF,false);g.drawString(mc.font,""+count(mc,Items.END_CRYSTAL),x+108,y+23,0xFFFF70E8,true);
        g.drawString(mc.font,"Totem",x+9,y+37,0xFFFFFFFF,false);g.drawString(mc.font,""+count(mc,Items.TOTEM_OF_UNDYING),x+108,y+37,0xFFFFB84D,true);
        g.drawString(mc.font,"Obsidian",x+9,y+51,0xFFFFFFFF,false);g.drawString(mc.font,""+count(mc,Items.OBSIDIAN),x+108,y+51,0xFFB8C2D0,true);
    }

    private static void armor(GuiGraphics g,Minecraft mc,int x,int y){
        panel(g,x,y,176,70,0xE50D1524,0xFF35D79B);
        g.drawString(mc.font,"ARMOR",x+8,y+7,0xFF35D79B,true);
        for(int i=0;i<4;i++){
            ItemStack stack=mc.player.getInventory().getItem(36+i);
            int ix=x+8+i*41;
            if(!stack.isEmpty()){
                g.renderItem(stack,ix,y+21);
                if(stack.isDamageableItem()){
                    int left=Math.max(0,stack.getMaxDamage()-stack.getDamageValue());
                    int max=Math.max(1,stack.getMaxDamage());
                    int pct=(int)(left*100L/max);
                    g.fill(ix,y+53,ix+34,y+57,0xFF293448);
                    g.fill(ix,y+53,ix+34*pct/100,y+57,pct>25?0xFF35D79B:0xFFFF6078);
                    g.drawString(mc.font,pct+"%",ix+7,y+42,0xFFFFFFFF,false);
                }
            }else g.fill(ix+10,y+28,ix+25,y+43,0xFF263448);
        }
    }

    private static void handItem(GuiGraphics g,Minecraft mc,int x,int y){
        ItemStack stack=mc.player.getMainHandItem();
        if(stack.isEmpty())return;
        int w=168;
        panel(g,x,y,w,48,0xE50D1524,0xFF5CEBFF);
        g.renderItem(stack,x+8,y+8);
        String name=stack.getHoverName().getString();
        if(name.length()>19)name=name.substring(0,18)+"…";
        g.drawString(mc.font,name,x+35,y+9,0xFFFFFFFF,true);
        g.drawString(mc.font,"x"+stack.getCount(),x+35,y+24,0xFF5CEBFF,false);
    }

    private static void potions(GuiGraphics g,Minecraft mc,int x,int y){
        int shown=0;
        for(MobEffectInstance e:mc.player.getActiveEffects()){
            if(shown>=3)break;
            String name=e.getEffect().value().getDisplayName().getString();
            int sec=Math.max(0,e.getDuration()/20);
            String text=(name.length()>11?name.substring(0,10)+"…":name)+" "+sec+"s";
            panel(g,x,y+shown*22,126,19,0xDD0D1524,0xFFFFB84D);
            g.drawString(mc.font,text,x+6,y+5,0xFFFFFFFF,false);
            shown++;
        }
    }

    private static void cps(GuiGraphics g,Minecraft mc,int x,int y){
        panel(g,x,y,100,36,0xE50D1524,0xFF35A7FF);
        g.drawString(mc.font,"CPS",x+8,y+6,0xFF35A7FF,true);
        g.drawString(mc.font,""+CLICKS.size(),x+54,y+6,0xFFFFFFFF,true);
        g.drawString(mc.font,"LEFT CLICK",x+8,y+21,0xFF8797B1,false);
    }

    private static void target(GuiGraphics g,Minecraft mc,int x,int y){
        panel(g,x,y,172,42,0xE50D1524,0xFFFF6078);
        g.drawString(mc.font,"TARGET HUD",x+8,y+7,0xFFFF6078,true);
        g.drawString(mc.font,"No target",x+8,y+23,0xFFB8C2D0,false);
    }

    private static void coords(GuiGraphics g,Minecraft mc,int x,int y){
        String s=String.format("XYZ  %d  /  %d  /  %d",(int)mc.player.getX(),(int)mc.player.getY(),(int)mc.player.getZ());
        panel(g,x,y,Math.min(196,mc.font.width(s)+18),21,0xE50D1524,0xFF35A7FF);
        g.drawString(mc.font,s,x+8,y+6,0xFFFFFFFF,false);
    }

    private static void keystrokes(GuiGraphics g,Minecraft mc,int x,int y){
        panel(g,x,y,122,84,0xE50D1524,0xFF9D7BFF);
        g.drawString(mc.font,"KEYSTROKES",x+8,y+7,0xFF9D7BFF,true);
        key(g,mc,"W",x+47,y+20,mc.options.keyUp.isDown());
        key(g,mc,"A",x+12,y+43,mc.options.keyLeft.isDown());
        key(g,mc,"S",x+47,y+43,mc.options.keyDown.isDown());
        key(g,mc,"D",x+82,y+43,mc.options.keyRight.isDown());
        key(g,mc,"JUMP",x+42,y+65,mc.options.keyJump.isDown());
    }

    private static void key(GuiGraphics g,Minecraft mc,String text,int x,int y,boolean down){
        int bg=down?0xFF2B8CFF:0xFF1B2638;
        g.fill(x,y,x+31,y+19,bg);g.fill(x,y,x+31,y+2,down?0xFF5CEBFF:0xFF34445C);
        g.drawString(mc.font,text,x+(31-mc.font.width(text))/2,y+6,0xFFFFFFFF,false);
    }

    private static void crosshair(GuiGraphics g,Minecraft mc,int x,int y){
        int col=ClientCore.CONFIG.neonCrosshair?0xFF5CEBFF:0xFFFFFFFF;
        g.fill(x-7,y,x-2,y+2,col);g.fill(x+3,y,x+8,y+2,col);g.fill(x,y-7,x+2,y-2,col);g.fill(x,y+3,x+2,y+8,col);
        g.fill(x,y,x+2,y+2,0xFFFFFFFF);
    }

    private static void hitIndicator(GuiGraphics g,Minecraft mc,int x,int y){
        float charge=mc.player.getAttackStrengthScale(0);
        if(charge<0.98f){int a=(int)(70*(1-charge));int col=(a<<24)|0xFF6078;g.fill(x-13,y-13,x-10,y+13,col);g.fill(x+10,y-13,x+13,y+13,col);g.fill(x-13,y-13,x+13,y-10,col);g.fill(x-13,y+10,x+13,y+13,col);}
    }

    private static int count(Minecraft mc,net.minecraft.world.item.Item item){
        int total=0;
        for(int i=0;i<mc.player.getInventory().getContainerSize();i++){ItemStack stack=mc.player.getInventory().getItem(i);if(stack.is(item))total+=stack.getCount();}
        return total;
    }

    private static int HIGHLIGHT(){return 0xFF35A7FF;}

    private static void panel(GuiGraphics g,int x,int y,int w,int h,int bg,int accent){
        g.fill(x+2,y+2,x+w+2,y+h+2,0x66000000);
        g.fill(x,y,x+w,y+h,bg);
        g.fill(x,y,x+w,y+2,accent);
        g.fill(x,y,x+2,y+h,accent);
    }
}
