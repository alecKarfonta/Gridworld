package com.team.gridworld.Controllers;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.team.gridworld.Views.Play;

public class MyContactListener implements ContactListener {
	private static final String TAG = MyContactListener.class.getName();
	
	private Play play; // reference play so you can call functions
	
	public MyContactListener(Play play) {
		this.play = play;
	}
	
	@Override
	public void beginContact(Contact contact) {
		
	}

	@Override
	public void endContact(Contact contact) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	
		
	}
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		if (impulse.getNormalImpulses()[0] > 200) {
			
		}
	}

}
