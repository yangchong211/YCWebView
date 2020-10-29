#### 基础使用目录介绍
- 51.关于图片显示隐藏设置



### 51.关于图片显示隐藏设置
- 方法一：
    - 无图模式
        ```
        mWebView.getSettings().setLoadsImagesAutomatically(boolean enable);
        mWebView.getSettings().setBlockNetworkImage(boolean enable);
        ```
    - 有图：正常加载显示所有图片
        ```
        mWebView.getSettings().setLoadsImagesAutomatically(true)
        mWebView.getSettings().setBlockNetworkImage(false)
        ```
    - 始终无图：所有图片都不显示
        ```
        mWebView.getSettings().setLoadsImagesAutomatically(false)
        mWebView.getSettings().setBlockNetworkImage(true)
        ```
    - 注：如果是先加载的网页图片，后设置的始终无图，则已加载的图片正常显示
    - 数据网络无图
        ```
        mWebView.getSettings().setLoadsImagesAutomatically(true)
        mWebView.getSettings().setBlockNetworkImage(true)
        ```
        - 注：wifi网络，与有图模式一致；数据网络下，已经下载到缓存的图片正常显示，未下载到缓存的图片不去网络请求显示。
- 方法二：（新版sdk新加接口，如果在用的sdk中没有该接口需要更新sdk） 设置接口如下：
    ```
    mWebView.getSettingsExtension().setPicModel(model);//其中model位于IX5WebSettingsExtension中
    ```
    - 有图：model设置为IX5WebSettingsExtension.PicModel_NORMAL正常加载显示所有图片； 
    - 始终无图：model设置为IX5WebSettingsExtension.PicModel_NoPic不再显示图片（包括已加载出的图片）； 
    - 数据网络无图：model设置为IX5WebSettingsExtension.PicModel_NetNoPic数据网络下无图（已加载的图片正常显示）；


