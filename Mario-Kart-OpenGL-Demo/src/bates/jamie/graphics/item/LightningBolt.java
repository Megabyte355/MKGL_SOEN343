package bates.jamie.graphics.item;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.particle.LightningParticle;
import bates.jamie.graphics.util.Vec3;

public class LightningBolt extends Item{
	
	public LightningBolt() {
		
	}
	
	public void pressItem(Car car) {
		for(Car c : scene.getCars()) this.struckByLightning(c);
//		if(!car.equals(this)) car.struckByLightning();
	}
	
	private void struckByLightning(Car car)
	{
		if(!car.getStarPower() && !car.getInvisible())
		{
			if(!car.getMiniature())
			{
				car.bound.e = car.bound.e.multiply(0.5f);
				car.setScale(car.getScale()/2);
			}
			
			car.setMiniature(true);
			car.setMiniatureDuration(400);
			car.velocity = 0;
			
			if(car.getSlipping()) car.setSlipDuration(24);
			else car.spin();
		}
		
		Vec3 source = getLightningVector(car); 
		scene.addParticle(new LightningParticle(source));
	}
	
	private Vec3 getLightningVector(Car car)
	{
		return bound.c.add(car.bound.u.yAxis.multiply(20));
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
