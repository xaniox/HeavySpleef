/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.objects;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerState {

	private ItemStack[] contents;
	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;
	private float exhaustion;
	private float saturation;
	private int foodLevel;
	private double health;
	private GameMode gm;
	private Collection<PotionEffect> potioneffects;
	private float exp;
	private int level;
	private boolean fly;

	public PlayerState(ItemStack[] invContents, ItemStack helmet,
			ItemStack chestplate, ItemStack leggings, ItemStack boots,
			float exhaustion, float saturation, int foodLevel, double health,
			GameMode gm, Collection<PotionEffect> potionEffects, float exp,
			int level, boolean fly) {
		this.setContents(invContents);
		this.setHelmet(helmet);
		this.setChestplate(chestplate);
		this.setLeggings(leggings);
		this.setBoots(boots);
		this.setExhaustion(exhaustion);
		this.setSaturation(saturation);
		this.setFoodLevel(foodLevel);
		this.setHealth(health);
		this.setGm(gm);
		this.setPotioneffects(potionEffects);
		this.setExp(exp);
		this.setLevel(level);
		this.fly = fly;
	}

	public ItemStack[] getContents() {
		return contents;
	}

	public void setContents(ItemStack[] contents) {
		this.contents = contents;
	}

	public float getExhaustion() {
		return exhaustion;
	}

	public void setExhaustion(float exhaustion) {
		this.exhaustion = exhaustion;
	}

	public float getSaturation() {
		return saturation;
	}

	public void setSaturation(float saturation) {
		this.saturation = saturation;
	}

	public int getFoodLevel() {
		return foodLevel;
	}

	public void setFoodLevel(int foodLevel) {
		this.foodLevel = foodLevel;
	}

	public double getHealth() {
		return health;
	}

	public void setHealth(double health) {
		this.health = health;
	}

	public GameMode getGm() {
		return gm;
	}

	public void setGm(GameMode gm) {
		this.gm = gm;
	}

	public Collection<PotionEffect> getPotioneffects() {
		return potioneffects;
	}

	public void setPotioneffects(Collection<PotionEffect> potioneffects) {
		this.potioneffects = potioneffects;
	}

	public float getExp() {
		return exp;
	}

	public void setExp(float exp) {
		this.exp = exp;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public ItemStack getBoots() {
		return boots;
	}

	public void setBoots(ItemStack boots) {
		this.boots = boots;
	}

	public ItemStack getLeggings() {
		return leggings;
	}

	public void setLeggings(ItemStack leggings) {
		this.leggings = leggings;
	}

	public ItemStack getHelmet() {
		return helmet;
	}

	public void setHelmet(ItemStack helmet) {
		this.helmet = helmet;
	}

	public ItemStack getChestplate() {
		return chestplate;
	}

	public void setChestplate(ItemStack chestplate) {
		this.chestplate = chestplate;
	}

	public void setFly(boolean fly) {
		this.fly = fly;
	}

	public boolean isFly() {
		return this.fly;
	}

}
