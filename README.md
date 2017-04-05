# Glin

Glin, A retrofit like network framework

### Usage

#### get Glin
if you want to use Glin only, copy this code to your gradle
``` java
compile 'org.loader:glin:1.0.2'
```
if you want to use Glin and the default OkClient, copy this code to your gradle
``` java
compile 'org.loader:glin-okclient:1.0.0'
```

#### write your client and parser, config glin
``` java
Glin glin = new Glin.Builder()
    .client(new OkClient())
    .baseUrl("http://192.168.201.39") // the basic url
    .debug(true) // debug mode
    .parserFactory(new FastJsonParserFactory()) // your parser factory
    .cacheProvider(new DefaultCacheProvider(Environment.getExternalStorageDirectory() + "/test/", 2000)) // use default cacheProvider
    .timeout(10000) // timeout in ms
    .build();
```

#### create an interface
``` java
 public interface UserApi {
      // use @ShouldCache, Glin will cache the lastest response
      // the key of cache is your url and params
      @ShouldCache
      @POST("/users/list")
      Call<User> list(@Arg("name") String userName);
  }
```

#### request the network and callback
``` java
 UserApi api = glin.create(UserApi.class, getClass().getName());
 Call<User> call = api.list("qibin");
  call.enqueue(new Callback<User>() {
      @Override
      public void onResponse(Result<User> result) {
          if (result.isOK()) {
              Toast.makeText(MainActivity.this, result.getResult().getName(), Toast.LENGTH_SHORT).show();
          }else {
              Toast.makeText(MainActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
          }
      }
  });
```

#### how to create client and parser
build your client: your client must implement the IClient interface. 
see also: [OkClient](https://github.com/qibin0506/Glin/blob/master/glinsample/src/main/java/com/example/glinsample/OkClient.java) in sample.

build your parser: your parser must extends the `Parser` class, and your parserFactory must implement the `ParserFactory` interface.
see also: [Parser](https://github.com/qibin0506/Glin/blob/master/glinsample/src/main/java/com/example/glinsample/Parser.java) and [FastJsonParserFactory](https://github.com/qibin0506/Glin/blob/master/glinsample/src/main/java/com/example/glinsample/FastJsonParserFactory.java) in sample.
      
