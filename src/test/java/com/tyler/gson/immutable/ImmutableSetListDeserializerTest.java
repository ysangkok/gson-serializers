package com.tyler.gson.immutable;

import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.hamcrest.core.Is;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;
import com.google.gson.GsonBuilder;

public class ImmutableSetListDeserializerTest {
	private final ImmutableSet<String> testSet = ImmutableSet.of( "cc" );
	private final ImmutableList<ImmutableSet<String>> testSetList = ImmutableList.of( testSet );
	private final String jsonSet = "[ \"cc\" ]";
	private final String jsonSetList = "{ \"list\": [ " + jsonSet + " ] }";
	private final EmbeddedInterface tester = new GsonBuilder().registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
	                .registerTypeAdapter(ImmutableSet.class, new ImmutableSetDeserializer()).create().fromJson( jsonSetList, EmbeddedInterface.class );

	@Test
	public void testEmbeddedEquals() {
		
		ImmutableList.Builder<ImmutableSet<String>> listBuilder = new ImmutableList.Builder<ImmutableSet<String>>();
		for (int i = 0; i<tester.list.size(); i++)
			listBuilder.add(ImmutableSet.copyOf(tester.list.get(i)));
		ImmutableList<ImmutableSet<String>> immuList = listBuilder.build();
		assertThat( immuList, Is.is( testSetList ));
	}

	@Test(expected=ClassCastException.class) // the embedded set is actually an ArrayList and once we iterate it gets casted to ImmutableSet
	public void testEmbeddedException() {
		tester.list.get(0).iterator();
	}

	private static final class EmbeddedInterface { 
		private final ImmutableList<ImmutableSet<String>> list;
		
		private EmbeddedInterface(){
			this.list = ImmutableList.of();
		}		
	}
}
