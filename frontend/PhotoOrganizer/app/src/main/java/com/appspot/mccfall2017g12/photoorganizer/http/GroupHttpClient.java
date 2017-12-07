package com.appspot.mccfall2017g12.photoorganizer.http;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupHttpClient {
	private static final OkHttpClient client = new OkHttpClient();
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final ObjectWriter ObjectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

	public Response createGroup(CreateGroupRequest createGroupRequest, String contentType, String authorization)
			throws IOException {
		String json = ObjectWriter.writeValueAsString(createGroupRequest);
		RequestBody body = RequestBody.create(JSON, json);
		Request request = new Request.Builder().url(Endpoints.CREATE).post(body).addHeader("Content-Type", contentType)
				.addHeader("Authorization", authorization).build();
		Response response = client.newCall(request).execute();
		return response;
	}

	public Response joinGroup(JoinGroupRequest joinGroupRequest, String contentType, String authorization)
			throws IOException {
		String json = ObjectWriter.writeValueAsString(joinGroupRequest);
		RequestBody body = RequestBody.create(JSON, json);
		Request request = new Request.Builder().url(Endpoints.JOIN).post(body).addHeader("Content-Type", contentType)
				.addHeader("Authorization", authorization).build();
		Response response = client.newCall(request).execute();
		return response;
	}

	public Response deleteGroup(DeleteGroupRequest deleteGroupRequest, String contentType, String authorization)
			throws IOException {
		String json = ObjectWriter.writeValueAsString(deleteGroupRequest);
		RequestBody body = RequestBody.create(JSON, json);
		Request request = new Request.Builder().url(Endpoints.DELETE).post(body).addHeader("Content-Type", contentType)
				.addHeader("Authorization", authorization).build();
		Response response = client.newCall(request).execute();
		return response;
	}
}
