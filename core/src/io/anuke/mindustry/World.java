package io.anuke.mindustry;

import static io.anuke.mindustry.Vars.*;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.anuke.mindustry.ai.Pathfind;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Generator;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.Blocks;
import io.anuke.mindustry.world.blocks.ProductionBlocks;
import io.anuke.mindustry.world.blocks.WeaponBlocks;
import io.anuke.ucore.core.Graphics;
import io.anuke.ucore.entities.Entities;
import io.anuke.ucore.entities.Entity;
import io.anuke.ucore.entities.SolidEntity;
import io.anuke.ucore.util.Mathf;

public class World{
	private static Tile[] temptiles = new Tile[4];
	
	public static boolean solid(int x, int y){
		Tile tile = tile(x, y);
		
		return tile == null || tile.block().solid;
	}
	
	public static int width(){
		return Vars.mapPixmaps[Vars.currentMap].getWidth();
	}
	
	public static int height(){
		return Vars.mapPixmaps[Vars.currentMap].getHeight();
	}
	
	public static Tile tile(int x, int y){
		if(!Mathf.inBounds(x, y, tiles)) return null;
		return tiles[x][y];
	}
	
	public static Tile cursorTile(){
		return tile(tilex(), tiley());
	}
	
	public static Tile[] getNearby(int x, int y){
		temptiles[0] = tile(x+1, y);
		temptiles[1] = tile(x, y+1);
		temptiles[2] = tile(x-1, y);
		temptiles[3] = tile(x, y-1);
		return temptiles;
	}
	
	public static void loadMap(int id){
		spawnpoints.clear();
		
		int size = mapPixmaps[id].getWidth();
		worldsize = size;
		pixsize = worldsize*tilesize;
		tiles = new Tile[worldsize][worldsize];
		currentMap = id;
		
		for(int x = 0; x < worldsize; x ++){
			for(int y = 0; y < worldsize; y ++){
				tiles[x][y] = new Tile(x, y, Blocks.stone);
			}
		}
		
		Entities.resizeTree(0, 0, pixsize, pixsize);
		
		Generator.generate(id);
		
		Pathfind.reset();
		
		core.setBlock(ProductionBlocks.core);
		int x = core.x, y = core.y;
		
		set(x, y-1, ProductionBlocks.conveyor, 1);
		set(x, y-2, ProductionBlocks.conveyor, 1);
		set(x, y-3, ProductionBlocks.conveyor, 1);
		set(x, y-4, ProductionBlocks.stonedrill, 0);
		//just in case
		tiles[x][y-4].setFloor(Blocks.stone);
		
		
		tiles[x+2][y-2].setFloor(Blocks.stone);
		set(x+2, y-2, ProductionBlocks.stonedrill, 0);
		set(x+2, y-1, ProductionBlocks.conveyor, 1);
		set(x+2, y, WeaponBlocks.turret, 0);
		
		tiles[x-2][y-2].setFloor(Blocks.stone);
		set(x-2, y-2, ProductionBlocks.stonedrill, 0);
		set(x-2, y-1, ProductionBlocks.conveyor, 1);
		set(x-2, y, WeaponBlocks.turret, 0);
		
		Pathfind.updatePath();
	}
	
	static void set(int x, int y, Block type, int rot){
		tiles[x][y].setBlock(type);
		tiles[x][y].rotation = rot;
	}
	
	public static boolean validPlace(int x, int y, Block type){

		if(!cursorNear())
			return false;
		
		for(Tile spawn : spawnpoints){
			if(Vector2.dst(x * tilesize, y * tilesize, spawn.worldx(), spawn.worldy()) < enemyspawnspace){
				return false;
			}
		}

		for(SolidEntity e : Entities.getNearby(x * tilesize, y * tilesize, tilesize * 2f)){
			Rectangle.tmp.setSize(e.hitsize);
			Rectangle.tmp.setCenter(e.x, e.y);

			if(getCollider(x, y).overlaps(Rectangle.tmp)){
				return false;
			}
		}
		return tile(x, y).block() == Blocks.air;
	}
	
	public static boolean cursorNear(){
		return Vector2.dst(player.x, player.y, tilex() * tilesize, tiley() * tilesize) <= placerange;
	}
	
	public static Rectangle getCollider(int x, int y){
		return Rectangle.tmp2.setSize(tilesize).setCenter(x * tilesize, y * tilesize);
	}
	
	public static TileEntity findTileTarget(float x, float y, Tile tile, float range, boolean damaged){
		Entity closest = null;
		float dst = 0;
		
		int rad = (int)(range/tilesize)+1;
		int tilex = Mathf.scl2(x, tilesize);
		int tiley = Mathf.scl2(y, tilesize);
		
		for(int rx = -rad; rx <= rad; rx ++){
			for(int ry = -rad; ry <= rad; ry ++){
				Tile other = tile(rx+tilex, ry+tiley);
				
				if(other == null || other.entity == null ||(tile != null && other.entity == tile.entity)) continue;
				
				TileEntity e = other.entity;
				
				if(damaged && ((TileEntity) e).health >= ((TileEntity) e).tile.block().health)
					continue;
				
				float ndst = Vector2.dst(x, y, e.x, e.y);
				if(ndst < range && (closest == null || ndst < dst)){
					dst = ndst;
					closest = e;
				}
			}
		}

		return (TileEntity) closest;
	}
	
	public static float roundx(){
		return Mathf.round2(Graphics.mouseWorld().x, tilesize);
	}

	public static float roundy(){
		return Mathf.round2(Graphics.mouseWorld().y, tilesize);
	}

	public static int tilex(){
		return Mathf.scl2(Graphics.mouseWorld().x, tilesize);
	}

	public static int tiley(){
		return Mathf.scl2(Graphics.mouseWorld().y, tilesize);
	}
}
