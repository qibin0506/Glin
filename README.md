# Glin

Glin, 一款灵活支持中间件的Java&Android动态代理网络框架

### 功能列表
    1. 默认支持GET, POST, DELETE, PUT请求方式
    
    2. 默认支持JSON Body请求方式
    
    3. 可自定义请求客户端
    
    4. 可扩展请求方式
    
    5. 支持中间件
    
    6. 支持Path替换
    
    7. 支持数据缓存
    
    8. more...

### 升级日志
    v2.0
        1. 支持中间件
        
        2. 添加@Path注解
        
        3. 默认添加日志中间件
        
        4. 添加Context上下文

### 使用教程

#### 获取 Glin
在你的gradle中添加如下compile
``` java
compile 'org.loader:glin:2.0'
```
如果你不想花时间定制网络请求方式, 可使用我提供的OkClient, 添加方法如下
``` java
compile 'org.loader:glin-okclient:2.0'
```
**注意: 如果使用Glin2.0, glin-okclient就必须使用2.0以上**

#### 自定义解析类
1. 通过继承`Parser`类来实现项目的数据解析类, 通常情况下需要实体类和列表类解析两种
2. 通过继承`ParserFactory`将上面实现的两个解析类告诉Glin

例子:
``` java
// CommParser.java
public class CommParser extends Parser {

    public CommParser(String key) {
        super(key);
    }

    @Override
    public <T> Result<T> parse(Class<T> klass, NetResult netResult) {
        Result<T> result = new Result<>();
        try {
            JSONObject baseObject = JSON.parseObject(netResult.getResponse());
            if (baseObject.containsKey("message")) {
                result.setMessage(baseObject.getString("message"));// message always get
            }

            result.setCode(baseObject.getIntValue("code"));
            result.setObj(result.getCode());
            result.ok(baseObject.getBooleanValue("ok"));
            if (result.isOK()) { // ok true
                if (baseObject.containsKey(mKey)) {
                    T t = baseObject.getObject(mKey, klass);
                    result.setResult(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage("error");
        }
        return result;
    }
}
```

``` java
// ListParser.java
public class ListParser extends Parser {

    public ListParser(String key) {
        super(key);
    }

    @Override
    public <T> Result<T> parse(Class<T> klass, NetResult netResult) {
        Result<T> result = new Result<>();
        try {
            JSONObject baseObject = JSON.parseObject(netResult.getResponse());
            if (baseObject.containsKey("message")) {
                result.setMessage(baseObject.getString("message"));// message always get
            }

            res.setCode(baseObject.getIntValue("code"));
            result.setObj(res.getCode());
            result.ok(baseObject.getBooleanValue("ok"));
            if (result.isOK()) { // ok true
                if (baseObject.containsKey(mKey)) {
                    JSONArray arr = baseObject.getJSONArray(mKey);
                    T t = (T) baseObject.parseArray(arr.toString(), klass);
                    result.setResult(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage("error");
        }

        return result;
    }
}
```

``` java
// FastJsonParserFactory
public class FastJsonParserFactory implements ParserFactory {

    @Override
    public Parser getParser() {
        return new CommParser("data");
    }

    @Override
    public Parser getListParser() {
        return new ListParser("data");
    }
}
```

#### 配置Glin
``` java
private static final LogHelper.LogPrinter logPrinter = new LogHelper.LogPrinter() {
        @Override
        public void print(String tag, String content) {
            Log.d(tag, content);
        }
    };
    
//....

Glin glin = new Glin.Builder()
    .client(new OkClient())
    .baseUrl("http://192.168.201.39") // the basic url
    .logChan(new LogChan(true, logPrinter)) // log printer
    .parserFactory(new FastJsonParserFactory()) // your parser factory
    .cacheProvider(new DefaultCacheProvider(Environment.getExternalStorageDirectory() + "/test/", 2000)) // use default cacheProvider
    .timeout(10000) // timeout in ms
    .build();
```

#### 创建网络访问接口
``` java
 public interface UserApi {
      // use @ShouldCache, Glin will cache the lastest response
      // the key of cache is your url and params
      @ShouldCache
      @POST("/users/list")
      Call<User> list(@Arg("name") String userName);
  }
```

#### 访问网络
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

### 高级用法

### get 请求

``` java
    @GET("http://example.com")
    Call<User> getUser(@Arg("username") String name);
```

### post 请求

``` java
    @POST("http://example.com")
    Call<User> getUser(@Arg("username") String name);
```

### delete 请求

``` java
    @DEL("http://example.com")
    Call<User> deleteUser(@Arg("username") String name);
```

### put 请求

``` java
    @PUT("http://example.com")
    Call<User> putUser(@Arg("username") String name);
```


### json 请求

``` java
    @JSON("http://example.com")
    Call<User> getUser(String name);
```

### 添加 url path

``` java
    @GET("http://example.com/{:name}/{:age}")
    Call<User> getUser(@Path("name") String name, @Path("age") int age);
```

### 中间件支持

#### 自定义中间件

继承Chan类, 实现自定义中间件, 实现run(Context ctx)方法, 在run方法里调用next()方法使流程继续.
通过调用Call的before(Chan chan)方法设置请求前的中间件, 在调用before(Chan chan)后, 可通过使用一系列的next(Chan chan)方法设置请求前的中间件.
通过调用Call的after(Chan chan)方法设置请求后的中间件, 在调用after(Chan chan)后, 可通过使用一系列的next(Chan chan)方法设置请求前的中间件.

例子(该例子实现了请求前检查用户id, 如果不存在, 则请求用户id接口, 然后发起请求获取用户信息, 最后检查用户信息)

``` java
// 检查用户id的中间件
class UserIdChan extends Chan {
    @Override
    public void run(Context ctx) {
        // 如果uid存在, 则继续流程
        if (SpUtils.get("uid") != null) {
            next();
            return;
        }
        
        glin.create(Api.class, tag).uid().enqueue(new Callback<Uid>() {
            @Override
            public void onResponse(Result<Uid> result) {
                // save uid
                // 省略成功判断
                SpUtils.put("uid", reuslt.getResult().getUid());
                next();
            }
        });
    }
}
```

``` java
// 检查用户信息中间件
class UserInfoChan extends Chan {
    @Override
    public void run(Context ctx) {
        if (SpUtils.get("user_name") == null) {
            sendUserName();
            next();
            return;
        }
        // 调用cancel方法会中断流程
        cancel();
    }
}
```

``` java
// 使用中间件
Call<User> call = glin.create(Api.class, tag).userInfo();
call.before(new UserIdChan()).next(new OtherBeforeChan())
    .after(new UserInfoChan()).next(new OtherAfterChan())
    .enqueue(new Callback<User>() {
            @Override
            public void onResponse(Result<User> result) {
                //  处理结果
            }
     });
```
