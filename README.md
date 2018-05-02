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

    v3.2
    
        1. 修复使用@POST请求，无参数时系统崩溃错误
        2. 删除默认序列化函数异常的日志打印
    
    v3.1
    
        1. 新增：`Call`添加rewriteUrl方法，可用于在中间件中重写请求的URL
        
        2. 新增：添加全局中间件`GlobalChanNode`，GlobalChanNode支持添加多个`ChanNode`, 只需按照顺序添加即可
        
        3. 修改：现在日志中间件改用`GlobalChanNode`提供支持修改日志全局中间件方式：logChanNode -> globalChanNode(GlobalChanNode before, GlobalChanNode after)
        
        4. glinsample适配Glin3.1, 添加权限处理
        
        
    v3.0
        1. 支持更加灵活的中间件cancel机制，cancel支持自定义code和message
        

    v2.3
        1. 支持中间件
        
        2. 添加@Path注解
        
        3. 默认添加日志中间件
        
        4. 添加Context上下文

### 使用教程

#### 获取 Glin
在你的gradle中添加如下compile
``` java
compile 'org.loader:glin:3.2'
```
如果你不想花时间定制网络请求方式, 可使用我提供的OkClient, 添加方法如下
``` java
compile 'org.loader:glin-okclient:3.2'
```
**注意: 如果使用Glin3.0, glin-okclient就必须使用2.3以上**

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

// request Log
LogChanNode beforeLog = new LogChanNode(true, printer);
// response log
LogChanNode afterLog = new LogChanNode(true, printer);

// Global Chan Node before request
GlobalChanNode before = new GlobalChanNode(beforeLog/*, others...*/);
// Global Chan Node after response
GlobalChanNode after = new GlobalChanNode(afterLog/*, others...*/);

Glin glin = new Glin.Builder()
    .client(new OkClient())
    .baseUrl("http://exampile.com") // the basic url
    .globalChanNode(before, after)
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
    @GET("/info")
    Call<User> getUser(@Arg("username") String name);
```

### post 请求

``` java
    @POST("/info")
    Call<User> getUser(@Arg("username") String name);
```

### delete 请求

``` java
    @DEL("/info")
    Call<User> deleteUser(@Arg("username") String name);
```

### put 请求

``` java
    @PUT("/info")
    Call<User> putUser(@Arg("username") String name);
```


### json 请求

``` java
    @JSON("/info")
    Call<User> getUser(String name);
```

### 添加 url path

``` java
    @GET("/info/{:name}/{:age}")
    Call<User> getUser(@Path("name") String name, @Path("age") int age);
```

### 中间件支持

提供全局中间件支持(`GlobalChanNode.java`)和日志中间件(`LogChanNode.java`)

#### 自定义中间件

继承ChanNode类, 实现自定义中间件, 实现run(Context ctx)方法, 在run方法里调用next()方法使流程继续.

如果想使用全局中间件，可在`GlobalChanNode`的构造中添加`ChanNode`， 可参考`glinsample`.

通过调用Call的before(ChanNode chanNode)方法设置请求前的中间件, 在调用before(ChanNode chanNode)后, 可通过使用一系列的next(ChanNode chanNode)方法设置请求前的中间件.
通过调用Call的after(ChanNode chanNode)方法设置请求后的中间件, 在调用after(ChanNode chanNode)后, 可通过使用一系列的next(ChanNode chanNode)方法设置请求后的中间件.

在中间件中，在网络请求未发起之前， 如果想要修改参数， 可使用`ctx.getCall().getParams()`进行参数修改.

例子(该例子实现了请求前检查用户id, 如果不存在, 则请求用户id接口, 然后发起请求获取用户信息, 最后检查用户信息)

``` java
// 检查用户id的中间件
class UserIdChanNode extends ChanNode {
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
class UserInfoChanNode extends ChanNode {
    @Override
    public void run(Context ctx) {
        if (SpUtils.get("user_name") == null) {
            sendUserName();
            next();
            return;
        }
        // 调用cancel方法会中断流程
        //cancel();
 	cancel(4000, "no user name")
    }
}
```

``` java
// 使用中间件
Call<User> call = glin.create(Api.class, tag).userInfo();
call.before(new UserIdChanNode()).next(new OtherBeforeChanNode())
    .after(new UserInfoChanNode()).next(new OtherAfterChanNode())
    .enqueue(new Callback<User>() {
            @Override
            public void onResponse(Result<User> result) {
                //  处理结果
            }
     });
```
