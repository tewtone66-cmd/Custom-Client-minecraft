package tewtone66.customclient;

public final class Config {
    public boolean hudEnabled=true,fpsHud=true,keystrokes=true,armorHud=true,itemCounters=true,coords=true;
    public boolean performanceMode=false,compactHud=false,particles=true,weather=true,hitIndicator=true,cpsHud=true,pingHud=true;
    public boolean potionHud=true,targetHud=false,customCrosshair=true,sprintToggle=false,animatedBackground=true,glassUi=true,cosmetics=true;
    public boolean menuAnimations=true,lobbyParticles=true,itemAnimations=true,lowFire=true,cleanF3=true;
    public boolean neonCrosshair=true,orbitRing=false,hitFlash=true,lobbyGlow=true,nameplate=false,motionTrail=false;
    public int fpsLimit=120,uiScale=100,accent=0xFF39A7FF;
    public void toggle(String key){
        switch(key){
            case "hud"->hudEnabled=!hudEnabled; case "fps"->fpsHud=!fpsHud; case "keys"->keystrokes=!keystrokes;
            case "armor"->armorHud=!armorHud; case "counter"->itemCounters=!itemCounters; case "coords"->coords=!coords;
            case "perf"->performanceMode=!performanceMode; case "compact"->compactHud=!compactHud; case "particles"->particles=!particles;
            case "weather"->weather=!weather; case "hit"->hitIndicator=!hitIndicator; case "cps"->cpsHud=!cpsHud;
            case "ping"->pingHud=!pingHud; case "potion"->potionHud=!potionHud; case "target"->targetHud=!targetHud;
            case "crosshair"->customCrosshair=!customCrosshair; case "sprint"->sprintToggle=!sprintToggle;
            case "animated"->animatedBackground=!animatedBackground; case "glass"->glassUi=!glassUi;
            case "menuAnimations"->menuAnimations=!menuAnimations; case "lobbyParticles"->lobbyParticles=!lobbyParticles;
            case "itemAnimations"->itemAnimations=!itemAnimations; case "lowFire"->lowFire=!lowFire; case "cleanF3"->cleanF3=!cleanF3;
            case "cosmetics"->cosmetics=!cosmetics; case "neonCrosshair"->neonCrosshair=!neonCrosshair;
            case "orbitRing"->orbitRing=!orbitRing; case "hitFlash"->hitFlash=!hitFlash; case "lobbyGlow"->lobbyGlow=!lobbyGlow;
            case "nameplate"->nameplate=!nameplate; case "motionTrail"->motionTrail=!motionTrail;
        }
    }
}