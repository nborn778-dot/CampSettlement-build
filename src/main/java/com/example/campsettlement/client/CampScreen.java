package com.example.campsettlement.client;

import com.example.campsettlement.data.CampSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CampScreen extends Screen {
    public CampScreen(){super(Component.literal("Лагерь"));}

    @Override protected void init(){
        int cx=width/2;int top=height/2-70;
        addRenderableWidget(Button.builder(Component.literal("Нанять рабочего — 5 еды"),b->CampClient.serverAction(d->{if(d.spend(5,0,0)){d.addWorker();spawnWorker();}})).bounds(cx-110,top+45,220,20).build());
        addRenderableWidget(Button.builder(Component.literal("Нанять стража — 5 еды + 5 камня"),b->CampClient.serverAction(d->{if(d.spend(5,0,5)){d.addGuard();spawnGuard();}})).bounds(cx-110,top+70,220,20).build());
        addRenderableWidget(Button.builder(Component.literal("Закрыть"),b->onClose()).bounds(cx-50,top+105,100,20).build());
    }

    private void spawnWorker(){
        var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();if(server==null)return;
        server.execute(()->{var l=server.overworld();var d=CampSavedData.get(l);var p=d.getCampPos();var v=net.minecraft.world.entity.EntityType.VILLAGER.create(l);if(v!=null){v.moveTo(p.getX()+1.5,p.getY()+1,p.getZ()+1.5,0,0);v.addTag("campsettlement_worker");v.setVillagerData(v.getVillagerData().setProfession(net.minecraft.world.entity.npc.VillagerProfession.FARMER));v.setCustomName(Component.literal("Рабочий лагеря"));v.setCustomNameVisible(false);l.addFreshEntity(v);}});
    }
    private void spawnGuard(){
        var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();if(server==null)return;
        server.execute(()->{var l=server.overworld();var d=CampSavedData.get(l);var p=d.getCampPos();var g=net.minecraft.world.entity.EntityType.IRON_GOLEM.create(l);if(g!=null){g.moveTo(p.getX()+2.5,p.getY()+1,p.getZ()+2.5,0,0);g.addTag("campsettlement_guard");g.setCustomName(Component.literal("Страж лагеря"));l.addFreshEntity(g);}});
    }

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        renderBackground(g);int cx=width/2;int top=height/2-70;
        g.drawCenteredString(font,title,cx,top-25,0xFFFFFF);
        var server=Minecraft.getInstance().getSingleplayerServer();
        if(server!=null){CampSavedData d=CampSavedData.get(server.overworld());
            g.drawString(font,"Еда: "+d.getFood(),cx-100,top,0xFFFFFF);
            g.drawString(font,"Дерево: "+d.getWood(),cx,top,0xFFFFFF);
            g.drawString(font,"Камень: "+d.getStone(),cx+80,top,0xFFFFFF);
            g.drawString(font,"Рабочие: "+d.getWorkers()+"   Стражи: "+d.getGuards(),cx-100,top+20,0xBFE8FF);
            long left=Math.max(0,d.getNextRaid()-server.overworld().getGameTime());
            g.drawCenteredString(font,"Следующий рейд: "+(left/20)+" сек.",cx,top+35,0xFF7777);
        }
        super.render(g,mouseX,mouseY,partial);
    }
    @Override public boolean isPauseScreen(){return false;}
}
