package com.example.campsettlement.event;

import com.example.campsettlement.CampSettlement;
import com.example.campsettlement.data.CampSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Random;

@Mod.EventBusSubscriber(modid=CampSettlement.MOD_ID)
public class CampEvents {
    private static final Random R=new Random();

    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END || e.getServer().isDedicatedServer()) return;
        ServerLevel l=e.getServer().overworld(); CampSavedData d=CampSavedData.get(l);
        if(!d.hasCamp()) return;
        long now=l.getGameTime();
        if(now>=d.getNextProduction()) produce(l,d,now);
        if(now>=d.getNextRaid()) raid(l,d,now);
        if(d.getOutposts().isEmpty() && now>600) outpost(l,d);
    }

    private static void produce(ServerLevel l,CampSavedData d,long now){
        int workers=d.getWorkers();
        if(workers>0){d.addFood(workers);d.addWood(Math.max(1,workers/2));}
        d.addStone(Math.max(1,d.getGuards()/2));
        d.setNextProduction(now+600);
    }

    private static void raid(ServerLevel l,CampSavedData d,long now){
        ServerPlayer p=l.getServer().getPlayerList().getPlayers().stream().findFirst().orElse(null); if(p==null)return;
        BlockPos c=d.getCampPos();
        int difficulty=2+(int)Math.min(10,now/24000);
        int n=Math.max(1,difficulty+d.getWorkers()/2-d.getGuards());
        for(int i=0;i<n;i++){
            double a=R.nextDouble()*Math.PI*2,dist=22+R.nextDouble()*12;
            int x=c.getX()+(int)(Math.cos(a)*dist),z=c.getZ()+(int)(Math.sin(a)*dist);
            int y=l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);
            Zombie mob=new Zombie(net.minecraft.world.entity.EntityType.ZOMBIE,l);
            mob.moveTo(x+.5,y,z+.5,R.nextFloat()*360,0);mob.setTarget(p);l.addFreshEntity(mob);
        }
        if(d.getGuards()<2){
            Pillager pill=net.minecraft.world.entity.EntityType.PILLAGER.create(l);
            if(pill!=null){
                int px=c.getX()+20;
                int pz=c.getZ()+20;
                int py=l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,px,pz);
                pill.moveTo(px+.5,py,pz+.5,0,0);
                pill.setTarget(p);
                l.addFreshEntity(pill);
            }
        }
        p.sendSystemMessage(Component.literal("§cНа лагерь напали! Врагов: "+n));
        d.setNextRaid(now+12000);
    }

    private static void outpost(ServerLevel l,CampSavedData d){
        BlockPos c=d.getCampPos();double a=R.nextDouble()*Math.PI*2;int dist=70+R.nextInt(51);
        int x=c.getX()+(int)(Math.cos(a)*dist),z=c.getZ()+(int)(Math.sin(a)*dist);
        int y=l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);BlockPos center=new BlockPos(x,y,z);
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)l.setBlock(center.offset(dx,-1,dz),Blocks.COBBLESTONE.defaultBlockState(),3);
        l.setBlock(center,Blocks.OBSIDIAN.defaultBlockState(),3);l.setBlock(center.above(),Blocks.CAMPFIRE.defaultBlockState(),3);
        for(int dx:new int[]{-2,2})for(int dz:new int[]{-2,2})l.setBlock(center.offset(dx,0,dz),Blocks.OAK_FENCE.defaultBlockState(),3);
        for(int i=0;i<3;i++){Pillager m=net.minecraft.world.entity.EntityType.PILLAGER.create(l);if(m!=null){m.moveTo(center.getX()+R.nextInt(7)-3+.5,center.getY()+1,center.getZ()+R.nextInt(7)-3+.5,0,0);l.addFreshEntity(m);}}
        d.addOutpost(center);
    }

    public static boolean nearCamp(Entity e, CampSavedData d){return d.hasCamp() && e.blockPosition().closerThan(d.getCampPos(),24);}

    @SubscribeEvent public static void death(LivingDeathEvent e){
        if(!(e.getEntity().level() instanceof ServerLevel l))return;
        CampSavedData d=CampSavedData.get(l);if(!d.hasCamp())return;
        if(e.getEntity() instanceof Villager v && v.getTags().contains("campsettlement_worker") && nearCamp(v,d)) d.addWorker(-1);
        if(e.getEntity() instanceof IronGolem g && g.getTags().contains("campsettlement_guard") && nearCamp(g,d)) d.addGuard(-1);
    }
}
