package com.appspot.mccfall2017g12.photoorganizer.http;

public class Endpoints {
	private static final String SERVER = "https://mcc-fall-2017-g12.appspot.com";
	//private static final String SERVER = "http://192.168.0.17"; // local http address for debug

	public static final String CREATE = SERVER + "/photoorganizer/api/v1.0/group/create";
	public static final String JOIN = SERVER + "/photoorganizer/api/v1.0/group/join";
	public static final String DELETE = SERVER + "/photoorganizer/api/v1.0/group/delete";
}
