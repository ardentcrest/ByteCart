package com.github.catageek.ByteCart;


public interface Address {
	public Registry getRegion();
	public Registry getTrack();
	public Registry getStation();
	public boolean isTrain();
	public boolean setAddress(String s);
	public boolean setAddress(Address a);
	public boolean setTrain(boolean istrain);
}