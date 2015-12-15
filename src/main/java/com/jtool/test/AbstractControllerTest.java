package com.jtool.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import org.junit.Assert;
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

	@Deprecated
	protected String requestContentString(String uri, Map<String, Object> params) throws UnsupportedEncodingException, Exception {
		return this.mockMvc.perform(makePostByParams(uri, params)).andReturn().getResponse().getContentAsString();
	}

	protected String requestContentStringByPost(String uri, Object bean) throws UnsupportedEncodingException, Exception {
		Map<String, Object> params = convertBeanToRequestMap(bean);
		return this.mockMvc.perform(makePostByParams(uri, params)).andReturn().getResponse().getContentAsString();
	}

	protected String requestContentStringByGet(String uri, Object bean) throws UnsupportedEncodingException, Exception {
		Map<String, Object> params = convertBeanToRequestMap(bean);
		return this.mockMvc.perform(makeGetByParams(uri, params)).andReturn().getResponse().getContentAsString();
	}

	protected String requestContentStringByGet(String uri) throws UnsupportedEncodingException, Exception {
		Map<String, Object> params = new HashMap<>();
		return this.mockMvc.perform(makeGetByParams(uri, params)).andReturn().getResponse().getContentAsString();
	}

	protected Code requestPostResponseCode(String uri, Object bean) throws Exception {
		String source = this.requestContentStringByPost(uri, bean);
		return JSON.parseObject(source, Code.class);
	}

	protected void assertPostResponseCode(String uri, Object obj, int code) throws Exception {
		Assert.assertEquals(code, requestPostResponseCode(uri, obj).getCode());
	}

	private Map<String, Object> convertBeanToRequestMap(Object bean) {
		Map<String, Object> result = new HashMap<>();
		try {
			Field[] f = bean.getClass().getDeclaredFields();
			for(Field field : f){
				field.setAccessible(true);
				if(field.get(bean) != null) {
					result.put(field.getName(), field.get(bean));
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return result;
	}
}
