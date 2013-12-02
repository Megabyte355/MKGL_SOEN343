package bates.jamie.graphics.item;

import javax.media.opengl.GL2;

import bates.jamie.graphics.entity.Car;

public class Boo extends Item{
	
	private int booDuration = 0;
	
	public Boo() {
		
	}
	
	public void pressItem(Car car) {
		booDuration = 400;
		car.setInvisible(true);
	}
	
	@Override
	public void render(GL2 gl, float trajectory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hold() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canCollide(Item item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void collide(Item item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void collide(Car car) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

}
