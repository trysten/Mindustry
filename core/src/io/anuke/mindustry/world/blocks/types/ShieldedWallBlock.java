package io.anuke.mindustry.world.blocks.types;

import com.badlogic.gdx.graphics.Color;

import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Draw;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.core.Timers;

public class ShieldedWallBlock extends PowerBlock{
	static final float hitTime = 18f;
	static final Color hitColor = Color.SKY.cpy().mul(1.2f);
	public float powerToDamage = 0.1f;

	public ShieldedWallBlock(String name) {
		super(name);
	}
	
	@Override
	public int handleDamage(Tile tile, int amount){
		float drain = amount * powerToDamage;
		ShieldedWallEntity entity = tile.entity();
		
		if(entity.power > drain){
			entity.power -= drain;
			entity.hit = hitTime;
			return 0;
		}else if(entity.power > 0){
			int reduction = (int)(entity.power / powerToDamage);
			entity.power = 0;
			
			return amount - reduction;
		}
		
		return amount;
	}
	
	@Override
	public void draw(Tile tile){
		super.draw(tile);
		
		ShieldedWallEntity entity = tile.entity();
		
		if(entity.power > powerToDamage){
			Graphics.surface("shield", false);
			Draw.color(Color.ROYAL);
			Draw.rect("blank", tile.worldx(), tile.worldy(), Vars.tilesize, Vars.tilesize);
			Graphics.surface();
		}
		
		Draw.color(hitColor);
		Draw.alpha(entity.hit / hitTime * 0.9f);
		Draw.rect("blank", tile.worldx(), tile.worldy(), Vars.tilesize, Vars.tilesize);
		Draw.reset();
		
		entity.hit -= Timers.delta();
		entity.hit = Math.max(entity.hit, 0);
	}
	
	@Override
	public TileEntity getEntity(){
		return new ShieldedWallEntity();
	}
	
	static class ShieldedWallEntity extends PowerEntity{
		public float hit;
	}
}