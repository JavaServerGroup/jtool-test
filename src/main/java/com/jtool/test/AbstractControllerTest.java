package com.jtool.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class AbstractControllerTest {

	@Resource
	private WebApplicationContext webApplicationContext;

	protected MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
	}
	
	protected MockHttpServletRequestBuilder makePostByParams(String uri, Map<String, Object> params) {
		
		List<String> fileKeys = params.keySet().stream().filter(key -> params.get(key) instanceof MockMultipartFile).collect(Collectors.toList());
		
		MockHttpServletRequestBuilder mockHttpServletRequestBuilder;
		
		if(fileKeys.size() == 0) {
			mockHttpServletRequestBuilder = post(uri);
		} else {
			MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder = fileUpload(uri);
			fileKeys.stream().forEach(key -> mockMultipartHttpServletRequestBuilder.file((MockMultipartFile)params.get(key)));
			fileKeys.stream().forEach(key -> params.remove(key));
			mockHttpServletRequestBuilder = mockMultipartHttpServletRequestBuilder;
		}
		
		params.keySet().stream().forEach(e -> mockHttpServletRequestBuilder.param(e, params.get(e).toString()));
		
		return mockHttpServletRequestBuilder;
	}
	
	protected MockHttpServletRequestBuilder makeGetByParams(String uri, Map<String, Object> params) {
		MockHttpServletRequestBuilder mockHttpServletRequestBuilder = get(uri);
		params.keySet().stream().forEach(e -> mockHttpServletRequestBuilder.param(e, params.get(e).toString()));
		
		return mockHttpServletRequestBuilder;
	}

	protected String requestContentString(String uri, Map<String, Object> params) throws UnsupportedEncodingException, Exception {
		return this.mockMvc.perform(makePostByParams(uri, params)).andReturn().getResponse().getContentAsString();
	}
	
}
