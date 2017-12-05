package com.appspot.mccfall2017g12.photoorganizer.http.http;

import java.io.IOException;

import okhttp3.Response;

public class HttpTest {
	public static void main(String[] args) {
		GroupHttpClient groupHttpClient = new GroupHttpClient();
		String authorization = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVjODExZDdmZDlkNDY2ZDhhMzAxNjRkMTM5ZWQ3Mjc2NmM4YTZmZDkifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vbWNjLWZhbGwtMjAxNy1nMTIiLCJhdWQiOiJtY2MtZmFsbC0yMDE3LWcxMiIsImF1dGhfdGltZSI6MTUxMjQ4NTAyOCwidXNlcl9pZCI6IjVGSjN5MDA5eUllTGtLMmtHNmFjSGtmQWpRNTIiLCJzdWIiOiI1RkozeTAwOXlJZUxrSzJrRzZhY0hrZkFqUTUyIiwiaWF0IjoxNTEyNDg1MDI4LCJleHAiOjE1MTI0ODg2MjgsImVtYWlsIjoib2xsaS5raWxqdW5lbkBhYWx0by5maSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJvbGxpLmtpbGp1bmVuQGFhbHRvLmZpIl19LCJzaWduX2luX3Byb3ZpZGVyIjoicGFzc3dvcmQifX0.ZRc0bVPZjQdNxZLjXbRovMfYFqWEWPRsRQ5rZZqm-VmqCuMZCTIz7sPG42YQ9ycUDdDaA4vvGqO_mgmGZ4IrMp2uB-CI6OOSCPAI5ye_JqkvGGx3XbKZM8qjGxTr-YHFDpYLTPEULSTQAcEjQkJm4sSzxbuzE7RnnVpv2sv0SMBOJI4D99V-aDQMpt46TRg5Z0U8dvVb-mvS_Nqak4OsEiG6GVa1K_ei0nJKZxx0ESJ1zxNWqwNL1zO26scV924c3YX7E3IRzYSC5GgZLZqiVUGHnFfCvi55Znd8fAy3UXizv3Oxj1DBnUBiw7AkTRsdSxhKyqbjfCsLgajeLB6Syw";

		// CreateGroupRequest createGroupRequest = new CreateGroupRequest();
		// createGroupRequest.setAuthor("5FJ3y009yIeLkK2kG6acHkfAjQ52");
		// createGroupRequest.setGroup_name("mm1");
		// createGroupRequest.setValidity(5);
		//
		// try {
		// Response response = groupHttpClient.createGroup(createGroupRequest,
		// "application/json", authorization);
		// System.out.println(response.body().string());
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		JoinGroupRequest joinGroupRequest = new JoinGroupRequest();
		joinGroupRequest.setGroup_id("a1");
		joinGroupRequest.setUser_id("5FJ3y009yIeLkK2kG6acHkfAjQ52");
		joinGroupRequest.setToken("2becaf31-c13f-4fde-a6e4-2277e59a293b");

		try {
			Response response = groupHttpClient.joinGroup(joinGroupRequest, "application/json", authorization);
			System.out.println(response.body().string());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
