package com.tyler.gson.immutable;
import com.google.gson.reflect.TypeToken;

import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import java.lang.reflect.Type;

class AnotherType {
    int a;
    String b;
    AnotherType(int a, String b) {
        if (b == null) throw new RuntimeException("b is null");
        this.a = a;
        this.b = b;
    }
}

class AnotherTypeDeserializer implements JsonDeserializer<AnotherType> {
    @Override
    public AnotherType deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jobject = (JsonObject) json;

        return new AnotherType(
                jobject.get("a").getAsInt(),
                jobject.get("b").getAsString());
    }
}

class MyData {
        int x;
        String y;
	ImmutableSet<AnotherType> zs;
	MyData(int x, String y, ImmutableSet<AnotherType> zs) {
		this.x = x;
		this.y = y;
		this.zs = zs;
	}
}

class MyDataDeserializer implements JsonDeserializer<MyData> {
	@Override
	public MyData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject o = (JsonObject) json;

		Type collectionType = new TypeToken<Collection<AnotherType>>(){}.getType();

		ImmutableSet.Builder<AnotherType> setBuilder = new ImmutableSet.Builder<AnotherType>();
		Collection<AnotherType> col = context.deserialize(o.get("zs"), collectionType);
		for (AnotherType i: col) {
			assertThat(i, IsInstanceOf.any(AnotherType.class));
			setBuilder.add(i);
		}
		ImmutableSet<AnotherType> set = setBuilder.build();

		int x = o.get("x").getAsInt();
		String y = o.get("y").getAsString();

		return new MyData(x, y, set);
	}
}

class EmbeddedInterface {
	ImmutableList<MyData> list;
	EmbeddedInterface(ImmutableList<MyData> s){
		this.list = s;
	}
}

class EmbeddedInterfaceDeserializer implements JsonDeserializer<EmbeddedInterface> {
	@Override
	public EmbeddedInterface deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Type collectionType = new TypeToken<Collection<MyData>>(){}.getType();
		Collection<MyData> col = context.deserialize(json, collectionType);
		return new EmbeddedInterface(ImmutableList.copyOf(col));
	}
}

public class ImmutableSetListDeserializerTest {
	private Gson gson = new GsonBuilder().registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
		.registerTypeAdapter(ImmutableSet.class, new ImmutableSetDeserializer())
		.registerTypeAdapter(AnotherType.class, new AnotherTypeDeserializer())
		.registerTypeAdapter(EmbeddedInterface.class, new EmbeddedInterfaceDeserializer())
		.registerTypeAdapter(MyData.class, new MyDataDeserializer())
		.create();

	@Test
	public void testDeserializeAnotherType() {
		AnotherType i = gson.fromJson("{\"a\":12,\"b\":\"hij\"}", AnotherType.class);
		assertThat(i.a, Is.is(12));
		assertThat(i.b, Is.is("hij"));
	}

	@Test
	public void testDeserializeMyData() {
		MyData i = gson.fromJson("{\"x\": 512, \"y\": \"jones\", \"zs\": [{\"a\":42,\"b\":\"hi\"}]}", MyData.class);
		assertThat(i.zs.iterator().next().a, Is.is(42));
	}

	@Test
	public void testDeserializeList() {
		EmbeddedInterface i = gson.fromJson("[{\"x\": 100, \"y\": \"james\", \"zs\": [{\"a\":-5,\"b\":\"hallo\"}]}]", EmbeddedInterface.class);
		assertThat(i.list.get(0).zs.iterator().next().a, Is.is(-5));
	}
}
